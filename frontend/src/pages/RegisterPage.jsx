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
  Grid,
} from '@mui/material';
import { Email, Lock, Visibility, VisibilityOff, Person } from '@mui/icons-material';

const schema = zod.object({
  firstName: zod.string().min(2, 'First name must be at least 2 characters'),
  lastName: zod.string().min(2, 'Last name must be at least 2 characters'),
  email: zod.string().email('Please enter a valid email address'),
  password: zod.string().min(8, 'Password must be at least 8 characters'),
});

const RegisterPage = () => {
  const { register: registerAuth, loading } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = React.useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    try {
      await registerAuth(data.firstName, data.lastName, data.email, data.password);
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
      <Container maxWidth="sm">
        <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="h4" className="text-gradient" sx={{ fontWeight: 800, fontFamily: 'Sora', mb: 1 }}>
              Create Account
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Get started with AI Content Studio SaaS Platform
            </Typography>
          </Box>

          <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  error={!!errors.firstName}
                  helperText={errors.firstName?.message}
                  {...register('firstName')}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Person sx={{ color: 'text.muted' }} />
                      </InputAdornment>
                    ),
                  }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  error={!!errors.lastName}
                  helperText={errors.lastName?.message}
                  {...register('lastName')}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Person sx={{ color: 'text.muted' }} />
                      </InputAdornment>
                    ),
                  }}
                />
              </Grid>
            </Grid>

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
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Register'}
            </Button>
          </form>

          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Already have an account?{' '}
              <Link to="/login" style={{ fontWeight: 600 }}>
                Sign in here
              </Link>
            </Typography>
          </Box>
        </Card>
      </Container>
    </Box>
  );
};

export default RegisterPage;
