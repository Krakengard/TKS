// src/components/ResourceManagement.js
import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';

function ResourceManagement() {
    const [resources, setResources] = useState([]);
    const [newResource, setNewResource] = useState({
        name: '',
        description: '',
        type: '',
        pricePerHour: ''
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        loadResources();
    }, []);

    const loadResources = async () => {
        try {
            const data = await api.getAllResources();
            setResources(data);
        } catch (err) {
            setError('Nie udało się załadować zasobów');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const resourceToCreate = {
                ...newResource,
                pricePerHour: parseFloat(newResource.pricePerHour)
            };
            await api.createResource(resourceToCreate);
            alert('Zasób dodany pomyślnie');
            setNewResource({ name: '', description: '', type: '', pricePerHour: '' });
            loadResources();
        } catch (err) {
            alert('Błąd podczas dodawania zasobu: ' + (err.response?.data || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Czy na pewno chcesz usunąć ten zasób?')) {
            try {
                await api.deleteResource(id);
                alert('Zasób usunięty');
                loadResources();
            } catch (err) {
                alert('Błąd: ' + (err.response?.data || 'Nie można usunąć zasobu z aktywnymi rezerwacjami'));
            }
        }
    };

    if (loading) {
        return (
            <div className="container mt-4 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Ładowanie...</span>
                </div>
                <p className="mt-2">Ładowanie zasobów...</p>
            </div>
        );
    }

    return (
        <div className="container mt-4">
            <h2><i className="bi bi-pc-display me-2"></i>Zarządzanie zasobami</h2>

            {error && (
                <div className="alert alert-danger">
                    {error}
                </div>
            )}

            {/* Formularz dodawania */}
            <div className="card mb-4">
                <div className="card-header bg-primary text-white">
                    <h5 className="mb-0"><i className="bi bi-plus-circle me-2"></i>Dodaj nowy zasób</h5>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="row mb-3">
                            <div className="col-md-4">
                                <label className="form-label">Nazwa *</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Nazwa zasobu"
                                    value={newResource.name}
                                    onChange={e => setNewResource({...newResource, name: e.target.value})}
                                    required
                                />
                            </div>
                            <div className="col-md-3">
                                <label className="form-label">Typ *</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="np. AV Equipment, Computer, Room"
                                    value={newResource.type}
                                    onChange={e => setNewResource({...newResource, type: e.target.value})}
                                    required
                                />
                            </div>
                            <div className="col-md-3">
                                <label className="form-label">Cena za godzinę (zł) *</label>
                                <input
                                    type="number"
                                    className="form-control"
                                    placeholder="0.00"
                                    step="0.01"
                                    min="0"
                                    value={newResource.pricePerHour}
                                    onChange={e => setNewResource({...newResource, pricePerHour: e.target.value})}
                                    required
                                />
                            </div>
                            <div className="col-md-2 d-flex align-items-end">
                                <button type="submit" className="btn btn-primary w-100">
                                    <i className="bi bi-plus-lg me-1"></i> Dodaj
                                </button>
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-md-12">
                                <label className="form-label">Opis</label>
                                <textarea
                                    className="form-control"
                                    placeholder="Opis zasobu..."
                                    rows="2"
                                    value={newResource.description}
                                    onChange={e => setNewResource({...newResource, description: e.target.value})}
                                />
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            {/* Statystyki */}
            <div className="row mb-4">
                <div className="col-md-4">
                    <div className="card text-center">
                        <div className="card-body">
                            <h5 className="card-title text-primary">Łączna liczba</h5>
                            <h2 className="text-primary">{resources.length}</h2>
                            <p className="card-text">dostępnych zasobów</p>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card text-center">
                        <div className="card-body">
                            <h5 className="card-title text-info">Różne typy</h5>
                            <h2 className="text-info">
                                {[...new Set(resources.map(r => r.type))].length}
                            </h2>
                            <p className="card-text">unikalnych kategorii</p>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card text-center">
                        <div className="card-body">
                            <h5 className="card-title text-success">Średnia cena</h5>
                            <h2 className="text-success">
                                {resources.length > 0
                                    ? (resources.reduce((sum, r) => sum + r.pricePerHour, 0) / resources.length).toFixed(2)
                                    : '0.00'
                                } zł/h
                            </h2>
                            <p className="card-text">za godzinę</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Lista zasobów */}
            <div className="card">
                <div className="card-header bg-secondary text-white">
                    <h5 className="mb-0"><i className="bi bi-list-ul me-2"></i>Lista zasobów</h5>
                </div>
                <div className="card-body">
                    {resources.length === 0 ? (
                        <div className="text-center py-4">
                            <i className="bi bi-inbox display-4 text-muted"></i>
                            <h4 className="mt-3">Brak zasobów</h4>
                            <p>Dodaj pierwszy zasób używając formularza powyżej</p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-striped table-hover">
                                <thead>
                                <tr>
                                    <th>Nazwa</th>
                                    <th>Typ</th>
                                    <th>Opis</th>
                                    <th>Cena/h</th>
                                    <th>Akcje</th>
                                </tr>
                                </thead>
                                <tbody>
                                {resources.map(resource => (
                                    <tr key={resource.id}>
                                        <td>
                                            <strong>{resource.name}</strong>
                                        </td>
                                        <td>
                                            <span className="badge bg-info">{resource.type}</span>
                                        </td>
                                        <td>{resource.description}</td>
                                        <td>
                                            <strong className="text-success">{resource.pricePerHour} zł</strong>
                                        </td>
                                        <td>
                                            <button
                                                className="btn btn-sm btn-danger"
                                                onClick={() => handleDelete(resource.id)}
                                                title="Usuń zasób"
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
        </div>
    );
}

export default ResourceManagement;