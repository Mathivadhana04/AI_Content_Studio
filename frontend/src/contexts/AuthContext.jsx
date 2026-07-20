import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi, userApi } from '../api';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Initialize Auth State
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        try {
          const res = await userApi.getMe();
          setUser(res.data);
        } catch (err) {
          console.error('Failed to load user profile', err);
          // Token might be expired, let interceptor handle or clear
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await authApi.login({ email, password });
      const { accessToken, refreshToken, user: userData } = res.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      setUser(userData);
      toast.success('Successfully logged in!');
      return userData;
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Login failed. Please check credentials.';
      toast.error(errMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (firstName, lastName, email, password) => {
    setLoading(true);
    try {
      const res = await authApi.register({ firstName, lastName, email, password });
      const { accessToken, refreshToken, user: userData } = res.data;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      setUser(userData);
      toast.success('Registration successful!');
      return userData;
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Registration failed. Try again.';
      toast.error(errMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    const refreshToken = localStorage.getItem('refreshToken');
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch (err) {
      console.warn('Backend logout failed', err);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setUser(null);
      setLoading(false);
      toast.success('Successfully logged out.');
    }
  };

  const updateProfile = async (data) => {
    try {
      const res = await userApi.updateMe(data);
      setUser(res.data);
      toast.success('Profile updated successfully!');
      return res.data;
    } catch (err) {
      toast.error('Failed to update profile.');
      throw err;
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateProfile, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
