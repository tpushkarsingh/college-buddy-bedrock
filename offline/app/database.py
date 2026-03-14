from sqlalchemy import create_engine, Column, String, Boolean, Integer, ForeignKey, Date, JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
import sys
import os
import uuid

# Determine a persistent path for the database
def get_base_dir():
    if getattr(sys, 'frozen', False):
        # We are bundled. 
        bundle_root = sys._MEIPASS
        # Check if we are in onedir mode and data is in _internal
        internal_path = os.path.join(bundle_root, "_internal")
        if os.path.isdir(os.path.join(internal_path, "models")):
            return internal_path
        return bundle_root
    return os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

BASE_DIR = get_base_dir()
db_path = os.path.join(BASE_DIR, "users.db")
SQLALCHEMY_DATABASE_URL = f"sqlite:///{db_path}"

engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

class User(Base):
    __tablename__ = "users"
    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    email = Column(String, unique=True, index=True, nullable=False)
    passwordHash = Column(String, nullable=False)
    fullName = Column(String, nullable=False)
    role = Column(String, nullable=False)
    isActive = Column(Boolean, default=True)
    isApproved = Column(Boolean, default=False)
    tempPasswordActive = Column(Boolean, default=True)
    studentId = Column(String, nullable=True)
    employeeId = Column(String, nullable=True)

class Department(Base):
    __tablename__ = "departments"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String, unique=True, nullable=False)
    code = Column(String, unique=True, nullable=False)

class AcademicSession(Base):
    __tablename__ = "academic_sessions"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String, unique=True, nullable=False)
    isCurrent = Column(Boolean, default=False)
    startDate = Column(Date, nullable=True)
    endDate = Column(Date, nullable=True)

class Enrollment(Base):
    __tablename__ = "enrollments"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    userId = Column(String, ForeignKey("users.id"), nullable=False)
    sessionId = Column(String, ForeignKey("academic_sessions.id"), nullable=False)
    departmentId = Column(String, ForeignKey("departments.id"), nullable=False)
    year = Column(Integer, nullable=False)
    section = Column(String, nullable=False)
    isActive = Column(Boolean, default=True)

class Document(Base):
    __tablename__ = "documents"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    filename = Column(String, nullable=False)
    uploadedBy = Column(String, ForeignKey("users.id"), nullable=False)
    departmentId = Column(String, ForeignKey("departments.id"), nullable=False)
    targetYear = Column(Integer, nullable=False)
    targetSection = Column(String, nullable=True)
    subject = Column(String, nullable=True)
    vectorIds = Column(JSON, nullable=True) # Matches the ElementCollection in Java

Base.metadata.create_all(bind=engine)

def seed_data():
    db = SessionLocal()
    # Only seed if departments don't exist
    if db.query(Department).count() == 0:
        depts = [
            Department(name="Computer Science", code="CSE"),
            Department(name="Mechanical Engineering", code="ME"),
            Department(name="Electrical Engineering", code="EE")
        ]
        db.add_all(depts)
        
        session = AcademicSession(name="2024-2025", isCurrent=True)
        db.add(session)
        db.commit()
    db.close()

seed_data()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()