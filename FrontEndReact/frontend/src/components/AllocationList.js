// src/components/AllocationList.js
import React, { useState, useEffect } from 'react';
import api from '../services/api';

function AllocationList() {
    const [allocations, setAllocations] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadAllocations();
    }, []);

    const loadAllocations = async () => {
        try {
            const data = await api.getAllAllocations();
            setAllocations(data);
        } catch (error) {
            console.error('Error loading allocations:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="container mt-4">Ładowanie rezerwacji...</div>;
    }

    return (
        <div className="container mt-4">
            <h2><i className="bi bi-list-task"></i> Lista rezerwacji</h2>

            <table className="table table-striped">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Klient</th>
                    <th>Zasób</th>
                    <th>Rozpoczęcie</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                {allocations.map(allocation => (
                    <tr key={allocation.id}>
                        <td>{allocation.id?.substring(0, 8)}...</td>
                        <td>{allocation.customer?.name || 'Brak'}</td>
                        <td>{allocation.resource?.name || 'Brak'}</td>
                        <td>
                            {allocation.startTime ?
                                new Date(allocation.startTime).toLocaleString() :
                                'Brak'
                            }
                        </td>
                        <td>
                            {allocation.completed ?
                                <span className="badge bg-success">Zakończona</span> :
                                <span className="badge bg-primary">Aktywna</span>
                            }
                        </td>
                    </tr>
                ))}
                {allocations.length === 0 && (
                    <tr>
                        <td colSpan="5" className="text-center">
                            Brak rezerwacji
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default AllocationList;