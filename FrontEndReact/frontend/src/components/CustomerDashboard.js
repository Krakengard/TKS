import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';

function CustomerDashboard() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [customer, setCustomer] = useState(null);
    const [allocations, setAllocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const savedUser = sessionStorage.getItem('currentUser');
        if (savedUser) {
            const user = JSON.parse(savedUser);

            // Jeśli id nie jest podane lub to ten sam użytkownik
            if (!id || user.id === id) {
                setCustomer(user);
                loadAllocations(user.id);
            } else {
                // Próba dostępu do cudzego panelu - przekieruj do własnego
                navigate(`/customer/${user.id}`);
            }
        } else {
            navigate('/login');
        }
    }, [id, navigate]);

    // NOWA FUNKCJA - brakowała jej
    const loadAllocations = async (customerId) => {
        try {
            setLoading(true);
            const allAllocations = await api.getAllAllocations();

            // Filtruj alokacje dla danego klienta
            const customerAllocations = allAllocations.filter(
                allocation => allocation.customer && allocation.customer.id === customerId
            );

            setAllocations(customerAllocations);
        } catch (err) {
            console.error('Error loading allocations:', err);
            setError('Nie udało się załadować rezerwacji');
        } finally {
            setLoading(false);
        }
    };

    // Usuń tę starą funkcję (loadCustomerData) lub zaktualizuj ją:
    const refreshData = () => {
        if (customer) {
            loadAllocations(customer.id);
        }
    };

    const handleLogout = () => {
        sessionStorage.removeItem('jwtToken');
        sessionStorage.removeItem('currentUser');
        navigate('/');
    };

    const handleCreateAllocation = () => {
        navigate(`/allocations/new?customerId=${customer.id}`);
    };

    if (loading) {
        return (
            <div className="container mt-4 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Ładowanie...</span>
                </div>
                <p className="mt-2">Ładowanie panelu klienta...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mt-4">
                <div className="alert alert-danger">
                    {error}
                </div>
                <button className="btn btn-primary" onClick={() => navigate('/')}>
                    Wróć do strony głównej
                </button>
            </div>
        );
    }

    if (!customer) {
        return (
            <div className="container mt-4">
                <div className="alert alert-warning">
                    Nie znaleziono danych klienta
                </div>
                <button className="btn btn-primary" onClick={() => navigate('/')}>
                    Wróć do strony głównej
                </button>
            </div>
        );
    }

    const now = new Date();
    const currentAllocations = allocations.filter(allocation =>
        !allocation.completed &&
        (!allocation.endTime || new Date(allocation.endTime) > now)
    );

    const pastAllocations = allocations.filter(allocation =>
        allocation.completed ||
        (allocation.endTime && new Date(allocation.endTime) <= now)
    );

    return (
        <div className="container mt-4">
            {/* Nagłówek */}
            <div className="card bg-primary text-white mb-4">
                <div className="card-body">
                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <h1 className="card-title">
                                <i className="bi bi-person-badge me-2"></i>
                                Panel Klienta
                            </h1>
                            <p className="card-text">
                                Witaj <strong>{customer.name}</strong>! Tutaj możesz zarządzać swoimi rezerwacjami.
                            </p>
                        </div>
                        <button className="btn btn-light" onClick={handleLogout}>
                            <i className="bi bi-box-arrow-right me-1"></i>
                            Wyloguj
                        </button>
                    </div>
                </div>
            </div>

            {/* Szybkie statystyki */}
            <div className="row mb-4">
                <div className="col-md-4">
                    <div className="card text-center">
                        <div className="card-body">
                            <h5 className="card-title text-primary">
                                <i className="bi bi-plus-circle"></i> Nowa rezerwacja
                            </h5>
                            <p className="card-text">Utwórz nową rezerwację zasobu</p>
                            <button className="btn btn-primary" onClick={handleCreateAllocation}>
                                <i className="bi bi-plus-lg me-1"></i> Utwórz
                            </button>
                        </div>
                    </div>
                </div>

                <div className="col-md-4">
                    <div className="card text-center">
                        <div className="card-body">
                            <h5 className="card-title text-warning">
                                <i className="bi bi-clock-history"></i> Bieżące rezerwacje
                            </h5>
                            <h2 className="text-warning">{currentAllocations.length}</h2>
                        </div>
                    </div>
                </div>

                <div className="col-md-4">
                    <div className="card text-center">
                        <div className="card-body">
                            <h5 className="card-title text-secondary">
                                <i className="bi bi-archive"></i> Historia
                            </h5>
                            <h2 className="text-secondary">{pastAllocations.length}</h2>
                        </div>
                    </div>
                </div>
            </div>

            {/* Bieżące rezerwacje */}
            <div className="card mb-4">
                <div className="card-header bg-primary text-white">
                    <h3 className="card-title">
                        <i className="bi bi-play-circle me-2"></i>
                        Bieżące rezerwacje
                    </h3>
                </div>
                <div className="card-body">
                    {currentAllocations.length === 0 ? (
                        <div className="text-center py-4">
                            <i className="bi bi-inbox display-4 text-muted"></i>
                            <h4 className="mt-3">Brak bieżących rezerwacji</h4>
                            <p>Utwórz swoją pierwszą rezerwację!</p>
                            <button className="btn btn-primary" onClick={handleCreateAllocation}>
                                <i className="bi bi-plus-lg me-1"></i> Utwórz rezerwację
                            </button>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-striped">
                                <thead>
                                <tr>
                                    <th>Zasób</th>
                                    <th>Rozpoczęcie</th>
                                    <th>Zakończenie</th>
                                    <th>Akcje</th>
                                </tr>
                                </thead>
                                <tbody>
                                {currentAllocations.map(allocation => (
                                    <tr key={allocation.id}>
                                        <td>
                                            <strong>{allocation.resource?.name || 'Brak'}</strong><br />
                                            <small className="text-muted">{allocation.resource?.type || ''}</small>
                                        </td>
                                        <td>
                                            {allocation.startTime ?
                                                new Date(allocation.startTime).toLocaleString('pl-PL') :
                                                'Brak'
                                            }
                                        </td>
                                        <td>
                                            {allocation.endTime ?
                                                new Date(allocation.endTime).toLocaleString('pl-PL') :
                                                <span className="text-warning">Otwarta</span>
                                            }
                                        </td>
                                        <td>
                                            <button
                                                className="btn btn-sm btn-outline-success me-1"
                                                onClick={async () => {
                                                    if (window.confirm('Czy na pewno chcesz zakończyć tę rezerwację?')) {
                                                        try {
                                                            await api.completeAllocation(allocation.id);
                                                            alert('Rezerwacja zakończona pomyślnie!');
                                                            refreshData(); // Odśwież dane
                                                        } catch (error) {
                                                            alert('Błąd podczas kończenia rezerwacji: ' + error.message);
                                                        }
                                                    }
                                                }}
                                            >
                                                <i className="bi bi-check-lg"></i> Zakończ
                                            </button>
                                            <button
                                                className="btn btn-sm btn-outline-danger"
                                                onClick={async () => {
                                                    if (window.confirm('Czy na pewno chcesz usunąć tę rezerwację?')) {
                                                        try {
                                                            await api.deleteAllocation(allocation.id);
                                                            alert('Rezerwacja usunięta pomyślnie!');
                                                            refreshData(); // Odśwież dane
                                                        } catch (error) {
                                                            alert('Błąd podczas usuwania rezerwacji: ' + error.message);
                                                        }
                                                    }
                                                }}
                                            >
                                                <i className="bi bi-trash"></i> Usuń
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Historia rezerwacji */}
            <div className="card">
                <div className="card-header bg-secondary text-white">
                    <h3 className="card-title">
                        <i className="bi bi-archive me-2"></i>
                        Historia rezerwacji
                    </h3>
                </div>
                <div className="card-body">
                    {pastAllocations.length === 0 ? (
                        <div className="text-center py-4">
                            <i className="bi bi-clock-history display-4 text-muted"></i>
                            <h4 className="mt-3">Brak historii rezerwacji</h4>
                            <p>Twoje zakończone rezerwacje pojawią się tutaj.</p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-striped">
                                <thead>
                                <tr>
                                    <th>Zasób</th>
                                    <th>Rozpoczęcie</th>
                                    <th>Zakończenie</th>
                                    <th>Status</th>
                                </tr>
                                </thead>
                                <tbody>
                                {pastAllocations.map(allocation => (
                                    <tr key={allocation.id}>
                                        <td>
                                            <strong>{allocation.resource?.name || 'Brak'}</strong><br />
                                            <small className="text-muted">{allocation.resource?.type || ''}</small>
                                        </td>
                                        <td>
                                            {allocation.startTime ?
                                                new Date(allocation.startTime).toLocaleString('pl-PL') :
                                                'Brak'
                                            }
                                        </td>
                                        <td>
                                            {allocation.endTime ?
                                                new Date(allocation.endTime).toLocaleString('pl-PL') :
                                                <span className="text-warning">Otwarta</span>
                                            }
                                        </td>
                                        <td>
                                            {allocation.completed ?
                                                <span className="badge bg-success">Zakończona</span> :
                                                <span className="badge bg-warning text-dark">Wygasła</span>
                                            }
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Informacje o koncie */}
            <div className="card mt-4">
                <div className="card-header">
                    <h3 className="card-title">
                        <i className="bi bi-info-circle me-2"></i>
                        Informacje o koncie
                    </h3>
                </div>
                <div className="card-body">
                    <div className="row">
                        <div className="col-md-6">
                            <p><strong>Login:</strong> {customer.login}</p>
                            <p><strong>Imię i nazwisko:</strong> {customer.name}</p>
                            <p><strong>Email:</strong> {customer.email}</p>
                        </div>
                        <div className="col-md-6">
                            <p><strong>Telefon:</strong> {customer.phoneNumber}</p>
                            <p><strong>Adres:</strong> {customer.address}</p>
                            <p><strong>Status konta:</strong>
                                {customer.active ?
                                    <span className="badge bg-success ms-2">Aktywne</span> :
                                    <span className="badge bg-danger ms-2">Nieaktywne</span>
                                }
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CustomerDashboard;