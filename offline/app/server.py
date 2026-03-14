from fastapi import FastAPI, UploadFile, File, Form, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from pydantic import BaseModel, field_validator
from typing import List, Optional, Union
import os
import shutil
import sys
from jose import JWTError, jwt
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse

from app.rag import ask
from app.ingest import ingest
from app.database import get_db, User, Department, AcademicSession, Enrollment, Document
from app.auth_utils import verify_password, get_password_hash, create_access_token, SECRET_KEY, ALGORITHM

app = FastAPI()

# Enable CORS for frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="api/v1/auth/authenticate")

# --- Request Models (Matching Frontend camelCase) ---
class AuthRequest(BaseModel):
    email: str
    password: str

class RegisterRequest(BaseModel):
    fullName: str
    email: str
    password: str
    role: str = "STUDENT"
    studentId: Optional[str] = None
    employeeId: Optional[str] = None
    departmentCode: Optional[str] = None
    year: Optional[int] = None
    section: Optional[str] = None

    @field_validator('year', 'studentId', 'employeeId', 'departmentCode', 'section', mode='before')
    @classmethod
    def empty_string_to_none(cls, v):
        if v == "":
            return None
        return v

class ChatRequest(BaseModel):
    query: str

# --- Auth Helpers ---
def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    user = db.query(User).filter(User.email == email).first()
    if user is None:
        raise credentials_exception
    return user

# --- Metadata Endpoints ---
@app.get("/api/v1/departments")
def get_departments(db: Session = Depends(get_db)):
    return db.query(Department).all()

@app.get("/api/v1/sessions/current")
def get_current_session(db: Session = Depends(get_db)):
    session = db.query(AcademicSession).filter(AcademicSession.isCurrent == True).first()
    if not session:
        raise HTTPException(status_code=404, detail="No active session found")
    return session

# --- Auth Endpoints ---
@app.post("/api/v1/auth/register")
def register(request: RegisterRequest, db: Session = Depends(get_db)):
    if db.query(User).filter(User.email == request.email).first():
        raise HTTPException(status_code=400, detail="Email already exists")
    
    # 1. Create User
    new_user = User(
        email=request.email,
        passwordHash=get_password_hash(request.password),
        fullName=request.fullName,
        role=request.role,
        isActive=True,
        isApproved=False,
        tempPasswordActive=False,
        studentId=request.studentId,
        employeeId=request.employeeId
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    # 2. Create Enrollment if details provided (Production Logic)
    if request.departmentCode and request.year and request.section:
        dept = db.query(Department).filter(Department.code == request.departmentCode).first()
        session = db.query(AcademicSession).filter(AcademicSession.isCurrent == True).first()
        
        if dept and session:
            enrollment = Enrollment(
                userId=new_user.id,
                sessionId=session.id,
                departmentId=dept.id,
                year=request.year,
                section=request.section,
                isActive=True
            )
            db.add(enrollment)
            db.commit()
    
    access_token = create_access_token(data={"sub": new_user.email})
    return {
        "token": access_token,
        "fullName": new_user.fullName,
        "role": new_user.role,
        "isApproved": new_user.isApproved
    }

@app.post("/api/v1/auth/authenticate")
def authenticate(request: AuthRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == request.email).first()
    if not user or not verify_password(request.password, user.passwordHash):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    
    if not user.isApproved:
        raise HTTPException(status_code=403, detail="Account pending approval")

    access_token = create_access_token(data={"sub": user.email})
    return {
        "token": access_token,
        "fullName": user.fullName,
        "role": user.role,
        "isApproved": user.isApproved
    }

# --- Chat ---
@app.post("/api/v1/chat")
def chat(request: ChatRequest, current_user: User = Depends(get_current_user)):
    answer = ask(request.query)
    return {"answer": answer}

@app.get("/api/v1/chat/history")
def get_history(current_user: User = Depends(get_current_user)):
    return [] 

# --- Documents ---
@app.post("/api/v1/documents")
async def upload_document(
    file: UploadFile = File(...),
    departmentCode: str = Form(...),
    targetYear: int = Form(...),
    subject: str = Form(...),
    targetSection: Optional[str] = Form(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role not in ["FACULTY", "FACULTY_ASSISTANT", "ADMIN"]:
        raise HTTPException(status_code=403, detail="Unauthorized")

    dept = db.query(Department).filter(Department.code == departmentCode).first()
    if not dept:
        raise HTTPException(status_code=404, detail="Department not found")

    # Save physical file
    os.makedirs("data", exist_ok=True)
    file_path = os.path.join("data", file.filename)
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    
    try:
        # 1. Ingest into Vector Store
        ingest(file_path)
        
        # 2. Record in DB (Production Parity)
        new_doc = Document(
            filename=file.filename,
            uploadedBy=current_user.id,
            departmentId=dept.id,
            targetYear=targetYear,
            targetSection=targetSection,
            subject=subject
        )
        db.add(new_doc)
        db.commit()
        
        return {"message": "Document ingested and filed successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/v1/health")
def health_check():
    return {"status": "ok"}

# --- Admin Endpoints ---
@app.get("/api/v1/admin/users/pending")
def get_pending_users(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role != "ADMIN":
        raise HTTPException(status_code=403, detail="Admin access required")
    return db.query(User).filter(User.isApproved == False).all()

@app.post("/api/v1/admin/users/{user_id}/approve")
def approve_user(user_id: str, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role != "ADMIN":
        raise HTTPException(status_code=403, detail="Admin access required")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    user.isApproved = True
    db.commit()
    return {"message": "User approved successfully"}

@app.post("/api/v1/admin/departments")
def create_department(dept: dict, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role != "ADMIN":
        raise HTTPException(status_code=403, detail="Admin access required")
    new_dept = Department(name=dept["name"], code=dept["code"])
    db.add(new_dept)
    db.commit()
    db.refresh(new_dept)
    return new_dept

@app.post("/api/v1/admin/sessions")
def create_session(sess: dict, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role != "ADMIN":
        raise HTTPException(status_code=403, detail="Admin access required")
    # If this is set as current, unset others
    if sess.get("isCurrent"):
        db.query(AcademicSession).update({AcademicSession.isCurrent: False})
    
    new_session = AcademicSession(name=sess["name"], isCurrent=sess.get("isCurrent", False))
    db.add(new_session)
    db.commit()
    db.refresh(new_session)
    return new_session

# --- Static File Serving (React Frontend) ---
# This MUST be last to avoid intercepting API routes
def get_base_dir():
    if getattr(sys, 'frozen', False):
        bundle_root = sys._MEIPASS
        internal_path = os.path.join(bundle_root, "_internal")
        if os.path.isdir(os.path.join(internal_path, "dist")):
            return internal_path
        return bundle_root
    return os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

bundle_dir = get_base_dir()
dist_path = os.path.abspath(os.path.join(bundle_dir, "dist"))
# Handle nested dist folder if it exists
if not os.path.isfile(os.path.join(dist_path, "index.html")):
    nested_dist_path = os.path.join(dist_path, "dist")
    if os.path.isfile(os.path.join(nested_dist_path, "index.html")):
        dist_path = nested_dist_path

if os.path.exists(dist_path):
    assets_path = os.path.join(dist_path, "assets")
    if os.path.exists(assets_path):
        app.mount("/assets", StaticFiles(directory=assets_path), name="assets")

    @app.get("/{full_path:path}")
    async def serve_frontend(full_path: str):
        if full_path.startswith("api/"):
            raise HTTPException(status_code=404)
        
        file_path = os.path.join(dist_path, full_path)
        if os.path.isfile(file_path):
            return FileResponse(file_path)
            
        return FileResponse(os.path.join(dist_path, "index.html"))