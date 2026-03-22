import React from 'react';
import { Link } from 'react-router-dom';

function HomePage() {
    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card">
                        <div className="card-header bg-primary text-white">
                            <h2 className="text-center">System Rezerwacji Zasobów</h2>
                        </div>
                        <div className="card-body text-center">
                            <div className="mb-4">
                                <h4>Witamy w systemie rezerwacji zasobów</h4>
                                <p className="text-muted">
                                    Zarejestruj się jako klient lub zaloguj, aby dokonać rezerwacji
                                </p>
                            </div>

                            <div className="d-grid gap-3 col-md-6 mx-auto">
                                <Link to="/register" className="btn btn-success btn-lg">
                                    Zarejestruj się jako nowy klient
                                </Link>
                                <Link to="/login" className="btn btn-primary btn-lg">
                                    Zaloguj się (istniejący klient)
                                </Link>
                                <Link to="/allocations" className="btn btn-info btn-lg">
                                    Przeglądaj wszystkie rezerwacje
                                </Link>
                                <Link to="/users" className="btn btn-secondary btn-lg">
                                    Zarządzaj użytkownikami
                                </Link>
                            </div>

                            <div className="mt-5">
                                <h5>Dostępne funkcje:</h5>
                                <ul className="list-group list-group-flush">
                                    <li className="list-group-item">✅ Rejestracja nowego klienta</li>
                                    <li className="list-group-item">✅ Logowanie istniejącego klienta</li>
                                    <li className="list-group-item">✅ Rezerwacja zasobów</li>
                                    <li className="list-group-item">✅ Przeglądanie wszystkich rezerwacji</li>
                                    <li className="list-group-item">✅ Kończenie rezerwacji</li>
                                    <li className="list-group-item">✅ Usuwanie rezerwacji</li>
                                    <li className="list-group-item">✅ Zarządzanie użytkownikami</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default HomePage;