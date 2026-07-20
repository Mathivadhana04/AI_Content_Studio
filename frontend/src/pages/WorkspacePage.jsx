import React, { useState, useEffect } from 'react';
import { workspaceApi } from '../api';
import {
  Box,
  Typography,
  Card,
  Grid,
  TextField,
  Button,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Stack,
  MenuItem,
  ListItemSecondaryAction,
} from '@mui/material';
import { Group as GroupIcon, Person as PersonIcon, Add as AddIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';

const ROLES = [
  { value: 'EDITOR', label: 'Editor (Can generate and edit)' },
  { value: 'VIEWER', label: 'Viewer (Read-only access)' },
];

const WorkspacePage = () => {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Workspace Info
  const [workspaceInfo, setWorkspaceInfo] = useState(null);

  // Invite Form Fields
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState('EDITOR');
  const [submittingInvite, setSubmittingInvite] = useState(false);

  // Create Workspace Form
  const [newWsName, setNewWsName] = useState('');
  const [newWsDesc, setNewWsDesc] = useState('');
  const [submittingWs, setSubmittingWs] = useState(false);

  useEffect(() => {
    loadWorkspaceDetails();
  }, []);

  const loadWorkspaceDetails = async () => {
    const wsId = localStorage.getItem('currentWorkspaceId');
    if (!wsId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const [infoRes, membersRes] = await Promise.all([
        workspaceApi.getById(wsId),
        workspaceApi.getMembers(wsId),
      ]);
      setWorkspaceInfo(infoRes.data);
      setMembers(membersRes.data || []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load workspace memberships.');
    } finally {
      setLoading(false);
    }
  };

  const handleSendInvite = async () => {
    const wsId = localStorage.getItem('currentWorkspaceId');
    if (!wsId || !inviteEmail) return;

    setSubmittingInvite(true);
    try {
      await workspaceApi.invite(wsId, {
        email: inviteEmail,
        role: inviteRole,
      });
      setInviteEmail('');
      toast.success('Invitation email notification sent successfully!');
      loadWorkspaceDetails();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to send invite.';
      toast.error(errMsg);
    } finally {
      setSubmittingInvite(false);
    }
  };

  const handleRemoveMember = async (userId) => {
    const wsId = localStorage.getItem('currentWorkspaceId');
    if (!wsId) return;

    if (!window.confirm('Are you sure you want to remove this member from the workspace?')) return;

    try {
      await workspaceApi.removeMember(wsId, userId);
      toast.success('Member removed.');
      loadWorkspaceDetails();
    } catch (err) {
      toast.error('Failed to remove member.');
    }
  };

  const handleCreateWorkspace = async () => {
    if (!newWsName.trim()) {
      toast.error('Workspace name is required');
      return;
    }

    setSubmittingWs(true);
    try {
      const res = await workspaceApi.create({
        name: newWsName,
        description: newWsDesc,
      });
      setNewWsName('');
      setNewWsDesc('');
      toast.success('Workspace created successfully!');
      // Set as active workspace
      localStorage.setItem('currentWorkspaceId', res.data.id);
      window.location.reload();
    } catch (err) {
      toast.error('Failed to create workspace.');
    } finally {
      setSubmittingWs(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title">Workspace Settings</Typography>
        <Typography className="page-subtitle">Manage workspace configurations, team memberships, and invites.</Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Members Management Column */}
        {workspaceInfo && (
          <Grid item xs={12} md={7}>
            <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Box>
                <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
                  Active Team Members ({workspaceInfo.name})
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {workspaceInfo.description || 'No description provided.'}
                </Typography>
              </Box>

              <List>
                {members.map((member) => (
                  <React.Fragment key={member.id}>
                    <ListItem sx={{ py: 1.5, px: 0 }}>
                      <ListItemIcon>
                        <PersonIcon sx={{ color: '#8b5cf6' }} />
                      </ListItemIcon>
                      <ListItemText
                        primary={member.fullName}
                        secondary={`${member.email} • ${member.role}`}
                      />
                      {workspaceInfo.owner.id !== member.id && (
                        <ListItemSecondaryAction>
                          <Button size="small" color="error" onClick={() => handleRemoveMember(member.id)}>
                            Remove
                          </Button>
                        </ListItemSecondaryAction>
                      )}
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
              </List>

              {/* Invite Form */}
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5 }}>
                  Invite Collaborator
                </Typography>
                <Stack spacing={2}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Email Address"
                    value={inviteEmail}
                    onChange={(e) => setInviteEmail(e.target.value)}
                  />
                  <TextField
                    fullWidth
                    select
                    size="small"
                    label="Role Permission"
                    value={inviteRole}
                    onChange={(e) => setInviteRole(e.target.value)}
                  >
                    {ROLES.map((r) => (
                      <MenuItem key={r.value} value={r.value}>
                        {r.label}
                      </MenuItem>
                    ))}
                  </TextField>
                  <Button
                    variant="contained"
                    className="btn-brand"
                    onClick={handleSendInvite}
                    disabled={submittingInvite}
                    startIcon={<AddIcon />}
                  >
                    Send Invitation
                  </Button>
                </Stack>
              </Box>
            </Card>
          </Grid>
        )}

        {/* Create Workspace Column */}
        <Grid item xs={12} md={5}>
          <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              Create New Workspace
            </Typography>

            <Stack spacing={2}>
              <TextField
                fullWidth
                label="Workspace Name"
                value={newWsName}
                onChange={(e) => setNewWsName(e.target.value)}
              />
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Workspace Description"
                value={newWsDesc}
                onChange={(e) => setNewWsDesc(e.target.value)}
              />
              <Button
                variant="outlined"
                onClick={handleCreateWorkspace}
                disabled={submittingWs}
              >
                Create Workspace
              </Button>
            </Stack>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default WorkspacePage;
