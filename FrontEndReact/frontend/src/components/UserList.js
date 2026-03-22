import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';

function UserList() {
    const [users, setUsers] = useState([]);
    const [filter, setFilter] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const data = await api.getUsers();
            setUsers(data);
            setError('');
        } catch (err) {
            setError('Nie udało się załadować użytkowników');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterChange = async (e) => {
        const value = e.target.value;
        setFilter(value);

        try {
            setLoading(true);
            if (value.trim()) {
                const filteredUsers = await api.searchUsers(value);
                setUsers(filteredUsers);
            } else {
                await loadUsers();
            }
        } catch (err) {
            const allUsers = await api.getUsers();
            const filtered = allUsers.filter(user =>
                user.login.toLowerCase().includes(value.toLowerCase()) ||
                user.name.toLowerCase().includes(value.toLowerCase())
            );
            setUsers(filtered);
        } finally {
            setLoading(false);
        }
    };

    const handleActivate = async (id) => {
        if (!window.confirm('Czy na pewno chcesz aktywować tego użytkownika?')) {
            return;
        }

        try {
            await api.activateUser(id);
            alert('Użytkownik został aktywowany');
            loadUsers();
        } catch (err) {
            alert('Błąd podczas aktywacji: ' + (err.message || 'Spróbuj ponownie'));
        }
    };

    const handleDeactivate = async (id) => {
        if (!window.confirm('Czy na pewno chcesz dezaktywować tego użytkownika?')) {
            return;
        }

        try {
            await api.deactivateUser(id);
            alert('Użytkownik został dezaktywowany');
            loadUsers();
        } catch (err) {
            alert('Błąd podczas dezaktywacji: ' + (err.message || 'Spróbuj ponownie'));
        }
    };

    const handleEdit = (user) => {
        navigate(`/users/edit/${user.id}`);
    };

    if (loading) {
        return <div className="container mt-4">Ładowanie użytkowników...</div>;
    }

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-people"></i> Zarządzanie użytkownikami</h2>
            </div>

            {error && (
                <div className="alert alert-danger">
                    {error}
                </div>
            )}

            {/* FILTROWANIE */}
            <div className="card mb-4">
                <div className="card-body">
                    <div className="row">
                        <div className="col-md-6">
                            <label htmlFor="filter" className="form-label">Filtruj użytkowników:</label>
                            <input
                                type="text"
                                className="form-control"
                                id="filter"
                                placeholder="Wpisz login lub imię..."
                                value={filter}
                                onChange={handleFilterChange}
                            />
                            <small className="form-text text-muted">
                                Filtruj po loginie lub imieniu i nazwisku
                            </small>
                        </div>
                        <div className="col-md-6">
                            <div className="mt-4">
                                <span className="badge bg-primary me-2">
                                    Administratorzy: {users.filter(u => u.type === 'administrator').length}
                                </span>
                                <span className="badge bg-info me-2">
                                    Managerowie: {users.filter(u => u.type === 'resourceManager').length}
                                </span>
                                <span className="badge bg-success">
                                    Klienci: {users.filter(u => u.type === 'customer').length}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* TABELA UŻYTKOWNIKÓW */}
            <div className="table-responsive">
                <table className="table table-striped table-hover">
                    <thead className="table-dark">
                    <tr>
                        <th>Login</th>
                        <th>Imię i nazwisko</th>
                        <th>Email</th>
                        <th>Typ</th>
                        <th>Status</th>
                        <th>Akcje</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>
                                <strong>{user.login}</strong><br />
                                <small className="text-muted">ID: {user.id?.substring(0, 8)}...</small>
                            </td>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>
                                {user.type === 'administrator' ?
                                    <span className="badge bg-primary">Administrator</span> :
                                    user.type === 'resourceManager' ?
                                        <span className="badge bg-info">Manager zasobów</span> :
                                        <span className="badge bg-success">Klient</span>
                                }
                            </td>
                            <td>
                                {user.active ?
                                    <span className="badge bg-success">
                                            <i className="bi bi-check-circle"></i> Aktywny
                                        </span> :
                                    <span className="badge bg-danger">
                                            <i className="bi bi-x-circle"></i> Nieaktywny
                                        </span>
                                }
                            </td>
                            <td>
                                <div className="btn-group btn-group-sm" role="group">
                                    {/* PRZYCISKI AKTYWACJI/DEZAKTYWACJI */}
                                    <button
                                        className="btn btn-outline-success"
                                        onClick={() => handleActivate(user.id)}
                                        disabled={user.active}
                                        title="Aktywuj użytkownika"
                                    >
                                        <i className="bi bi-check-lg"></i> Aktywuj
                                    </button>

                                    <button
                                        className="btn btn-outline-warning"
                                        onClick={() => handleDeactivate(user.id)}
                                        disabled={!user.active}
                                        title="Dezaktywuj użytkownika"
                                    >
                                        <i className="bi bi-x-lg"></i> Dezaktywuj
                                    </button>

                                    {/* PRZYCISK EDYCJI */}
                                    <button
                                        className="btn btn-outline-info"
                                        onClick={() => handleEdit(user)}
                                        title="Edytuj użytkownika"
                                    >
                                        <i className="bi bi-pencil"></i> Edytuj
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}

                    {users.length === 0 && (
                        <tr>
                            <td colSpan="6" className="text-center text-muted py-4">
                                <i className="bi bi-people display-4"></i><br />
                                {filter ? 'Brak użytkowników spełniających kryteria' : 'Brak użytkowników'}
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* LEGENDA */}
            <div className="card mt-4">
                <div className="card-body">
                    <h5 className="card-title"><i className="bi bi-info-circle"></i> Legenda akcji:</h5>
                    <div className="row">
                        <div className="col-md-4">
                            <button className="btn btn-outline-success btn-sm" disabled>
                                <i className="bi bi-check-lg"></i> Aktywuj
                            </button>
                            <span className="ms-2">- przywróć dostęp do konta</span>
                        </div>
                        <div className="col-md-4">
                            <button className="btn btn-outline-warning btn-sm" disabled>
                                <i className="bi bi-x-lg"></i> Dezaktywuj
                            </button>
                            <span className="ms-2">- zablokuj dostęp do konta</span>
                        </div>
                        <div className="col-md-4">
                            <button className="btn btn-outline-info btn-sm" disabled>
                                <i className="bi bi-pencil"></i> Edytuj
                            </button>
                            <span className="ms-2">- zmień dane użytkownika</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default UserList;