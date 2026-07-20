import React, { useState } from 'react';
import { seoApi } from '../api';
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
  Chip,
} from '@mui/material';
import {
  Search as SeoIcon,
  AutoAwesome as SparklesIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';

const SeoKeywordAnalyzer = () => {
  const [text, setText] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [report, setReport] = useState(null);

  const handleAnalyze = async () => {
    if (!text.trim()) {
      toast.error('Please enter some text copy to analyze');
      return;
    }
    setLoading(true);
    try {
      const res = await seoApi.analyzeText({ text, targetKeyword: keyword });
      setReport(res.data);
      toast.success('SEO Quality analysis complete!');
    } catch (err) {
      toast.error('Failed to run SEO keyword check.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title text-gradient">Custom SEO Keyword Score</Typography>
        <Typography className="page-subtitle">Algorithmic analysis of custom copy blocks for target keywords and headings density.</Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Input Panel */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card className="card-neon" sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              Audit Content
            </Typography>

            <TextField
              fullWidth
              label="Target Keyword"
              placeholder="e.g. cloud storage, serverless db"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />

            <TextField
              fullWidth
              multiline
              rows={15}
              label="Paste Copy Content"
              placeholder="Write or paste your article draft copy here in Markdown or plain text..."
              value={text}
              onChange={(e) => setText(e.target.value)}
            />

            <Button
              variant="contained"
              className="btn-brand"
              onClick={handleAnalyze}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SeoIcon />}
            >
              Analyze SEO Quality
            </Button>
          </Card>
        </Grid>

        {/* Report Output Panel */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card className="card-neon" sx={{ p: 4, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, mb: 2 }}>
              SEO Score Report
            </Typography>
            <Divider sx={{ mb: 3 }} />

            {loading ? (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', py: 8 }}>
                <CircularProgress />
              </Box>
            ) : report ? (
              <Stack spacing={3} sx={{ flexGrow: 1 }}>
                {/* Score Dial */}
                <Box sx={{ textAlign: 'center', py: 2 }}>
                  <Typography variant="h2" sx={{ fontWeight: 800, color: report.overallScore >= 80 ? '#10b981' : '#fbbf24', fontFamily: 'Sora' }}>
                    {report.overallScore} <span style={{ fontSize: '1.25rem', color: '#a8a3c4' }}>/ 100</span>
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Overall SEO Score
                  </Typography>
                </Box>

                {/* Score List */}
                <Stack spacing={1.5}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption">Content Word Length</Typography>
                    <Typography variant="caption" sx={{ fontWeight: 700 }}>{report.contentLengthScore} / 100</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption">Keyword Density Check</Typography>
                    <Typography variant="caption" sx={{ fontWeight: 700 }}>{report.keywordDensityScore} / 100</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption">Readability Index</Typography>
                    <Typography variant="caption" sx={{ fontWeight: 700 }}>{report.readabilityScore} / 100</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption">Heading Tag Structure</Typography>
                    <Typography variant="caption" sx={{ fontWeight: 700 }}>{report.headingStructureScore} / 100</Typography>
                  </Box>
                </Stack>

                <Divider />

                {/* Recommendations */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                    Optimization Checklist
                  </Typography>
                  <ul style={{ paddingLeft: 18, fontSize: '0.82rem', color: '#a8a3c4', lineHeight: 1.6 }}>
                    {report.recommendations.split('\n').map((rec, i) => (
                      <li key={i} style={{ marginBottom: 6 }}>{rec}</li>
                    ))}
                  </ul>
                </Box>
              </Stack>
            ) : (
              <Box sx={{ display: 'flex', flexGrow: 1, alignItems: 'center', justifyContent: 'center', border: '1px dashed rgba(255,255,255,0.05)', borderRadius: 3, py: 10 }}>
                <Typography variant="body2" color="text.secondary" align="center">
                  Analyze copy block on the left to review SEO quality metrics.
                </Typography>
              </Box>
            )}
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SeoKeywordAnalyzer;
