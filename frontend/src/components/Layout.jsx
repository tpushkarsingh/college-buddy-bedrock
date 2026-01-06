import { useAuth } from '../context/AuthContext';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, MessageSquare, Upload, Users, LogOut, User } from 'lucide-react';
import clsx from 'clsx';

export default function Layout({ children }) {
    const { user, logout } = useAuth();
    const location = useLocation();

    const navigation = [
        { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard, current: location.pathname === '/dashboard' },
        ...(user?.role === 'STUDENT' ? [{ name: 'Chat', href: '/chat', icon: MessageSquare, current: location.pathname === '/chat' }] : []),
        ...(user?.role === 'FACULTY' || user?.role === 'FACULTY_ASSISTANT' ? [{ name: 'Upload', href: '/upload', icon: Upload, current: location.pathname === '/upload' }] : []),
        ...(user?.role === 'ADMIN' ? [{ name: 'Admin', href: '/admin', icon: Users, current: location.pathname === '/admin' }] : []),
    ];

    return (
        <div className="min-h-screen bg-gray-100 flex">
            {/* Sidebar */}
            <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0 bg-dark text-white">
                <div className="flex-1 flex flex-col min-h-0">
                    <div className="flex items-center h-16 flex-shrink-0 px-4 bg-gray-900">
                        <h1 className="text-xl font-bold text-white">College Buddy</h1>
                    </div>
                    <div className="flex-1 flex flex-col overflow-y-auto">
                        <nav className="flex-1 px-2 py-4 space-y-1">
                            {navigation.map((item) => (
                                <Link
                                    key={item.name}
                                    to={item.href}
                                    className={clsx(
                                        item.current ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white',
                                        'group flex items-center px-2 py-2 text-sm font-medium rounded-md'
                                    )}
                                >
                                    <item.icon
                                        className={clsx(
                                            item.current ? 'text-gray-300' : 'text-gray-400 group-hover:text-gray-300',
                                            'mr-3 flex-shrink-0 h-6 w-6'
                                        )}
                                        aria-hidden="true"
                                    />
                                    {item.name}
                                </Link>
                            ))}
                        </nav>
                    </div>
                    <div className="flex-shrink-0 flex bg-gray-800 p-4">
                        <div className="flex items-center w-full">
                            <div className="flex-shrink-0">
                                <User className="h-8 w-8 rounded-full bg-gray-500 p-1" />
                            </div>
                            <div className="ml-3">
                                <p className="text-sm font-medium text-white">{user?.fullName}</p>
                                <p className="text-xs font-medium text-gray-300">{user?.role}</p>
                            </div>
                            <button onClick={logout} className="ml-auto text-gray-400 hover:text-white">
                                <LogOut className="h-5 w-5" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main content */}
            <div className="md:pl-64 flex flex-col flex-1">
                <main className="flex-1">
                    <div className="py-6">
                        <div className="max-w-7xl mx-auto px-4 sm:px-6 md:px-8">
                            {children}
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
}
