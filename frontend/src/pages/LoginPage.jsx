import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import {
  Box,
  Card,
  TextField,
  Button,
  Typography,
  Container,
  InputAdornment,
  IconButton,
  CircularProgress,
} from '@mui/material';
import { Email, Lock, Visibility, VisibilityOff } from '@mui/icons-material';

const schema = zod.object({
  email: zod.string().email('Please enter a valid email address'),
  password: zod.string().min(1, 'Password is required'),
});

const LoginPage = () => {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = React.useState(false);

  React.useEffect(() => {
    // Trigger golden spark on page load/entry
    if (window.triggerGoldenSpark) {
      setTimeout(() => {
        window.triggerGoldenSpark();
      }, 500);
    }
  }, []);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    try {
      await login(data.email, data.password);
      // Trigger golden spark on login success
      if (window.triggerGoldenSpark) {
        window.triggerGoldenSpark();
      }
      navigate('/dashboard');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'radial-gradient(circle at center, #15152a 0%, #0a0a0f 100%)',
        p: 2,
      }}
    >
      <Container maxWidth="xs">
        <Card className="card-neon" sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="h4" className="text-gradient" sx={{ fontWeight: 800, fontFamily: 'Sora', mb: 1 }}>
              Welcome Back
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sign in to your AI Content Studio account
            </Typography>
          </Box>

          <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            <TextField
              fullWidth
              label="Email Address"
              error={!!errors.email}
              helperText={errors.email?.message}
              {...register('email')}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Email sx={{ color: 'text.muted' }} />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              type={showPassword ? 'text' : 'password'}
              label="Password"
              error={!!errors.password}
              helperText={errors.password?.message}
              {...register('password')}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock sx={{ color: 'text.muted' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button
              fullWidth
              type="submit"
              variant="contained"
              disabled={loading}
              sx={{ py: 1.5, mt: 1 }}
              className="btn-brand"
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
            </Button>
          </form>

          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Don't have an account?{' '}
              <Link to="/register" style={{ fontWeight: 600 }}>
                Register here
              </Link>
            </Typography>
          </Box>
        </Card>
      </Container>
    </Box>
  );
};

export default LoginPage;
