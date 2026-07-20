import React, { useState } from 'react';
import { aiApi } from '../api';
import {
  Box,
  Card,
  Typography,
  TextField,
  Button,
  Grid,
  MenuItem,
  CircularProgress,
  Stack,
  IconButton,
  Divider,
} from '@mui/material';
import {
  Campaign as CampaignIcon,
  ContentCopy as CopyIcon,
  AutoAwesome as SparklesIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';

const AdCopyGenerator = () => {
  const [platform, setPlatform] = useState('GOOGLE');
  const [productName, setProductName] = useState('');
  const [description, setDescription] = useState('');
  const [audience, setAudience] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState('');

  const handleGenerate = async () => {
    if (!productName.trim() || !description.trim()) {
      toast.error('Product Name and Description are required');
      return;
    }
    setLoading(true);
    try {
      toast.loading('Generating marketing copy variations...', { id: 'ad-act' });
      const res = await aiApi.adCopy({
        platform,
        productName,
        description,
        targetAudience: audience,
      });
      toast.dismiss('ad-act');
      setResult(res.data.message);
      
      // Trigger golden spark visual celebration
      if (window.triggerGoldenSpark) {
        window.triggerGoldenSpark();
      }
      
      toast.success('Ad Copy variants successfully generated!');
    } catch (err) {
      toast.dismiss('ad-act');
      toast.error('AI Copy generation failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
    toast.success('Copied copy to clipboard!');
  };

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title text-gradient">Ad Copy Generator</Typography>
        <Typography className="page-subtitle">Instantly generate platform-specific variations with hooks and call-to-actions.</Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Settings Panel */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card className="card-neon" sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 1 }}>
              <CampaignIcon sx={{ color: '#8b5cf6' }} /> Ad Settings
            </Typography>

            <TextField
              fullWidth
              select
              label="Target Platform"
              value={platform}
              onChange={(e) => setPlatform(e.target.value)}
            >
              <MenuItem value="GOOGLE">Google Search Ads</MenuItem>
              <MenuItem value="FACEBOOK">Facebook Newsfeed Ads</MenuItem>
              <MenuItem value="INSTAGRAM">Instagram Feed/Story Ads</MenuItem>
              <MenuItem value="LINKEDIN">LinkedIn Sponsored Copy</MenuItem>
            </TextField>

            <TextField
              fullWidth
              label="Product / Service Name"
              placeholder="e.g. Acme Cloud Storage"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
            />

            <TextField
              fullWidth
              multiline
              rows={4}
              label="Product Description / Core Value"
              placeholder="e.g. Ultra-secure file sharing and backup for developer teams. Fits any infrastructure, fully encrypted."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />

            <TextField
              fullWidth
              label="Target Audience (Optional)"
              placeholder="e.g. Freelance designers, remote managers"
              value={audience}
              onChange={(e) => setAudience(e.target.value)}
            />

            <Button
              variant="contained"
              className="btn-brand"
              onClick={handleGenerate}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SparklesIcon />}
            >
              Generate Ad Copies
            </Button>
          </Card>
        </Grid>

        {/* Copy Outputs Panel */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card className="card-neon" sx={{ p: 4, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
                Generated Copy Copy
              </Typography>
              {result && (
                <IconButton onClick={() => handleCopy(result)}>
                  <CopyIcon />
                </IconButton>
              )}
            </Box>
            <Divider sx={{ mb: 3 }} />

            {loading ? (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', py: 8 }}>
                <CircularProgress />
              </Box>
            ) : result ? (
              <Box sx={{ background: '#141424', p: 3, borderRadius: 3, border: '1px solid rgba(139,92,246,0.18)', flexGrow: 1, overflowY: 'auto' }}>
                <Typography variant="body2" sx={{ whiteSpace: 'pre-line', fontFamily: 'Inter', lineHeight: 1.8, color: '#f0eeff' }}>
                  {result}
                </Typography>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', border: '1px dashed rgba(255,255,255,0.05)', borderRadius: 3, py: 10 }}>
                <Typography variant="body2" color="text.secondary">
                  Specify parameters on the left and generate to populate variations.
                </Typography>
              </Box>
            )}
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default AdCopyGenerator;
