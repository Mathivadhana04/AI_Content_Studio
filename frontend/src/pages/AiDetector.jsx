import React, { useState } from 'react';
import { aiApi } from '../api';
import {
  Box,
  Card,
  Typography,
  TextField,
  Button,
  Grid,
  CircularProgress,
  Stack,
  Divider,
} from '@mui/material';
import {
  FactCheck as DetectorIcon,
  AutoAwesome as SparklesIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';

const AiDetector = () => {
  const [text, setText] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleDetect = async () => {
    if (!text.trim()) {
      toast.error('Please enter some text copy to audit');
      return;
    }
    setLoading(true);
    try {
      toast.loading('Analyzing text perplexity and patterns...', { id: 'detect-act' });
      const res = await aiApi.detect({ text });
      toast.dismiss('detect-act');
      setResult(res.data);
      toast.success('Auditing assessment completed!');
    } catch (err) {
      toast.dismiss('detect-act');
      toast.error('AI content detection failed.');
    } finally {
      setLoading(false);
    }
  };

  const getScoreColor = (score) => {
    if (score >= 70) return '#ef4444'; // Red for AI generated
    if (score >= 40) return '#fbbf24'; // Yellow for Mixed
    return '#10b981'; // Green for human
  };

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title text-gradient">AI Content Detector</Typography>
        <Typography className="page-subtitle">Analyze copy perplexity and burstiness to evaluate AI model authorship likelihood scores.</Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Input Panel */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card className="card-neon" sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              Auditor Copy Inputs
            </Typography>

            <TextField
              fullWidth
              multiline
              rows={15}
              label="Audit Text Block"
              placeholder="Paste the draft or segment copy you wish to audit here..."
              value={text}
              onChange={(e) => setText(e.target.value)}
            />

            <Button
              variant="contained"
              className="btn-brand"
              onClick={handleDetect}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <DetectorIcon />}
            >
              Analyze Content Origin
            </Button>
          </Card>
        </Grid>

        {/* Assessment Panel */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card className="card-neon" sx={{ p: 4, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
              Linguistic assessment
            </Typography>
            <Divider sx={{ mb: 3 }} />

            {loading ? (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', py: 8 }}>
                <CircularProgress />
              </Box>
            ) : result ? (
              <Stack spacing={3} sx={{ flexGrow: 1 }}>
                {/* Score Circle */}
                <Box sx={{ textAlign: 'center', py: 2 }}>
                  <Typography variant="h2" sx={{ fontWeight: 800, color: getScoreColor(result.score), fontFamily: 'Sora' }}>
                    {result.score}%
                  </Typography>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#f0eeff', mt: 0.5 }}>
                    {result.category}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Estimated AI-generation probability
                  </Typography>
                </Box>

                <Divider />

                {/* Audit Feedback Text */}
                <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5, color: '#a8a3c4' }}>
                    Audit Observations & Forensics Report
                  </Typography>
                  <Box sx={{ background: '#141424', p: 3, borderRadius: 3, border: '1px solid rgba(139,92,246,0.18)', flexGrow: 1, overflowY: 'auto' }}>
                    <Typography variant="body2" sx={{ fontFamily: 'Inter', lineHeight: 1.8, color: '#d1cde8', whiteSpace: 'pre-line' }}>
                      {result.feedback}
                    </Typography>
                  </Box>
                </Box>
              </Stack>
            ) : (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', border: '1px dashed rgba(255,255,255,0.05)', borderRadius: 3, py: 10 }}>
                <Typography variant="body2" color="text.secondary" align="center">
                  Analyze copy block on the left to verify authorship probabilities.
                </Typography>
              </Box>
            )}
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default AiDetector;
