import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { CustomValidationUtils } from '../utils/validation';

function RegisterForm() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        login: '',
        name: '',
        email: '',
        phoneNumber: '',
        address: '',
        password: '',
        confirmPassword: ''
    });

    const [errors, setErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
        // Wyczyść błąd dla tego pola
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: ''
            });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.login.trim()) {
            newErrors.login = 'Login jest wymagany';
        } else if (!CustomValidationUtils.isValidLogin(formData.login)) {
            newErrors.login = 'Login musi mieć 3-50 znaków (tylko litery, cyfry, podkreślniki)';
        } else if (CustomValidationUtils.hasDangerousCharacters(formData.login)) {
            newErrors.login = 'Login zawiera niedozwolone znaki';
        } else if (CustomValidationUtils.isReservedLogin(formData.login)) {
            newErrors.login = 'Login jest zarezerwowany (admin, root, system, administrator)';
        }

        if (!formData.name.trim()) {
            newErrors.name = 'Imię i nazwisko są wymagane';
        } else if (!CustomValidationUtils.isValidName(formData.name)) {
            newErrors.name = 'Imię i nazwisko musi mieć 2-100 znaków';
        } else if (CustomValidationUtils.hasDangerousCharacters(formData.name)) {
            newErrors.name = 'Imię i nazwisko zawiera niedozwolone znaki';
        }

        if (!formData.email.trim()) {
            newErrors.email = 'Email jest wymagany';
        } else if (!CustomValidationUtils.isValidEmail(formData.email)) {
            newErrors.email = 'Nieprawidłowy format email';
        } else if (CustomValidationUtils.hasDangerousCharacters(formData.email)) {
            newErrors.email = 'Email zawiera niedozwolone znaki';
        }

        if (!formData.phoneNumber.trim()) {
            newErrors.phoneNumber = 'Numer telefonu jest wymagany';
        } else if (!CustomValidationUtils.isValidPhoneNumber(formData.phoneNumber)) {
            newErrors.phoneNumber = 'Nieprawidłowy format numeru telefonu';
        }

        if (!formData.address.trim()) {
            newErrors.address = 'Adres jest wymagany';
        } else if (!CustomValidationUtils.isValidAddress(formData.address)) {
            newErrors.address = 'Adres musi mieć 5-200 znaków';
        } else if (CustomValidationUtils.hasDangerousCharacters(formData.address)) {
            newErrors.address = 'Adres zawiera niedozwolone znaki';
        }

        if (!formData.password.trim()) {
            newErrors.password = 'Hasło jest wymagane';
        } else if (formData.password.length < 6) {
            newErrors.password = 'Hasło musi mieć co najmniej 6 znaków';
        }

        if (!formData.confirmPassword.trim()) {
            newErrors.confirmPassword = 'Potwierdzenie hasła jest wymagane';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Hasła nie są identyczne';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);

        try {
            const customer = {
                login: formData.login,
                name: formData.name,
                email: formData.email,
                phoneNumber: formData.phoneNumber,
                address: formData.address,
                password: formData.password,
                type: 'customer',
                active: true
            };

            const response = await api.createUser(customer);

            alert(`Rejestracja zakończona pomyślnie! Witamy ${response.user.name}!`);

            // Automatyczne logowanie po rejestracji
            const loginResponse = await api.login(formData.login, formData.password);

            navigate(`/customer/${response.user.id}`);

        } catch (error) {
            console.error('Registration error:', error);
            setErrorMessage(`Wystąpił błąd podczas rejestracji: ${error.response?.data || 'Spróbuj ponownie'}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card">
                        <div className="card-header bg-success text-white">
                            <h2 className="text-center">Rejestracja nowego klienta</h2>
                        </div>

                        <div className="card-body">
                            {errorMessage && (
                                <div className="alert alert-danger">
                                    {errorMessage}
                                </div>
                            )}

                            <form onSubmit={handleSubmit}>
                                {/* LOGIN */}
                                <div className="mb-3">
                                    <label htmlFor="login" className="form-label">Login *</label>
                                    <input
                                        type="text"
                                        className={`form-control ${errors.login ? 'is-invalid' : ''}`}
                                        id="login"
                                        name="login"
                                        value={formData.login}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.login && (
                                        <div className="invalid-feedback">
                                            {errors.login}
                                        </div>
                                    )}
                                    <small className="form-text text-muted">
                                        Login musi mieć od 3 do 50 znaków (tylko litery, cyfry, podkreślniki)
                                    </small>
                                </div>

                                {/* HASŁO */}
                                <div className="mb-3">
                                    <label htmlFor="password" className="form-label">Hasło *</label>
                                    <input
                                        type="password"
                                        className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                                        id="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.password && (
                                        <div className="invalid-feedback">
                                            {errors.password}
                                        </div>
                                    )}
                                    <small className="form-text text-muted">
                                        Hasło musi mieć co najmniej 6 znaków
                                    </small>
                                </div>

                                {/* POTWIERDZENIE HASŁA */}
                                <div className="mb-3">
                                    <label htmlFor="confirmPassword" className="form-label">Potwierdź hasło *</label>
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

                                {/* IMIĘ I NAZWISKO */}
                                <div className="mb-3">
                                    <label htmlFor="name" className="form-label">Imię i nazwisko *</label>
                                    <input
                                        type="text"
                                        className={`form-control ${errors.name ? 'is-invalid' : ''}`}
                                        id="name"
                                        name="name"
                                        value={formData.name}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.name && (
                                        <div className="invalid-feedback">
                                            {errors.name}
                                        </div>
                                    )}
                                </div>

                                {/* EMAIL */}
                                <div className="mb-3">
                                    <label htmlFor="email" className="form-label">Email *</label>
                                    <input
                                        type="email"
                                        className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                                        id="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.email && (
                                        <div className="invalid-feedback">
                                            {errors.email}
                                        </div>
                                    )}
                                </div>

                                {/* TELEFON */}
                                <div className="mb-3">
                                    <label htmlFor="phoneNumber" className="form-label">Numer telefonu *</label>
                                    <input
                                        type="tel"
                                        className={`form-control ${errors.phoneNumber ? 'is-invalid' : ''}`}
                                        id="phoneNumber"
                                        name="phoneNumber"
                                        value={formData.phoneNumber}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.phoneNumber && (
                                        <div className="invalid-feedback">
                                            {errors.phoneNumber}
                                        </div>
                                    )}
                                    <small className="form-text text-muted">
                                        Format: +48 123 456 789 lub 123-456-789
                                    </small>
                                </div>

                                {/* ADRES */}
                                <div className="mb-3">
                                    <label htmlFor="address" className="form-label">Adres *</label>
                                    <textarea
                                        className={`form-control ${errors.address ? 'is-invalid' : ''}`}
                                        id="address"
                                        name="address"
                                        value={formData.address}
                                        onChange={handleChange}
                                        rows="3"
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {errors.address && (
                                        <div className="invalid-feedback">
                                            {errors.address}
                                        </div>
                                    )}
                                    <small className="form-text text-muted">
                                        Adres musi mieć od 5 do 200 znaków
                                    </small>
                                </div>

                                <div className="d-grid gap-2 d-md-flex justify-content-md-end">
                                    <button
                                        type="button"
                                        className="btn btn-secondary me-md-2"
                                        onClick={() => navigate('/')}
                                        disabled={isSubmitting}
                                    >
                                        Anuluj
                                    </button>
                                    <button
                                        type="submit"
                                        className="btn btn-success"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                                Rejestracja...
                                            </>
                                        ) : 'Zarejestruj się'}
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

export default RegisterForm;