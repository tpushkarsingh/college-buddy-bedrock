import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Layout from '../components/Layout';
import ChatInterface from '../components/ChatInterface';
import UploadForm from '../components/UploadForm';
import AdminPanel from '../components/AdminPanel';
import UserManagement from '../components/UserManagement';

export default function Dashboard() {
    const { user } = useAuth();
    const [adminTab, setAdminTab] = useState('approvals');

    return (
        <Layout>
            <div className="mb-6">
                <h1 className="text-2xl font-semibold text-gray-900">
                    {user?.role === 'STUDENT' && 'Study Assistant'}
                    {user?.role === 'FACULTY' && 'Faculty Dashboard'}
                    {user?.role === 'ADMIN' && 'Admin Dashboard'}
                </h1>
                <p className="mt-1 text-sm text-gray-500">
                    Welcome back, {user?.fullName}!
                </p>
            </div>

            {user?.role === 'STUDENT' && <ChatInterface />}
            {(user?.role === 'FACULTY' || user?.role === 'FACULTY_ASSISTANT') && <UploadForm />}
            {user?.role === 'ADMIN' && (
                <div>
                    {/* Admin Tabs */}
                    <div className="border-b border-gray-200 mb-6">
                        <nav className="-mb-px flex space-x-8">
                            <button
                                onClick={() => setAdminTab('approvals')}
                                className={`${adminTab === 'approvals'
                                        ? 'border-indigo-500 text-indigo-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                    } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm transition-colors`}
                            >
                                Pending Approvals
                            </button>
                            <button
                                onClick={() => setAdminTab('users')}
                                className={`${adminTab === 'users'
                                        ? 'border-indigo-500 text-indigo-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                    } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm transition-colors`}
                            >
                                Manage Users
                            </button>
                        </nav>
                    </div>

                    {/* Tab Content */}
                    {adminTab === 'approvals' && <AdminPanel />}
                    {adminTab === 'users' && <UserManagement />}
                </div>
            )}
        </Layout>
    );
}
