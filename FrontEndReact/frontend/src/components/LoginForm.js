import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { CustomValidationUtils } from '../utils/validation';

function LoginForm() {
    const navigate = useNavigate();
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');

        if (!login.trim()) {
            setErrorMessage('Login jest wymagany');
            return;
        }

        if (!password.trim()) {
            setErrorMessage('Hasło jest wymagane');
            return;
        }

        if (CustomValidationUtils.hasDangerousCharacters(login)) {
            setErrorMessage('Login zawiera niedozwolone znaki');
            return;
        }

        setIsSubmitting(true);

        try {
            console.log(`Logowanie użytkownika: ${login}`);

            const response = await api.login(login, password);

            console.log('Zalogowano pomyślnie:', response);

            alert(`Witaj ${response.user.name}! Zalogowano pomyślnie.`);

            navigate(`/customer/${response.user.id}`);

        } catch (error) {
            console.error('Login error:', error);
            setErrorMessage(`Błąd logowania: ${error.response?.data || 'Spróbuj ponownie'}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-header bg-primary text-white">
                            <h2 className="text-center">Logowanie</h2>
                        </div>

                        <div className="card-body">
                            {errorMessage && (
                                <div className="alert alert-danger">
                                    {errorMessage}
                                </div>
                            )}

                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label htmlFor="login" className="form-label">Login</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        id="login"
                                        value={login}
                                        onChange={(e) => setLogin(e.target.value)}
                                        required
                                        disabled={isSubmitting}
                                        placeholder="Wprowadź swój login"
                                    />
                                </div>

                                <div className="mb-3">
                                    <label htmlFor="password" className="form-label">Hasło</label>
                                    <input
                                        type="password"
                                        className="form-control"
                                        id="password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                        disabled={isSubmitting}
                                        placeholder="Wprowadź swoje hasło"
                                    />
                                </div>

                                <div className="d-grid gap-2">
                                    <button
                                        type="submit"
                                        className="btn btn-primary btn-lg"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                                Logowanie...
                                            </>
                                        ) : 'Zaloguj się'}
                                    </button>

                                    <button
                                        type="button"
                                        className="btn btn-outline-secondary"
                                        onClick={() => navigate('/register')}
                                        disabled={isSubmitting}
                                    >
                                        Nie masz konta? Zarejestruj się
                                    </button>

                                    <button
                                        type="button"
                                        className="btn btn-link"
                                        onClick={() => navigate('/')}
                                        disabled={isSubmitting}
                                    >
                                        Powrót do strony głównej
                                    </button>
                                </div>
                            </form>
                        </div>

                        <div className="card-footer text-center">
                            <small className="text-muted">
                                <strong>Przykładowe konta testowe:</strong><br />
                                • Login: <code>admin</code> | Hasło: <code>admin123</code> (Administrator)<br />
                                • Login: <code>manager</code> | Hasło: <code>manager123</code> (Manager)<br />
                                • Login: <code>john_doe</code> | Hasło: <code>password123</code> (Klient)<br />
                                • Login: <code>jane_smith</code> | Hasło: <code>password123</code> (Klient)
                            </small>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default LoginForm;