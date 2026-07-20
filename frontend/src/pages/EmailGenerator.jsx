import React, { useState, useEffect } from 'react';
import { emailApi, blogApi } from '../api';
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
import { Email as EmailIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';

const EMAIL_TYPES = [
  { value: 'NEWSLETTER', label: 'Newsletter Digest' },
  { value: 'LAUNCH', label: 'Product Launch Announcement' },
  { value: 'DIGEST', label: 'Content Recap Digest' },
  { value: 'CAMPAIGN', label: 'Promotional Campaign' },
];

const EmailGenerator = () => {
  const [blogs, setBlogs] = useState([]);
  const [selectedBlogId, setSelectedBlogId] = useState('');
  const [emailType, setEmailType] = useState('NEWSLETTER');
  const [subject, setSubject] = useState('');
  const [audience, setAudience] = useState('Subscribers');

  const [loading, setLoading] = useState(false);
  const [loadingBlogs, setLoadingBlogs] = useState(true);
  const [resultHtml, setResultHtml] = useState('');

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
        setSubject('Newsletter: ' + contentList[0].title);
      }
    } catch (err) {
      toast.error('Failed to load blog catalog.');
    } finally {
      setLoadingBlogs(false);
    }
  };

  const handleBlogChange = (id) => {
    setSelectedBlogId(id);
    const chosen = blogs.find(b => b.id === id);
    if (chosen) {
      setSubject('Newsletter: ' + chosen.title);
    }
  };

  const handleGenerate = async () => {
    if (!subject.trim()) {
      toast.error('Subject line is required');
      return;
    }
    setLoading(true);
    try {
      toast.loading('Generating email HTML copy...', { id: 'email-act' });
      const res = await emailApi.generate({
        emailType,
        subject,
        targetAudience: audience,
        sourceBlogId: selectedBlogId ? parseInt(selectedBlogId) : null,
      });
      toast.dismiss('email-act');
      setResultHtml(res.data.htmlContent);
      if (window.triggerGoldenSpark) {
        window.triggerGoldenSpark();
      }
      toast.success('Email campaign successfully generated!');
    } catch (err) {
      toast.dismiss('email-act');
      toast.error('AI Email generation failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title">Email Campaign Generator</Typography>
        <Typography className="page-subtitle">Draft newsletters and launch campaigns derived from articles.</Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Settings Card */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card className="card-neon" sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              Email Settings
            </Typography>

            {loadingBlogs ? (
              <CircularProgress size={24} sx={{ mx: 'auto' }} />
            ) : (
              <TextField
                fullWidth
                select
                label="Source Article"
                value={selectedBlogId}
                onChange={(e) => handleBlogChange(e.target.value)}
              >
                <MenuItem value=""><em>None (Empty context)</em></MenuItem>
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
              label="Campaign Type"
              value={emailType}
              onChange={(e) => setEmailType(e.target.value)}
            >
              {EMAIL_TYPES.map((t) => (
                <MenuItem key={t.value} value={t.value}>
                  {t.label}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              fullWidth
              label="Subject Line"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
            />

            <TextField
              fullWidth
              label="Target Subscribers List"
              value={audience}
              onChange={(e) => setAudience(e.target.value)}
            />

            <Button
              variant="contained"
              className="btn-brand"
              onClick={handleGenerate}
              disabled={loading}
              startIcon={<EmailIcon />}
            >
              Generate HTML Email
            </Button>
          </Card>
        </Grid>

        {/* Output Result card */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card className="card-neon" sx={{ p: 4, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
              HTML Preview Output
            </Typography>

            {loading ? (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center' }}>
                <CircularProgress />
              </Box>
            ) : resultHtml ? (
              <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1, gap: 2 }}>
                <Paper
                  sx={{
                    p: 1,
                    background: '#ffffff',
                    flexGrow: 1,
                    minHeight: 400,
                    borderRadius: 2,
                    overflow: 'hidden'
                  }}
                >
                  {/* Styled Iframe Preview */}
                  <iframe
                    title="Email HTML Preview"
                    srcDoc={resultHtml}
                    style={{ width: '100%', height: '100%', border: 'none', background: 'white' }}
                  />
                </Paper>
                <Button
                  variant="contained"
                  className="btn-brand"
                  onClick={() => {
                    navigator.clipboard.writeText(resultHtml);
                    toast.success('HTML layout copied!');
                  }}
                >
                  Copy HTML Source Code
                </Button>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', border: '1px dashed rgba(255,255,255,0.05)', borderRadius: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Trigger configuration on left to compose newsletter outlines.
                </Typography>
              </Box>
            )}
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default EmailGenerator;
