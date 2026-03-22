import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { CustomValidationUtils } from '../utils/validation';

function UserEditForm() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        login: '',
        name: '',
        email: '',
        type: 'customer',
        active: true,
        phoneNumber: '',
        address: '',
        department: '',
        managedResourceType: ''
    });

    const [originalUser, setOriginalUser] = useState(null);
    const [objectSignature, setObjectSignature] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        if (id) {
            loadUserData();
        }
    }, [id]);

    const loadUserData = async () => {
        try {
            setLoading(true);
            const response = await api.getUserById(id);

            if (!response || !response.user) {
                setErrorMessage('Użytkownik nie istnieje');
                return;
            }

            const user = response.user;
            setOriginalUser(user);
            setObjectSignature(response.signature || '');

            const formData = {
                login: user.login || '',
                name: user.name || '',
                email: user.email || '',
                type: user.type || 'customer',
                active: user.active !== undefined ? user.active : true,
                phoneNumber: user.phoneNumber || '',
                address: user.address || '',
                department: user.department || '',
                managedResourceType: user.managedResourceType || ''
            };

            setFormData(formData);

        } catch (err) {
            console.error('Error loading user:', err);
            setErrorMessage('Nie udało się załadować danych użytkownika');
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === 'checkbox' ? checked : value
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

        if (!formData.login.trim()) {
            newErrors.login = 'Login jest wymagany';
        } else if (formData.login !== originalUser?.login) {
            if (!CustomValidationUtils.isValidLogin(formData.login)) {
                newErrors.login = 'Login musi mieć 3-50 znaków (tylko litery, cyfry, podkreślniki)';
            } else if (CustomValidationUtils.hasDangerousCharacters(formData.login)) {
                newErrors.login = 'Login zawiera niedozwolone znaki';
            } else if (CustomValidationUtils.isReservedLogin(formData.login)) {
                newErrors.login = 'Login jest zarezerwowany';
            }
        }

        if (!formData.name.trim()) {
            newErrors.name = 'Imię i nazwisko jest wymagane';
        } else if (!CustomValidationUtils.isValidName(formData.name)) {
            newErrors.name = 'Imię i nazwisko musi mieć 2-100 znaków';
        }

        if (!formData.email.trim()) {
            newErrors.email = 'Email jest wymagany';
        } else if (!CustomValidationUtils.isValidEmail(formData.email)) {
            newErrors.email = 'Nieprawidłowy format email';
        }

        // Walidacja specyficzna dla typu użytkownika
        if (formData.type === 'customer') {
            if (!formData.phoneNumber.trim()) {
                newErrors.phoneNumber = 'Numer telefonu jest wymagany dla klienta';
            } else if (!CustomValidationUtils.isValidPhoneNumber(formData.phoneNumber)) {
                newErrors.phoneNumber = 'Nieprawidłowy format numeru telefonu';
            }

            if (!formData.address.trim()) {
                newErrors.address = 'Adres jest wymagany dla klienta';
            } else if (!CustomValidationUtils.isValidAddress(formData.address)) {
                newErrors.address = 'Adres musi mieć 5-200 znaków';
            }
        }

        if (formData.type === 'administrator' && !formData.department.trim()) {
            newErrors.department = 'Departament jest wymagany dla administratora';
        }

        if (formData.type === 'resourceManager' && !formData.managedResourceType.trim()) {
            newErrors.managedResourceType = 'Typ zarządzanych zasobów jest wymagany';
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

        if (!window.confirm('Czy na pewno chcesz zapisać zmiany?')) {
            return;
        }

        setIsSubmitting(true);

        try {
            const userToUpdate = {
                id: id,
                login: formData.login,
                name: formData.name,
                email: formData.email,
                type: formData.type,
                active: formData.active
            };

            if (formData.type === 'customer') {
                userToUpdate.phoneNumber = formData.phoneNumber;
                userToUpdate.address = formData.address;
            } else if (formData.type === 'administrator') {
                userToUpdate.department = formData.department;
            } else if (formData.type === 'resourceManager') {
                userToUpdate.managedResourceType = formData.managedResourceType;
            }

            const response = await api.updateUser(id, userToUpdate, objectSignature);

            alert('Dane użytkownika zostały zaktualizowane pomyślnie!');
            navigate('/users');

        } catch (err) {
            console.error('Error updating user:', err);
            setErrorMessage(`Błąd aktualizacji: ${err.response?.data || 'Spróbuj ponownie'}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    const renderTypeSpecificFields = () => {
        switch (formData.type) {
            case 'customer':
                return (
                    <>
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
                                <div className="invalid-feedback">{errors.phoneNumber}</div>
                            )}
                        </div>

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
                                <div className="invalid-feedback">{errors.address}</div>
                            )}
                        </div>
                    </>
                );

            case 'administrator':
                return (
                    <div className="mb-3">
                        <label htmlFor="department" className="form-label">Departament *</label>
                        <input
                            type="text"
                            className={`form-control ${errors.department ? 'is-invalid' : ''}`}
                            id="department"
                            name="department"
                            value={formData.department}
                            onChange={handleChange}
                            required
                            disabled={isSubmitting}
                        />
                        {errors.department && (
                            <div className="invalid-feedback">{errors.department}</div>
                        )}
                    </div>
                );

            case 'resourceManager':
                return (
                    <div className="mb-3">
                        <label htmlFor="managedResourceType" className="form-label">Typ zarządzanych zasobów *</label>
                        <input
                            type="text"
                            className={`form-control ${errors.managedResourceType ? 'is-invalid' : ''}`}
                            id="managedResourceType"
                            name="managedResourceType"
                            value={formData.managedResourceType}
                            onChange={handleChange}
                            required
                            disabled={isSubmitting}
                            placeholder="np. AV Equipment, Computers, Rooms"
                        />
                        {errors.managedResourceType && (
                            <div className="invalid-feedback">{errors.managedResourceType}</div>
                        )}
                    </div>
                );

            default:
                return null;
        }
    };

    if (loading) {
        return (
            <div className="container mt-4 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Ładowanie...</span>
                </div>
                <p className="mt-2">Ładowanie danych użytkownika...</p>
            </div>
        );
    }

    if (errorMessage && !originalUser) {
        return (
            <div className="container mt-4">
                <div className="alert alert-danger">
                    {errorMessage}
                </div>
                <button className="btn btn-primary" onClick={() => navigate('/users')}>
                    Wróć do listy użytkowników
                </button>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card">
                        <div className="card-header bg-info text-white">
                            <h2 className="text-center">
                                <i className="bi bi-pencil-square me-2"></i>
                                Edycja użytkownika
                            </h2>
                        </div>

                        <div className="card-body">
                            {errorMessage && (
                                <div className="alert alert-danger">
                                    {errorMessage}
                                </div>
                            )}

                            {objectSignature && (
                                <div className="alert alert-info">
                                    <i className="bi bi-shield-check me-2"></i>
                                    Obiekt zabezpieczony podpisem JWS
                                </div>
                            )}

                            <form onSubmit={handleSubmit}>
                                {/* PODSTAWOWE INFORMACJE */}
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
                                        <div className="invalid-feedback">{errors.login}</div>
                                    )}
                                </div>

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
                                        <div className="invalid-feedback">{errors.name}</div>
                                    )}
                                </div>

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
                                        <div className="invalid-feedback">{errors.email}</div>
                                    )}
                                </div>

                                {/* TYP UŻYTKOWNIKA */}
                                <div className="mb-3">
                                    <label htmlFor="type" className="form-label">Typ użytkownika *</label>
                                    <select
                                        className="form-select"
                                        id="type"
                                        name="type"
                                        value={formData.type}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting}
                                    >
                                        <option value="customer">Klient</option>
                                        <option value="administrator">Administrator</option>
                                        <option value="resourceManager">Manager zasobów</option>
                                    </select>
                                </div>

                                {/* POLA SPECYFICZNE DLA TYPU */}
                                {renderTypeSpecificFields()}

                                {/* STATUS */}
                                <div className="mb-3 form-check">
                                    <input
                                        type="checkbox"
                                        className="form-check-input"
                                        id="active"
                                        name="active"
                                        checked={formData.active}
                                        onChange={handleChange}
                                        disabled={isSubmitting}
                                    />
                                    <label className="form-check-label" htmlFor="active">
                                        Konto aktywne
                                    </label>
                                </div>

                                <div className="d-grid gap-2 d-md-flex justify-content-md-end">
                                    <button
                                        type="button"
                                        className="btn btn-secondary me-md-2"
                                        onClick={() => navigate('/users')}
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
                                                Zapisuję...
                                            </>
                                        ) : 'Zapisz zmiany'}
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

export default UserEditForm;