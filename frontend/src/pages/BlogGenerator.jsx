import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { blogApi } from '../api';
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
  Slider,
} from '@mui/material';
import { Create as CreateIcon, AutoAwesome as SparklesIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';

const TONES = [
  { value: 'PROFESSIONAL', label: 'Professional' },
  { value: 'FORMAL', label: 'Formal' },
  { value: 'ACADEMIC', label: 'Academic' },
  { value: 'CASUAL', label: 'Casual' },
  { value: 'MARKETING', label: 'Marketing' },
  { value: 'TECHNICAL', label: 'Technical' },
  { value: 'HUMANIZED', label: 'Humanized' },
  { value: 'SIMPLIFIED', label: 'Simplified' },
  { value: 'PERSUASIVE', label: 'Persuasive' },
  { value: 'CREATIVE', label: 'Creative' },
];

const BlogGenerator = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1);

  // Form Fields State
  const [topic, setTopic] = useState('');
  const [audience, setAudience] = useState('general audience');
  const [tone, setTone] = useState('PROFESSIONAL');
  const [keywords, setKeywords] = useState('');
  const [wordCount, setWordCount] = useState(1000);

  const handleNext = () => {
    if (step === 1 && !topic.trim()) {
      toast.error('Please enter a topic description first');
      return;
    }
    setStep(2);
  };

  const handleBack = () => setStep(1);

  const handleSubmit = async () => {
    setLoading(true);
    const workspaceId = localStorage.getItem('currentWorkspaceId');
    try {
      const payload = {
        topic,
        targetAudience: audience,
        tone,
        keywords,
        targetWordCount: wordCount,
        workspaceId: workspaceId ? parseInt(workspaceId) : null,
      };

      toast.loading('AI content generation is running. Please wait up to 30 seconds...', { id: 'gen-loading' });
      const res = await blogApi.generate(payload);
      toast.dismiss('gen-loading');
      toast.success('Blog generated successfully!');
      if (window.triggerGoldenSpark) {
        window.triggerGoldenSpark();
      }
      
      // Redirect to editor
      navigate(`/editor/${res.data.id}`);
    } catch (err) {
      toast.dismiss('gen-loading');
      const errMsg = err.response?.data?.message || 'Failed to generate blog post. Verify Groq connection.';
      toast.error(errMsg);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexGrow: 1, flexDirection: 'column', alignItems: 'center', justifyContent: 'center', p: 4 }}>
        <Card sx={{ p: 6, textAlign: 'center', maxWidth: 450, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box>
            <CircularProgress size={60} thickness={4} sx={{ color: '#8b5cf6', mb: 2 }} />
            <SparklesIcon sx={{ fontSize: 32, color: '#fbbf24', animation: 'pulse 1.5s infinite', display: 'block', mx: 'auto' }} />
          </Box>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 800, fontFamily: 'Sora', mb: 1 }}>
              Generating Article Outline...
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Llama models are writing sections, structuring headings, and optimizing content readability. This can take up to 25 seconds.
            </Typography>
          </Box>
        </Card>
      </Box>
    );
  }

  return (
    <Box className="page-container">
      <Box className="page-header">
        <Typography className="page-title text-gradient">AI Content Wizard</Typography>
        <Typography className="page-subtitle">Define content topics, tone parameters, and targets to construct articles.</Typography>
      </Box>

      <Card className="card-neon" sx={{ p: 4, maxWidth: 900, mx: 'auto' }}>
        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 4 }}>
          <Box sx={{ width: 32, height: 32, borderRadius: '50%', background: step === 1 ? '#8b5cf6' : '#252540', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>1</Box>
          <Box sx={{ flexGrow: 1, height: 2, background: 'rgba(255,255,255,0.05)' }} />
          <Box sx={{ width: 32, height: 32, borderRadius: '50%', background: step === 2 ? '#8b5cf6' : '#252540', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>2</Box>
        </Stack>

        {step === 1 ? (
          <Stack spacing={3}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              What would you like to write about?
            </Typography>
            
            <TextField
              fullWidth
              multiline
              rows={4}
              label="Article Topic Description"
              placeholder="e.g. A comprehensive guide on React 19 features including Server Components, Action Hooks, and resource loading optimizations."
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
            />

            <TextField
              fullWidth
              label="Target Audience"
              placeholder="e.g. Web developers, product managers, marketing experts"
              value={audience}
              onChange={(e) => setAudience(e.target.value)}
            />

            <Button variant="contained" className="btn-brand" onClick={handleNext}>
              Continue Configuration
            </Button>
          </Stack>
        ) : (
          <Stack spacing={3}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700 }}>
              Optimize Generation Settings
            </Typography>

            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Writing Tone"
                  value={tone}
                  onChange={(e) => setTone(e.target.value)}
                >
                  {TONES.map((opt) => (
                    <MenuItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Target Keywords (comma separated)"
                  placeholder="e.g. React 19, Server Components, web development"
                  value={keywords}
                  onChange={(e) => setKeywords(e.target.value)}
                />
              </Grid>
            </Grid>

            <Box sx={{ px: 1, py: 2 }}>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Target Word Count: <strong>{wordCount} words</strong>
              </Typography>
              <Slider
                value={wordCount}
                min={300}
                max={3000}
                step={100}
                onChange={(e, val) => setWordCount(val)}
                valueLabelDisplay="auto"
                sx={{ color: '#8b5cf6' }}
              />
            </Box>

            <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
              <Button variant="outlined" onClick={handleBack} sx={{ flexGrow: 1 }}>
                Back
              </Button>
              <Button variant="contained" className="btn-brand" onClick={handleSubmit} sx={{ flexGrow: 2 }}>
                Generate Blog
              </Button>
            </Stack>
          </Stack>
        )}
      </Card>
    </Box>
  );
};

export default BlogGenerator;
