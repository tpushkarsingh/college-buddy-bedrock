import { useState } from 'react';
import api from '../lib/api';
import { Upload, FileText, CheckCircle, AlertCircle } from 'lucide-react';

export default function UploadForm() {
    const [file, setFile] = useState(null);
    const [metadata, setMetadata] = useState({
        departmentCode: '',
        targetYear: '',
        targetSection: '',
        subject: ''
    });
    const [status, setStatus] = useState('idle'); // idle, uploading, success, error

    const handleFileChange = (e) => {
        if (e.target.files) {
            setFile(e.target.files[0]);
        }
    };

    const handleChange = (e) => {
        setMetadata({ ...metadata, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) return;

        setStatus('uploading');
        const formData = new FormData();
        formData.append('file', file);
        formData.append('departmentCode', metadata.departmentCode);
        formData.append('targetYear', metadata.targetYear);
        formData.append('targetSection', metadata.targetSection);
        formData.append('subject', metadata.subject);

        try {
            await api.post('/documents', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            setStatus('success');
            setFile(null);
            setMetadata({ departmentCode: '', targetYear: '', targetSection: '', subject: '' });
        } catch (error) {
            setStatus('error');
        }
    };

    return (
        <div className="bg-white shadow sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Upload Study Material</h3>
                <div className="mt-2 max-w-xl text-sm text-gray-500">
                    <p>Upload PDF documents for students. Ensure metadata is correct for proper filtering.</p>
                </div>
                <form className="mt-5 space-y-4" onSubmit={handleSubmit}>

                    <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
                        <div className="sm:col-span-3">
                            <label className="block text-sm font-medium text-gray-700">Department Code</label>
                            <input type="text" name="departmentCode" required value={metadata.departmentCode} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm" />
                        </div>

                        <div className="sm:col-span-3">
                            <label className="block text-sm font-medium text-gray-700">Subject</label>
                            <input type="text" name="subject" required value={metadata.subject} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm" />
                        </div>

                        <div className="sm:col-span-3">
                            <label className="block text-sm font-medium text-gray-700">Target Year (1-4)</label>
                            <input type="number" name="targetYear" required min="1" max="4" value={metadata.targetYear} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm" />
                        </div>

                        <div className="sm:col-span-3">
                            <label className="block text-sm font-medium text-gray-700">Target Section (Optional)</label>
                            <input type="text" name="targetSection" value={metadata.targetSection} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm" placeholder="Leave empty for all sections" />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700">PDF Document</label>
                        <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md">
                            <div className="space-y-1 text-center">
                                <FileText className="mx-auto h-12 w-12 text-gray-400" />
                                <div className="flex text-sm text-gray-600">
                                    <label htmlFor="file-upload" className="relative cursor-pointer bg-white rounded-md font-medium text-primary hover:text-indigo-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-primary">
                                        <span>Upload a file</span>
                                        <input id="file-upload" name="file-upload" type="file" className="sr-only" accept=".pdf" onChange={handleFileChange} required />
                                    </label>
                                    <p className="pl-1">or drag and drop</p>
                                </div>
                                <p className="text-xs text-gray-500">PDF up to 10MB</p>
                                {file && <p className="text-sm text-green-600 font-medium">{file.name}</p>}
                            </div>
                        </div>
                    </div>

                    <div className="flex justify-end">
                        <button
                            type="submit"
                            disabled={status === 'uploading'}
                            className="inline-flex items-center px-6 py-3 border-2 border-transparent text-base font-bold rounded-lg shadow-lg text-white bg-indigo-600 hover:bg-indigo-700 hover:shadow-xl focus:outline-none focus:ring-4 focus:ring-indigo-300 disabled:opacity-50 transition-all duration-200 transform hover:scale-105"
                        >
                            {status === 'uploading' ? 'Uploading...' : 'Upload Document'}
                            <Upload className="ml-2 -mr-1 h-5 w-5" />
                        </button>
                    </div>

                    {status === 'success' && (
                        <div className="rounded-md bg-green-50 p-4">
                            <div className="flex">
                                <div className="flex-shrink-0">
                                    <CheckCircle className="h-5 w-5 text-green-400" />
                                </div>
                                <div className="ml-3">
                                    <p className="text-sm font-medium text-green-800">Document uploaded successfully!</p>
                                </div>
                            </div>
                        </div>
                    )}

                    {status === 'error' && (
                        <div className="rounded-md bg-red-50 p-4">
                            <div className="flex">
                                <div className="flex-shrink-0">
                                    <AlertCircle className="h-5 w-5 text-red-400" />
                                </div>
                                <div className="ml-3">
                                    <p className="text-sm font-medium text-red-800">Upload failed. Please try again.</p>
                                </div>
                            </div>
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
}
