import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

function ChangePasswordForm() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    const [errors, setErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });

        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: ''
            });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.currentPassword.trim()) {
            newErrors.currentPassword = 'Obecne hasło jest wymagane';
        }

        if (!formData.newPassword.trim()) {
            newErrors.newPassword = 'Nowe hasło jest wymagane';
        } else if (formData.newPassword.length < 6) {
            newErrors.newPassword = 'Hasło musi mieć co najmniej 6 znaków';
        }

        if (!formData.confirmPassword.trim()) {
            newErrors.confirmPassword = 'Potwierdzenie hasła jest wymagane';
        } else if (formData.newPassword !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Hasła nie są identyczne';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);

        try {
            await api.changePassword(formData.currentPassword, formData.newPassword);

            setSuccessMessage('Hasło zostało pomyślnie zmienione!');
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });

            setTimeout(() => {
                navigate(-1);
            }, 2000);

        } catch (error) {
            console.error('Błąd zmiany hasła:', error);

            let komunikat = 'Wystąpił nieznany błąd. Spróbuj ponownie później.';

            if (error.response) {
                if (error.response.data?.message) {
                    komunikat = error.response.data.message;
                }
                else if (error.response.status === 400) {
                    komunikat = 'Nieprawidłowe dane – sprawdź obecne hasło';
                }
                else if (error.response.status === 401 || error.response.status === 403) {
                    komunikat = 'Sesja wygasła lub brak uprawnień – zaloguj się ponownie';
                }
            }
            else if (error.request) {
                komunikat = 'Brak odpowiedzi od serwera. Sprawdź połączenie internetowe.';
            }

            setErrorMessage(komunikat);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-header bg-warning text-white">
                            <h2 className="text-center">
                                <i className="bi bi-key me-2"></i>
                                Zmiana hasła
                            </h2>
                        </div>

                        <div className="card-body">
                            {errorMessage && (
                                <div className="alert alert-danger">
                                    {errorMessage}
                                </div>
                            )}

                            {successMessage && (
                                <div className="alert alert-success">
                                    {successMessage}
                                </div>
                            )}

                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label htmlFor="currentPassword" className="form-label">Obecne hasło *</label>
                                    <input
                                        type="password"
                                        className={`form-control ${errors.currentPassword ? 'is-invalid' : ''}`}
                                        id="currentPassword"
                                        name="currentPassword"
                                        value={formData.currentPassword}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.currentPassword && (
                                        <div className="invalid-feedback">
                                            {errors.currentPassword}
                                        </div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label htmlFor="newPassword" className="form-label">Nowe hasło *</label>
                                    <input
                                        type="password"
                                        className={`form-control ${errors.newPassword ? 'is-invalid' : ''}`}
                                        id="newPassword"
                                        name="newPassword"
                                        value={formData.newPassword}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.newPassword && (
                                        <div className="invalid-feedback">
                                            {errors.newPassword}
                                        </div>
                                    )}
                                    <small className="form-text text-muted">
                                        Hasło musi mieć co najmniej 6 znaków
                                    </small>
                                </div>

                                <div className="mb-3">
                                    <label htmlFor="confirmPassword" className="form-label">Potwierdź nowe hasło *</label>
                                    <input
                                        type="password"
                                        className={`form-control ${errors.confirmPassword ? 'is-invalid' : ''}`}
                                        id="confirmPassword"
                                        name="confirmPassword"
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.confirmPassword && (
                                        <div className="invalid-feedback">
                                            {errors.confirmPassword}
                                        </div>
                                    )}
                                </div>

                                <div className="d-grid gap-2 d-md-flex justify-content-md-end">
                                    <button
                                        type="button"
                                        className="btn btn-secondary me-md-2"
                                        onClick={() => navigate(-1)}
                                        disabled={isSubmitting}
                                    >
                                        Anuluj
                                    </button>
                                    <button
                                        type="submit"
                                        className="btn btn-warning"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                                Zmieniam...
                                            </>
                                        ) : 'Zmień hasło'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ChangePasswordForm;