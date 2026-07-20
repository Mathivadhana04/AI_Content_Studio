import React, { useState, useEffect } from 'react';
import { socialApi, blogApi } from '../api';
import {
  Box,
  Card,
  Typography,
  TextField,
  Button,
  Grid,
  CircularProgress,
  MenuItem,
  Stack,
  Paper,
  Divider,
} from '@mui/material';
import { Share as ShareIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';

const PLATFORMS = [
  { value: 'LINKEDIN', label: 'LinkedIn Post' },
  { value: 'TWITTER', label: 'Twitter Thread/Post' },
  { value: 'FACEBOOK', label: 'Facebook Update' },
  { value: 'INSTAGRAM', label: 'Instagram Caption' },
];

const SocialGenerator = () => {
  const [blogs, setBlogs] = useState([]);
  const [selectedBlogId, setSelectedBlogId] = useState('');
  const [platform, setPlatform] = useState('LINKEDIN');
  
  const [loading, setLoading] = useState(false);
  const [loadingBlogs, setLoadingBlogs] = useState(true);
  const [result, setResult] = useState('');

  useEffect(() => {
    loadBlogs();
  }, []);

  const loadBlogs = async () => {
    setLoadingBlogs(true);
    try {
      const res = await blogApi.getMyBlogs();
      const contentList = res.data.content || [];
      setBlogs(contentList);
      if (contentList.length > 0) {
        setSelectedBlogId(contentList[0].id);
      }
    } catch (err) {
      toast.error('Failed to load blog catalog.');
    } finally {
      setLoadingBlogs(false);
    }
  };

  const handleGenerate = async () => {
    if (!selectedBlogId) {
      toast.error('Please choose a source blog article first');
      return;
    }
    setLoading(true);
    try {
      toast.loading('Generating social media content copy...', { id: 'social-act' });
      const res = await socialApi.generate({
        blogId: selectedBlogId,
        platform,
      });
      toast.dismiss('social-act');
      setResult(res.data.content);
      if (window.triggerGoldenSpark) {
        window.triggerGoldenSpark();
      }
      toast.success('Social post successfully generated!');
    } catch (err) {
      toast.dismiss('social-act');
      toast.error('AI post generation failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title">Social Content Generator</Typography>
        <Typography className="page-subtitle">Instantly generate platform-specific copy from blog posts.</Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Selector Input Card */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card className="card-neon" sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              Generate Copy
            </Typography>

            {loadingBlogs ? (
              <CircularProgress size={24} sx={{ mx: 'auto' }} />
            ) : (
              <TextField
                fullWidth
                select
                label="Choose Source Article"
                value={selectedBlogId}
                onChange={(e) => setSelectedBlogId(e.target.value)}
              >
                {blogs.map((b) => (
                  <MenuItem key={b.id} value={b.id}>
                    {b.title}
                  </MenuItem>
                ))}
              </TextField>
            )}

            <TextField
              fullWidth
              select
              label="Select Social Channel"
              value={platform}
              onChange={(e) => setPlatform(e.target.value)}
            >
              <MenuItem value="LINKEDIN">LinkedIn Post copy</MenuItem>
              <MenuItem value="TWITTER">Twitter/X Thread copy</MenuItem>
              <MenuItem value="FACEBOOK">Facebook Feed Post</MenuItem>
              <MenuItem value="INSTAGRAM">Instagram Caption copy</MenuItem>
            </TextField>

            <Button
              variant="contained"
              className="btn-brand"
              onClick={handleGenerate}
              disabled={loading}
              startIcon={<ShareIcon />}
            >
              Generate Social Copy
            </Button>
          </Card>
        </Grid>

        {/* Copy Result Display Card */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card className="card-neon" sx={{ p: 4, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
              Copy Output Preview
            </Typography>

            {loading ? (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center' }}>
                <CircularProgress />
              </Box>
            ) : result ? (
              <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1, gap: 2 }}>
                <Paper
                  sx={{
                    p: 3,
                    background: '#141424',
                    border: '1px solid rgba(139, 92, 246, 0.18)',
                    borderRadius: 3,
                    flexGrow: 1,
                    minHeight: 350,
                    whiteSpace: 'pre-wrap',
                    overflowY: 'auto'
                  }}
                >
                  <Typography variant="body2">{result}</Typography>
                </Paper>
                <Button
                  variant="contained"
                  className="btn-brand"
                  onClick={() => {
                    navigator.clipboard.writeText(result);
                    toast.success('Copy text captured!');
                  }}
                >
                  Copy to Clipboard
                </Button>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', border: '1px dashed rgba(255,255,255,0.05)', borderRadius: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Choose settings on left to produce live previews here.
                </Typography>
              </Box>
            )}
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SocialGenerator;
