// src/services/api.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

class RentalApiService {
    constructor() {
        this.api = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Content-Type': 'application/json',
            },
        });

        this.api.interceptors.request.use(
            config => {
                const token = sessionStorage.getItem('jwtToken');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            error => {
                return Promise.reject(error);
            }
        );

        this.api.interceptors.response.use(
            response => response,
            error => {
                if (error.response?.status === 401) {
                    sessionStorage.removeItem('jwtToken');
                    sessionStorage.removeItem('currentUser');
                    if (window.location.pathname !== '/login') {
                        window.location.href = '/login?session=expired';
                    }
                }
                return Promise.reject(error);
            }
        );
    }

    async getVerificationToken(userId) {
        try {
            const response = await this.api.get(`/users/${userId}/verification-token`);
            return response.data.verificationToken;
        } catch (error) {
            console.error(`Error getting verification token for user ${userId}:`, error);
            throw error;
        }
    }

    // ========== AUTH OPERATIONS ==========
    async login(login, password) {
        try {
            const response = await this.api.post('/auth/login', { login, password });
            if (response.data.token) {
                sessionStorage.setItem('jwtToken', response.data.token);
                sessionStorage.setItem('currentUser', JSON.stringify(response.data.user));
            }
            return response.data;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    }

    async logout() {
        try {
            await this.api.post('/auth/logout');
        } finally {
            sessionStorage.removeItem('jwtToken');
            sessionStorage.removeItem('currentUser');
        }
    }

    async changePassword(currentPassword, newPassword) {
        try {
            const response = await this.api.post('/auth/change-password', {
                currentPassword,
                newPassword
            });
            return response.data;
        } catch (error) {
            console.error('Change password error:', error);
            throw error;
        }
    }

    async getCurrentUser() {
        try {
            const response = await this.api.get('/auth/me');
            return response.data;
        } catch (error) {
            console.error('Get current user error:', error);
            throw error;
        }
    }

    // ========== USER OPERATIONS ==========
    async getUsers() {
        try {
            const response = await this.api.get('/users/search?loginPart=');
            return response.data;
        } catch (error) {
            console.error('Error fetching users:', error);
            return [];
        }
    }

    async getUserById(id, signature = null, verificationToken = null) {
        try {
            const config = {};
            if (signature) {
                config.headers = { ...config.headers, 'X-Object-Signature': signature };
            }
            if (verificationToken) {
                config.headers = { ...config.headers, 'X-Verification-Token': verificationToken };
            }

            const response = await this.api.get(`/users/${id}`, config);
            return response.data;
        } catch (error) {
            console.error(`Error fetching user ${id}:`, error);
            return null;
        }
    }

    async searchUsers(loginPart) {
        try {
            const response = await this.api.get(`/users/search?loginPart=${encodeURIComponent(loginPart)}`);
            return response.data;
        } catch (error) {
            console.error('Error searching users:', error);
            return [];
        }
    }

    async getUserByLogin(login) {
        try {
            const response = await this.api.get(`/users/search/exact?login=${login}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching user by login ${login}:`, error);
            return null;
        }
    }
    

    async createUser(user) {
        try {
            const response = await this.api.post('/users/register', user);
            return response.data;
        } catch (error) {
            console.error('Error creating user:', error);
            throw new Error(error.response?.data || 'Failed to create user');
        }
    }

    async updateUser(id, user, signature = null, verificationToken = null) {
        try {
            const config = {};
            if (signature) {
                config.headers = { ...config.headers, 'X-Object-Signature': signature };
            }
            if (verificationToken) {
                config.headers = { ...config.headers, 'X-Verification-Token': verificationToken };
            }

            const response = await this.api.put(`/users/${id}`, user, config);
            return response.data;
        } catch (error) {
            console.error(`Error updating user ${id}:`, error);
            throw error;
        }
    }

    async activateUser(id) {
        try {
            await this.api.post(`/users/${id}/activate`);
        } catch (error) {
            console.error(`Error activating user ${id}:`, error);
            throw error;
        }
    }

    async deactivateUser(id) {
        try {
            await this.api.post(`/users/${id}/deactivate`);
        } catch (error) {
            console.error(`Error deactivating user ${id}:`, error);
            throw error;
        }
    }

    // ========== ALLOCATION OPERATIONS ==========
    async getAllAllocations() {
        try {
            const response = await this.api.get('/allocations');
            return response.data;
        } catch (error) {
            console.error('Error fetching allocations:', error);
            return [];
        }
    }

    async createAllocation(customerId, resourceId, startTime, endTime) {
        try {
            const params = new URLSearchParams({
                customerId: customerId,
                resourceId: resourceId,
                startTime: startTime.toISOString()
            });
            if (endTime) {
                params.append('endTime', endTime.toISOString());
            }
            const response = await this.api.post(`/allocations?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error('Error creating allocation:', error);
            throw new Error(error.response?.data || 'Failed to create allocation');
        }
    }

    async completeAllocation(id) {
        try {
            await this.api.post(`/allocations/${id}/complete`);
        } catch (error) {
            console.error(`Error completing allocation ${id}:`, error);
            throw error;
        }
    }

    async deleteAllocation(id) {
        try {
            await this.api.delete(`/allocations/${id}`);
        } catch (error) {
            console.error(`Error deleting allocation ${id}:`, error);
            throw error;
        }
    }

    async getAllocationById(id) {
        try {
            const response = await this.api.get(`/allocations/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching allocation ${id}:`, error);
            return null;
        }
    }

    // ========== RESOURCE OPERATIONS ==========
    async getAllResources() {
        try {
            const response = await this.api.get('/resources');
            return response.data;
        } catch (error) {
            console.error('Error fetching resources:', error);
            return [];
        }
    }

    async getResourceById(id) {
        try {
            const response = await this.api.get(`/resources/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching resource ${id}:`, error);
            return null;
        }
    }

    async createResource(resource) {
        try {
            const response = await this.api.post('/resources', resource);
            return response.data;
        } catch (error) {
            console.error('Error creating resource:', error);
            throw error;
        }
    }

    async updateResource(id, resource) {
        try {
            const response = await this.api.put(`/resources/${id}`, resource);
            return response.data;
        } catch (error) {
            console.error(`Error updating resource ${id}:`, error);
            throw error;
        }
    }

    async deleteResource(id) {
        try {
            await this.api.delete(`/resources/${id}`);
        } catch (error) {
            console.error(`Error deleting resource ${id}:`, error);
            throw error;
        }
    }

    // Helper methods
    isAuthenticated() {
        return !!sessionStorage.getItem('jwtToken');
    }

    getUserRole() {
        const userStr = sessionStorage.getItem('currentUser');
        if (!userStr) return null;
        try {
            const user = JSON.parse(userStr);
            if (user.type === 'administrator') return 'ROLE_ADMIN';
            if (user.type === 'resourceManager') return 'ROLE_MANAGER';
            if (user.type === 'customer') return 'ROLE_CUSTOMER';
            return null;
        } catch (e) {
            return null;
        }
    }

    // Zwykły użytkownik pobiera tylko klientów
    async getCustomers() {
        try {
            const response = await this.api.get('/users/customers');
            return response.data;
        } catch (error) {
            console.error('Error fetching customers:', error);
            return [];
        }
    };

    async getAvailableResources() {
        try {
            const response = await this.api.get('/resources/available');
            return response.data;
        } catch (error) {
            console.error('Error fetching available resources:', error);
            return [];
        }
    };

    hasRole(role) {
        const userRole = this.getUserRole();
        if (!userRole) return false;
        return userRole === role;
    }
}

const apiService = new RentalApiService();
export default apiService;