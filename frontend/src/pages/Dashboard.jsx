import React, { useState, useEffect } from 'react';
import { analyticsApi, blogApi } from '../api';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Grid,
  Typography,
  Card,
  CircularProgress,
  Stack,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
} from '@mui/material';
import {
  Create as CreateIcon,
  Description as DescriptionIcon,
  Speed as SpeedIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

const COLORS = ['#8b5cf6', '#10b981', '#fbbf24', '#ef4444'];

const Dashboard = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [recentBlogs, setRecentBlogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const currentWs = localStorage.getItem('currentWorkspaceId');
      const [statsRes, blogsRes] = await Promise.all([
        analyticsApi.getSummary(),
        currentWs
          ? blogApi.getWorkspaceBlogs(currentWs, { page: 0, size: 5 })
          : blogApi.getMyBlogs({ page: 0, size: 5 }),
      ]);
      setStats(statsRes.data);
      setRecentBlogs(blogsRes.data.content || []);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Format Recharts status data
  const pieData = stats?.blogsByStatus
    ? Object.keys(stats.blogsByStatus).map((key) => ({
        name: key,
        value: stats.blogsByStatus[key],
      }))
    : [];

  // Format Recharts monthly trend data
  const areaData = stats?.blogsByMonth
    ? Object.keys(stats.blogsByMonth).map((key) => ({
        name: key,
        count: stats.blogsByMonth[key],
      }))
    : [];

  const cards = [
    {
      title: 'Total Articles',
      value: stats?.totalBlogs || 0,
      desc: 'Platform-wide drafts & published',
      icon: <DescriptionIcon sx={{ fontSize: 32, color: '#8b5cf6' }} />,
    },
    {
      title: 'Published',
      value: stats?.publishedBlogs || 0,
      desc: 'Active published articles',
      icon: <TrendingUpIcon sx={{ fontSize: 32, color: '#10b981' }} />,
    },
    {
      title: 'AI Requests Today',
      value: stats?.aiRequestsToday || 0,
      desc: 'Daily usage rate limit tracker',
      icon: <SpeedIcon sx={{ fontSize: 32, color: '#fbbf24' }} />,
    },
    {
      title: 'Avg SEO Score',
      value: stats?.averageSeoScore ? Math.round(stats.averageSeoScore) : 0,
      desc: 'Target optimization score',
      icon: <CreateIcon sx={{ fontSize: 32, color: '#8b5cf6' }} />,
    },
  ];

  return (
    <Box className="page-container">
      <Box className="page-header" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography className="page-title">Dashboard</Typography>
          <Typography className="page-subtitle">Welcome back! Here is a summary of your workspace content.</Typography>
        </Box>
        <Button variant="contained" className="btn-brand" onClick={() => navigate('/write')}>
          + Generate Blog
        </Button>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {cards.map((card, idx) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={idx}>
            <Card className="card-neon" sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Stack spacing={1}>
                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                  {card.title}
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, fontFamily: 'Sora' }}>
                  {card.value}
                </Typography>
                <Typography variant="caption" color="text.muted">
                  {card.desc}
                </Typography>
              </Stack>
              <Box>{card.icon}</Box>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Charts Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card className="card-neon" sx={{ p: 3, height: 350 }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
              Content Generation Trend
            </Typography>
            <Box sx={{ width: '100%', height: 260 }}>
              {areaData.length === 0 ? (
                <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                  <Typography variant="body2" color="text.secondary">No trend data available</Typography>
                </Box>
              ) : (
                <ResponsiveContainer>
                  <AreaChart data={areaData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.4} />
                        <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Area type="monotone" dataKey="count" stroke="#8b5cf6" fillOpacity={1} fill="url(#colorCount)" />
                  </AreaChart>
                </ResponsiveContainer>
              )}
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card className="card-neon" sx={{ p: 3, height: 350 }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
              Status Breakdown
            </Typography>
            <Box sx={{ width: '100%', height: 200, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
              {pieData.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No status data</Typography>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </Box>
            <Stack direction="row" spacing={2} justifyContent="center" sx={{ mt: 1 }}>
              {pieData.map((d, i) => (
                <Stack key={d.name} direction="row" spacing={0.5} alignItems="center">
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', background: COLORS[i % COLORS.length] }} />
                  <Typography variant="caption" color="text.secondary">
                    {d.name}: {d.value}
                  </Typography>
                </Stack>
              ))}
            </Stack>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Activity Table */}
      <Card className="card-neon" sx={{ p: 3 }}>
        <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
          Recent Articles
        </Typography>
        {recentBlogs.length === 0 ? (
          <Box sx={{ py: 6, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              No articles created yet in this workspace.
            </Typography>
            <Button variant="outlined" size="small" onClick={() => navigate('/write')}>
              Write Your First Post
            </Button>
          </Box>
        ) : (
          <TableContainer component={Paper} sx={{ background: 'transparent', boxShadow: 'none', border: 'none' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Title</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Words</TableCell>
                  <TableCell>SEO Score</TableCell>
                  <TableCell>Platform</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {recentBlogs.map((blog) => (
                  <TableRow key={blog.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/editor/${blog.id}`)}>
                    <TableCell sx={{ fontWeight: 600 }}>{blog.title}</TableCell>
                    <TableCell>
                      <Chip
                        label={blog.status}
                        size="small"
                        color={
                          blog.status === 'PUBLISHED'
                            ? 'success'
                            : blog.status === 'SCHEDULED'
                            ? 'warning'
                            : 'default'
                        }
                        sx={{ fontSize: '0.72rem', fontWeight: 700 }}
                      />
                    </TableCell>
                    <TableCell>{blog.wordCount}</TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>
                          {blog.seoScore || '-'}
                        </Typography>
                        {blog.seoScore > 0 && (
                          <Box
                            sx={{
                              width: 8,
                              height: 8,
                              borderRadius: '50%',
                              background: blog.seoScore >= 80 ? '#10b981' : blog.seoScore >= 50 ? '#f59e0b' : '#ef4444',
                            }}
                          />
                        )}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={blog.aiGenerated ? 'AI' : 'Manual'}
                        size="small"
                        variant="outlined"
                        sx={{
                          fontSize: '0.68rem',
                          borderColor: blog.aiGenerated ? 'rgba(139, 92, 246, 0.4)' : 'rgba(255,255,255,0.1)',
                          color: blog.aiGenerated ? '#a78bfa' : '#a8a3c4',
                        }}
                      />
                    </TableCell>
                    <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                      <Button size="small" variant="outlined" onClick={() => navigate(`/editor/${blog.id}`)}>
                        Edit
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Card>
    </Box>
  );
};

export default Dashboard;
