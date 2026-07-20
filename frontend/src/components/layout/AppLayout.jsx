import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useNotifications } from '../../contexts/NotificationContext';
import { workspaceApi } from '../../api';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  Badge,
  Avatar,
  Menu,
  MenuItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Select,
  FormControl,
  InputLabel,
  CircularProgress,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  Create as CreateIcon,
  Schedule as ScheduleIcon,
  Share as ShareIcon,
  Email as EmailIcon,
  Notifications as NotificationsIcon,
  Group as GroupIcon,
  Settings as SettingsIcon,
  ExitToApp as LogoutIcon,
  ArrowDropDown as ArrowDropDownIcon,
  Chat as ChatIcon,
  Campaign as CampaignIcon,
  FactCheck as DetectorIcon,
  Search as SeoIcon,
} from '@mui/icons-material';

const drawerWidth = 240;

const AppLayout = () => {
  const { user, logout } = useAuth();
  const { unreadCount, notifications, markAsRead, markAllAsRead } = useNotifications();
  const navigate = useNavigate();
  const location = useLocation();

  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);
  const [notifAnchorEl, setNotifAnchorEl] = useState(null);
  
  // Workspaces State
  const [workspaces, setWorkspaces] = useState([]);
  const [selectedWorkspace, setSelectedWorkspace] = useState('');
  const [loadingWorkspaces, setLoadingWorkspaces] = useState(false);

  useEffect(() => {
    loadWorkspaces();
  }, []);

  const loadWorkspaces = async () => {
    setLoadingWorkspaces(true);
    try {
      const res = await workspaceApi.getAll();
      const wsList = res.data.content || [];
      setWorkspaces(wsList);
      if (wsList.length > 0) {
        // Retrieve from localStorage or select first
        const savedWs = localStorage.getItem('currentWorkspaceId');
        const selected = wsList.find(w => w.id.toString() === savedWs) || wsList[0];
        setSelectedWorkspace(selected.id);
        localStorage.setItem('currentWorkspaceId', selected.id);
      }
    } catch (err) {
      console.error('Failed to load workspaces', err);
    } finally {
      setLoadingWorkspaces(false);
    }
  };

  const handleWorkspaceChange = (event) => {
    const val = event.target.value;
    setSelectedWorkspace(val);
    localStorage.setItem('currentWorkspaceId', val);
    // Reload page or trigger global reload
    window.location.reload();
  };

  const handleDrawerToggle = () => setMobileOpen(!mobileOpen);

  const handleProfileMenuOpen = (e) => setAnchorEl(e.currentTarget);
  const handleProfileMenuClose = () => setAnchorEl(null);

  const handleNotifMenuOpen = (e) => setNotifAnchorEl(e.currentTarget);
  const handleNotifMenuClose = () => setNotifAnchorEl(null);

  const handleLogout = async () => {
    handleProfileMenuClose();
    await logout();
    navigate('/login');
  };

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
    { text: 'AI Blog Writer', icon: <CreateIcon />, path: '/write' },
    { text: 'Social Generator', icon: <ShareIcon />, path: '/social' },
    { text: 'Email Generator', icon: <EmailIcon />, path: '/email' },
    { text: 'Ad Copy Generator', icon: <CampaignIcon />, path: '/ad-copy' },
    { text: 'SEO Analyzer', icon: <SeoIcon />, path: '/seo-analyzer' },
    { text: 'AI Detector', icon: <DetectorIcon />, path: '/ai-detector' },
    { text: 'AI Chatbot', icon: <ChatIcon />, path: '/chatbot' },
    { text: 'Workspace Members', icon: <GroupIcon />, path: '/workspace' },
  ];

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar sx={{ justifyContent: 'center', py: 2 }}>
        <Typography variant="h6" className="text-gradient" sx={{ fontWeight: 800, fontFamily: 'Sora' }}>
          AI Content Studio
        </Typography>
      </Toolbar>
      <Divider />
      
      {/* Workspace Selector */}
      <Box sx={{ p: 2 }}>
        {loadingWorkspaces ? (
          <CircularProgress size={20} sx={{ display: 'block', mx: 'auto' }} />
        ) : (
          <FormControl fullWidth size="small">
            <InputLabel id="workspace-select-label">Workspace</InputLabel>
            <Select
              labelId="workspace-select-label"
              value={selectedWorkspace}
              label="Workspace"
              onChange={handleWorkspaceChange}
              sx={{ background: '#1c1c30' }}
            >
              {workspaces.map((ws) => (
                <MenuItem key={ws.id} value={ws.id}>
                  {ws.name}
                </MenuItem>
              ))}
              <Divider />
              <MenuItem onClick={() => navigate('/workspace?create=true')}>
                + Create Workspace
              </MenuItem>
            </Select>
          </FormControl>
        )}
      </Box>
      <Divider />

      <List sx={{ px: 1, flexGrow: 1 }}>
        {menuItems.map((item) => (
          <ListItemButton
            key={item.text}
            selected={location.pathname === item.path}
            onClick={() => {
              navigate(item.path);
              setMobileOpen(false);
            }}
            sx={{
              borderRadius: 2,
              mb: 0.5,
              '&.Mui-selected': {
                background: 'rgba(139, 92, 246, 0.15)',
                borderLeft: '3px solid #8b5cf6',
              },
            }}
          >
            <ListItemIcon sx={{ color: location.pathname === item.path ? '#8b5cf6' : '#a8a3c4', minWidth: 40 }}>
              {item.icon}
            </ListItemIcon>
            <ListItemText primary={item.text} sx={{ '& .MuiTypography-root': { fontWeight: 550 } }} />
          </ListItemButton>
        ))}
      </List>
      
      <Divider />
      <Box sx={{ p: 2 }}>
        <Typography variant="caption" color="text.disabled" display="block" align="center">
          v1.0.0
        </Typography>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', background: '#0a0a0f' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2, display: { sm: 'none' } }}
            >
              <MenuIcon />
            </IconButton>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {/* Notification Bell */}
            <IconButton color="inherit" onClick={handleNotifMenuOpen}>
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>

            {/* User Dropdown */}
            <Box
              onClick={handleProfileMenuOpen}
              sx={{ display: 'flex', alignItems: 'center', gap: 1, cursor: 'pointer' }}
            >
              <Avatar
                src={user?.avatarUrl}
                sx={{ bgcolor: '#8b5cf6', width: 32, height: 32 }}
              >
                {user?.firstName?.[0]}
              </Avatar>
              <Typography variant="body2" sx={{ display: { xs: 'none', md: 'block' }, fontWeight: 600 }}>
                {user?.fullName}
              </Typography>
              <ArrowDropDownIcon sx={{ display: { xs: 'none', md: 'block' } }} />
            </Box>
          </Box>
        </Toolbar>
      </AppBar>

      {/* Notifications Dropdown */}
      <Menu
        anchorEl={notifAnchorEl}
        open={Boolean(notifAnchorEl)}
        onClose={handleNotifMenuClose}
        PaperProps={{ sx: { width: 320, maxHeight: 400, mt: 1.5 } }}
      >
        <Box sx={{ px: 2, py: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>Notifications</Typography>
          {unreadCount > 0 && (
            <Typography
              variant="caption"
              color="primary"
              sx={{ cursor: 'pointer', fontWeight: 600 }}
              onClick={markAllAsRead}
            >
              Mark all read
            </Typography>
          )}
        </Box>
        <Divider />
        <Box sx={{ maxHeight: 300, overflowY: 'auto' }}>
          {notifications.length === 0 ? (
            <Box sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary">No notifications</Typography>
            </Box>
          ) : (
            notifications.map((n) => (
              <MenuItem
                key={n.id}
                onClick={() => {
                  markAsRead(n.id);
                  if (n.actionUrl) navigate(n.actionUrl);
                  handleNotifMenuClose();
                }}
                sx={{
                  whiteSpace: 'normal',
                  py: 1.5,
                  background: n.read ? 'transparent' : 'rgba(139, 92, 246, 0.05)',
                  borderBottom: '1px solid rgba(139, 92, 246, 0.05)'
                }}
              >
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: n.read ? 400 : 700 }}>
                    {n.title}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" display="block">
                    {n.message}
                  </Typography>
                </Box>
              </MenuItem>
            ))
          )}
        </Box>
      </Menu>

      {/* Profile Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleProfileMenuClose}
        PaperProps={{ sx: { width: 180, mt: 1.5 } }}
      >
        <MenuItem onClick={() => { handleProfileMenuClose(); navigate('/settings'); }}>
          <ListItemIcon><SettingsIcon fontSize="small" /></ListItemIcon>
          <ListItemText>Settings</ListItemText>
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
          <ListItemText>Logout</ListItemText>
        </MenuItem>
      </Menu>

      {/* Sidebar navigation */}
      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        {/* Mobile Drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawerContent}
        </Drawer>
        
        {/* Desktop Drawer */}
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawerContent}
        </Drawer>
      </Box>

      {/* Main Content Area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default AppLayout;
