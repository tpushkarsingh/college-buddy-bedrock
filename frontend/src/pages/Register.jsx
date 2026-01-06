import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { UserPlus, GraduationCap } from 'lucide-react';

export default function Register() {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        role: 'STUDENT',
        departmentCode: '',
        year: '',
        section: '',
        studentId: '',
        employeeId: ''
    });
    const [error, setError] = useState('');
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Convert year to integer if present
            const data = { ...formData };
            if (data.year) data.year = parseInt(data.year);

            await register(data);
            // Redirect to login with a message (or dashboard if auto-login, but usually requires approval)
            // Since approval is needed, maybe show a success message instead of redirecting to dashboard immediately if not approved.
            // But AuthService.register returns a token? 
            // Wait, backend says "Enrollment is inactive until User is approved".
            // And AuthService.register returns isApproved=false.
            // So they can't do much.
            navigate('/login');
            alert('Registration successful! Please wait for admin approval.');
        } catch (err) {
            setError('Registration failed. Email might be taken.');
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
            <div className="sm:mx-auto sm:w-full sm:max-w-md">
                <div className="flex justify-center">
                    <GraduationCap className="h-12 w-12 text-primary" />
                </div>
                <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
                    Create your account
                </h2>
                <p className="mt-2 text-center text-sm text-gray-600">
                    Already have an account?{' '}
                    <Link to="/login" className="font-medium text-primary hover:text-indigo-500">
                        Sign in
                    </Link>
                </p>
            </div>

            <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
                <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 border border-gray-100">
                    <form className="space-y-6" onSubmit={handleSubmit}>
                        {error && (
                            <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md text-sm">
                                {error}
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-medium text-gray-700">Full Name</label>
                            <input name="fullName" type="text" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700">Email address</label>
                            <input name="email" type="email" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700">Password</label>
                            <input name="password" type="password" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700">Role</label>
                            <select name="role" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} value={formData.role}>
                                <option value="STUDENT">Student</option>
                                <option value="FACULTY">Faculty</option>
                                <option value="FACULTY_ASSISTANT">Faculty Assistant</option>
                            </select>
                        </div>

                        {formData.role === 'STUDENT' && (
                            <>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Department Code (e.g., CSE)</label>
                                    <input name="departmentCode" type="text" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700">Year (1-4)</label>
                                        <input name="year" type="number" min="1" max="4" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700">Section (A, B...)</label>
                                        <input name="section" type="text" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                                    </div>
                                </div>
                            </>
                        )}

                        {(formData.role === 'FACULTY' || formData.role === 'FACULTY_ASSISTANT') && (
                            <div>
                                <label className="block text-sm font-medium text-gray-700">Employee ID</label>
                                <input name="employeeId" type="text" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                            </div>
                        )}

                        {formData.role === 'STUDENT' && (
                            <div>
                                <label className="block text-sm font-medium text-gray-700">Student ID</label>
                                <input name="studentId" type="text" required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary sm:text-sm px-3 py-2 border" onChange={handleChange} />
                            </div>
                        )}

                        <div>
                            <button
                                type="submit"
                                className="w-full flex justify-center items-center py-3 px-6 border-2 border-transparent rounded-lg shadow-lg text-base font-bold text-white bg-indigo-600 hover:bg-indigo-700 hover:shadow-xl focus:outline-none focus:ring-4 focus:ring-indigo-300 transition-all duration-200 transform hover:scale-105"
                            >
                                <UserPlus className="h-5 w-5 mr-2" />
                                Register
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
