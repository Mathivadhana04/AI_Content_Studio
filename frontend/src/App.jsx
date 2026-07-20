import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { theme } from './styles/theme';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { NotificationProvider } from './contexts/NotificationContext';
import { Toaster } from 'react-hot-toast';

// Layout
import AppLayout from './components/layout/AppLayout';

// Pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Dashboard from './pages/Dashboard';
import BlogGenerator from './pages/BlogGenerator';
import BlogEditor from './pages/BlogEditor';
import SocialGenerator from './pages/SocialGenerator';
import EmailGenerator from './pages/EmailGenerator';
import WorkspacePage from './pages/WorkspacePage';
import SettingsPage from './pages/SettingsPage';
import ChatbotPage from './pages/ChatbotPage';
import AdCopyGenerator from './pages/AdCopyGenerator';
import SeoKeywordAnalyzer from './pages/SeoKeywordAnalyzer';
import AiDetector from './pages/AiDetector';

// Guard for Private Routes
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null; // Let global loading skeleton handle
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return children;
};

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: '#1c1c30',
            color: '#f0eeff',
            border: '1px solid rgba(139, 92, 246, 0.22)',
            borderRadius: '10px',
          },
        }}
      />
      <BrowserRouter>
        <AuthProvider>
          <NotificationProvider>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<LandingPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* Protected Workspace Dashboard Routes */}
              <Route
                element={
                  <ProtectedRoute>
                    <AppLayout />
                  </ProtectedRoute>
                }
              >
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/write" element={<BlogGenerator />} />
                <Route path="/editor/:id" element={<BlogEditor />} />
                <Route path="/social" element={<SocialGenerator />} />
                <Route path="/email" element={<EmailGenerator />} />
                <Route path="/chatbot" element={<ChatbotPage />} />
                <Route path="/ad-copy" element={<AdCopyGenerator />} />
                <Route path="/seo-analyzer" element={<SeoKeywordAnalyzer />} />
                <Route path="/ai-detector" element={<AiDetector />} />
                <Route path="/workspace" element={<WorkspacePage />} />
                <Route path="/settings" element={<SettingsPage />} />
              </Route>

              {/* Redirect any other path to landing page */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </NotificationProvider>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
};

export default App;
