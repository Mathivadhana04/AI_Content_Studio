import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { userApi } from '../api';
import {
  Box,
  Card,
  Typography,
  TextField,
  Button,
  Stack,
  Divider,
  Grid,
} from '@mui/material';
import toast from 'react-hot-toast';

const SettingsPage = () => {
  const { user, updateProfile } = useAuth();

  // Profile Form State
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [bio, setBio] = useState(user?.bio || '');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
  const [updatingProfile, setUpdatingProfile] = useState(false);

  // Password Form State
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [updatingPassword, setUpdatingPassword] = useState(false);

  const handleUpdateProfile = async () => {
    if (!firstName.trim() || !lastName.trim()) {
      toast.error('First and Last names are required');
      return;
    }
    setUpdatingProfile(true);
    try {
      await updateProfile({
        firstName,
        lastName,
        bio,
        avatarUrl,
        email: user.email,
        password: '', // Password is not updated through this method
      });
    } catch (err) {
      console.error(err);
    } finally {
      setUpdatingProfile(false);
    }
  };

  const handleChangePassword = async () => {
    if (!oldPassword || !newPassword) {
      toast.error('Both old and new passwords are required');
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }
    setUpdatingPassword(true);
    try {
      toast.loading('Changing password...', { id: 'pwd-act' });
      await userApi.changePassword({
        oldPassword,
        newPassword,
      });
      toast.dismiss('pwd-act');
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
      toast.success('Password changed successfully!');
    } catch (err) {
      toast.dismiss('pwd-act');
      const errMsg = err.response?.data?.message || 'Failed to update password.';
      toast.error(errMsg);
    } finally {
      setUpdatingPassword(false);
    }
  };

  return (
    <Box className="page-container" sx={{ maxWidth: 800 }}>
      <Box className="page-header">
        <Typography className="page-title">Profile Settings</Typography>
        <Typography className="page-subtitle">Configure account information, profile pictures, and password credentials.</Typography>
      </Box>

      <Stack spacing={4}>
        {/* Profile Card */}
        <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
            Profile Information
          </Typography>

          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="First Name"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Last Name"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Avatar Image URL"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Short Bio"
                value={bio}
                onChange={(e) => setBio(e.target.value)}
              />
            </Grid>
          </Grid>

          <Button
            variant="contained"
            className="btn-brand"
            onClick={handleUpdateProfile}
            disabled={updatingProfile}
          >
            Update Profile Info
          </Button>
        </Card>

        {/* Change Password Card */}
        <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
            Security Settings (Change Password)
          </Typography>

          <Stack spacing={2}>
            <TextField
              fullWidth
              type="password"
              label="Current Password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
            />
            <TextField
              fullWidth
              type="password"
              label="New Password (min 8 characters)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
            <TextField
              fullWidth
              type="password"
              label="Confirm New Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </Stack>

          <Button
            variant="outlined"
            onClick={handleChangePassword}
            disabled={updatingPassword}
          >
            Change Account Password
          </Button>
        </Card>
      </Stack>
    </Box>
  );
};

export default SettingsPage;
