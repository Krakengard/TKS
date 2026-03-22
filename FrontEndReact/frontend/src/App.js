// src/App.js
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import 'bootstrap-icons/font/bootstrap-icons.css';

// Import stron
import HomePage from './pages/HomePage';
import RegisterForm from './components/RegisterForm';
import LoginForm from './components/LoginForm';
import UserList from './components/UserList';
import AllocationList from './components/AllocationList';
import CustomerDashboard from './components/CustomerDashboard';
import AllocationForm from './components/AllocationForm';
import UserEditForm from './components/UserEditForm';
import ChangePasswordForm from './components/ChangePasswordForm';
import ResourceManagement from './components/ResourceManagement'; // DODAJ TEN IMPORT
import api from './services/api';

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userRole, setUserRole] = useState(null);
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = () => {
        const token = sessionStorage.getItem('jwtToken');
        const userStr = sessionStorage.getItem('currentUser');

        if (token && userStr) {
            setIsAuthenticated(true);
            const user = JSON.parse(userStr);
            setCurrentUser(user);
            if (user.type === 'administrator') setUserRole('ROLE_ADMIN');
            else if (user.type === 'resourceManager') setUserRole('ROLE_MANAGER');
            else if (user.type === 'customer') setUserRole('ROLE_CUSTOMER');
        } else {
            setIsAuthenticated(false);
            setUserRole(null);
            setCurrentUser(null);
        }
    };

    const handleLogout = async () => {
        try {
            await api.logout();
        } finally {
            sessionStorage.removeItem('jwtToken');
            sessionStorage.removeItem('currentUser');
            setIsAuthenticated(false);
            setUserRole(null);
            setCurrentUser(null);
            window.location.href = '/';
        }
    };

    const ProtectedRoute = ({ children, allowedRoles }) => {
        if (!isAuthenticated) {
            return <Navigate to="/login" />;
        }
        if (allowedRoles && !allowedRoles.includes(userRole)) {
            return <Navigate to="/" />;
        }
        return children;
    };

    return (
        <Router>
            {/* Navbar */}
            <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
                <div className="container">
                    <Link className="navbar-brand" to="/">
                        <i className="bi bi-calendar-check me-2"></i>
                        System Rezerwacji
                    </Link>

                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse"
                            data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false"
                            aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"></span>
                    </button>

                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav me-auto">
                            <li className="nav-item">
                                <Link className="nav-link" to="/">
                                    <i className="bi bi-house me-1"></i> Strona główna
                                </Link>
                            </li>

                            {isAuthenticated && (
                                <>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/allocations">
                                            <i className="bi bi-list-task me-1"></i> Rezerwacje
                                        </Link>
                                    </li>

                                    {userRole === 'ROLE_CUSTOMER' && (
                                        <li className="nav-item">
                                            <Link className="nav-link" to="/allocations/new">
                                                <i className="bi bi-plus-circle me-1"></i> Nowa rezerwacja
                                            </Link>
                                        </li>
                                    )}

                                    {(userRole === 'ROLE_ADMIN' || userRole === 'ROLE_MANAGER') && (
                                        <>
                                            <li className="nav-item">
                                                <Link className="nav-link" to="/users">
                                                    <i className="bi bi-people me-1"></i> Użytkownicy
                                                </Link>
                                            </li>
                                            <li className="nav-item">
                                                <Link className="nav-link" to="/resources">
                                                    <i className="bi bi-pc-display me-1"></i> Zasoby
                                                </Link>
                                            </li>
                                        </>
                                    )}
                                </>
                            )}
                        </ul>

                        <ul className="navbar-nav">
                            {isAuthenticated ? (
                                <li className="nav-item dropdown">
                                    <a className="nav-link dropdown-toggle" href="#" id="userDropdown"
                                       role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                        <i className="bi bi-person-circle me-1"></i> {currentUser?.name || 'Konto'}
                                    </a>
                                    <ul className="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
                                        <li>
                                            <Link className="dropdown-item" to={`/customer/${currentUser?.id}`}>
                                                <i className="bi bi-person me-2"></i> Panel klienta
                                            </Link>
                                        </li>
                                        <li>
                                            <Link className="dropdown-item" to="/change-password">
                                                <i className="bi bi-key me-2"></i> Zmień hasło
                                            </Link>
                                        </li>
                                        <li><hr className="dropdown-divider" /></li>
                                        <li>
                                            <button className="dropdown-item" onClick={handleLogout}>
                                                <i className="bi bi-box-arrow-right me-2"></i> Wyloguj
                                            </button>
                                        </li>
                                    </ul>
                                </li>
                            ) : (
                                <>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/register">
                                            <i className="bi bi-person-plus me-1"></i> Rejestracja
                                        </Link>
                                    </li>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/login">
                                            <i className="bi bi-box-arrow-in-right me-1"></i> Logowanie
                                        </Link>
                                    </li>
                                </>
                            )}
                        </ul>
                    </div>
                </div>
            </nav>

            {/* Główna zawartość */}
            <div className="container mt-4">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/register" element={<RegisterForm />} />
                    <Route path="/login" element={<LoginForm />} />

                    {/* Protected Routes */}
                    <Route path="/users" element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                            <UserList />
                        </ProtectedRoute>
                    } />

                    <Route path="/users/edit/:id" element={
                        <ProtectedRoute>
                            <UserEditForm />
                        </ProtectedRoute>
                    } />

                    <Route path="/allocations" element={
                        <ProtectedRoute>
                            <AllocationList />
                        </ProtectedRoute>
                    } />

                    <Route path="/customer/:id" element={
                        <ProtectedRoute>
                            <CustomerDashboard />
                        </ProtectedRoute>
                    } />

                    <Route path="/allocations/new" element={
                        <ProtectedRoute allowedRoles={['ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_MANAGER']}>
                            <AllocationForm />
                        </ProtectedRoute>
                    } />

                    <Route path="/change-password" element={
                        <ProtectedRoute>
                            <ChangePasswordForm />
                        </ProtectedRoute>
                    } />

                    {/* DODAJ TĄ TRASĘ */}
                    <Route path="/resources" element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                            <ResourceManagement />
                        </ProtectedRoute>
                    } />
                </Routes>
            </div>

            {/* Footer */}
            <footer className="bg-dark text-white mt-5 py-3">
                <div className="container text-center">
                    <small>System Rezerwacji Zasobów &copy; 2026</small>
                    {isAuthenticated && currentUser && (
                        <div className="mt-2">
                            <small>Zalogowany jako: <strong>{currentUser.name}</strong>
                                ({currentUser.login}) -
                                <span className="ms-2 badge bg-info">
                                    {currentUser.type === 'administrator' ? 'Administrator' :
                                        currentUser.type === 'resourceManager' ? 'Manager zasobów' :
                                            'Klient'}
                                </span>
                            </small>
                        </div>
                    )}
                </div>
            </footer>
        </Router>
    );
}

export default App;