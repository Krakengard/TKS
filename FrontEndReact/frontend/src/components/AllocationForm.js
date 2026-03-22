// AllocationForm.js - zmodyfikowany
import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../services/api';

function AllocationForm() {
    const navigate = useNavigate();
    const location = useLocation();

    const [formData, setFormData] = useState({
        customerId: '',
        resourceId: '',
        startTime: '',
        endTime: ''
    });

    const [resources, setResources] = useState([]);
    const [currentUser, setCurrentUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [validationErrors, setValidationErrors] = useState({});

    useEffect(() => {
        // Pobierz zalogowanego użytkownika
        const savedUser = sessionStorage.getItem('currentUser');
        if (savedUser) {
            const user = JSON.parse(savedUser);
            setCurrentUser(user);
            setFormData(prev => ({
                ...prev,
                customerId: user.id
            }));
        }

        loadResources();
    }, []);

    const loadResources = async () => {
        try {
            const data = await api.getAllResources();
            setResources(data);
        } catch (err) {
            console.error('Error loading resources:', err);

            // Jeśli błąd 403, spróbuj bez autoryzacji lub pokaż komunikaty
            if (err.response?.status === 403) {
                setError('Brak uprawnień do przeglądania zasobów. Zaloguj się ponownie.');
            } else {
                setError('Nie udało się załadować listy zasobów');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });

        if (validationErrors[name]) {
            setValidationErrors({
                ...validationErrors,
                [name]: ''
            });
        }
    };

    const validateAllocationForm = () => {
        const errors = {};

        if (!formData.customerId) {
            errors.customerId = 'Brak ID klienta';
        }

        if (!formData.resourceId) {
            errors.resourceId = 'Proszę wybrać zasób';
        }

        if (!formData.startTime) {
            errors.startTime = 'Data rozpoczęcia jest wymagana';
        } else {
            const startTime = new Date(formData.startTime);
            const now = new Date();

            if (startTime < now) {
                errors.startTime = 'Data rozpoczęcia nie może być w przeszłości';
            }
        }

        if (formData.endTime) {
            const startTime = new Date(formData.startTime);
            const endTime = new Date(formData.endTime);

            if (endTime <= startTime) {
                errors.endTime = 'Data zakończenia musi być późniejsza niż data rozpoczęcia';
            }

            const durationDays = (endTime - startTime) / (1000 * 60 * 60 * 24);
            if (durationDays > 30) {
                errors.endTime = 'Maksymalny czas rezerwacji to 30 dni';
            }
        }

        setValidationErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setValidationErrors({});

        if (!validateAllocationForm()) {
            return;
        }

        if (!window.confirm('Czy na pewno chcesz utworzyć tę rezerwację?')) {
            return;
        }

        setIsSubmitting(true);

        try {
            const startDate = new Date(formData.startTime);
            const endDate = formData.endTime ? new Date(formData.endTime) : null;

            await api.createAllocation(
                formData.customerId,
                formData.resourceId,
                startDate,
                endDate
            );

            alert('Rezerwacja została utworzona pomyślnie!');
            navigate(`/customer/${formData.customerId}`);

        } catch (err) {
            console.error('Error creating allocation:', err);
            setError(`Błąd tworzenia rezerwacji: ${err.response?.data?.message || err.message || 'Spróbuj ponownie'}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="container mt-4 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Ładowanie...</span>
                </div>
                <p className="mt-2">Ładowanie formularza...</p>
            </div>
        );
    }

    if (!currentUser) {
        return (
            <div className="container mt-4">
                <div className="alert alert-warning">
                    Musisz być zalogowany, aby utworzyć rezerwację.
                </div>
                <button className="btn btn-primary" onClick={() => navigate('/login')}>
                    Zaloguj się
                </button>
            </div>
        );
    }

    const defaultStartTime = new Date();
    defaultStartTime.setHours(defaultStartTime.getHours() + 1);
    defaultStartTime.setMinutes(0, 0, 0);

    const defaultEndTime = new Date(defaultStartTime);
    defaultEndTime.setHours(defaultEndTime.getHours() + 2);

    const formatDateTimeLocal = (date) => {
        return date.toISOString().slice(0, 16);
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-10">
                    <div className="card">
                        <div className="card-header bg-primary text-white">
                            <h2 className="text-center">Nowa rezerwacja zasobu</h2>
                        </div>

                        <div className="card-body">
                            {/* Informacja o zalogowanym użytkowniku */}
                            <div className="alert alert-info mb-4">
                                <h5>
                                    <i className="bi bi-person me-2"></i>
                                    Rezerwujący: {currentUser.name} ({currentUser.login})
                                </h5>
                                <input type="hidden" name="customerId" value={currentUser.id} />
                            </div>

                            {error && (
                                <div className="alert alert-danger">
                                    {error}
                                </div>
                            )}

                            <form onSubmit={handleSubmit}>
                                {/* Wybór zasobu */}
                                <div className="mb-3">
                                    <label htmlFor="resourceId" className="form-label">Zasób *</label>
                                    <select
                                        className={`form-select ${validationErrors.resourceId ? 'is-invalid' : ''}`}
                                        id="resourceId"
                                        name="resourceId"
                                        value={formData.resourceId}
                                        onChange={handleChange}
                                        required
                                        disabled={isSubmitting || resources.length === 0}
                                    >
                                        <option value="">Wybierz zasób...</option>
                                        {resources.map(resource => (
                                            <option key={resource.id} value={resource.id}>
                                                {resource.name} ({resource.type}) - {resource.pricePerHour} zł/h
                                            </option>
                                        ))}
                                    </select>
                                    {validationErrors.resourceId && (
                                        <div className="invalid-feedback">
                                            {validationErrors.resourceId}
                                        </div>
                                    )}
                                    {resources.length === 0 && !error && (
                                        <small className="form-text text-warning">
                                            Brak dostępnych zasobów
                                        </small>
                                    )}
                                </div>

                                {/* Data rozpoczęcia */}
                                <div className="mb-3">
                                    <label htmlFor="startTime" className="form-label">Data i godzina rozpoczęcia *</label>
                                    <input
                                        type="datetime-local"
                                        className={`form-control ${validationErrors.startTime ? 'is-invalid' : ''}`}
                                        id="startTime"
                                        name="startTime"
                                        value={formData.startTime || formatDateTimeLocal(defaultStartTime)}
                                        onChange={handleChange}
                                        min={formatDateTimeLocal(new Date())}
                                        required
                                        disabled={isSubmitting}
                                    />
                                    {validationErrors.startTime && (
                                        <div className="invalid-feedback">
                                            {validationErrors.startTime}
                                        </div>
                                    )}
                                </div>

                                {/* Data zakończenia (opcjonalna) */}
                                <div className="mb-3">
                                    <label htmlFor="endTime" className="form-label">Data i godzina zakończenia (opcjonalnie)</label>
                                    <input
                                        type="datetime-local"
                                        className={`form-control ${validationErrors.endTime ? 'is-invalid' : ''}`}
                                        id="endTime"
                                        name="endTime"
                                        value={formData.endTime || formatDateTimeLocal(defaultEndTime)}
                                        onChange={handleChange}
                                        min={formData.startTime || formatDateTimeLocal(defaultStartTime)}
                                        disabled={isSubmitting}
                                    />
                                    {validationErrors.endTime && (
                                        <div className="invalid-feedback">
                                            {validationErrors.endTime}
                                        </div>
                                    )}
                                    <small className="form-text text-muted">
                                        Jeśli puste, rezerwacja będzie otwarta. Maksymalny czas: 30 dni
                                    </small>
                                </div>

                                <div className="d-grid gap-2 d-md-flex justify-content-md-end">
                                    <button
                                        type="button"
                                        className="btn btn-secondary me-md-2"
                                        onClick={() => navigate(`/customer/${currentUser.id}`)}
                                        disabled={isSubmitting}
                                    >
                                        Anuluj
                                    </button>
                                    <button
                                        type="submit"
                                        className="btn btn-success"
                                        disabled={isSubmitting || resources.length === 0}
                                    >
                                        {isSubmitting ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                                Tworzenie...
                                            </>
                                        ) : 'Utwórz rezerwację'}
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

export default AllocationForm;