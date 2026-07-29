import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { blogApi, aiApi, seoApi, headlineApi } from '../api';
import {
  Box,
  Grid,
  Card,
  Typography,
  TextField,
  Button,
  CircularProgress,
  Stack,
  Tabs,
  Tab,
  MenuItem,
  List,
  ListItem,
  ListItemText,
  Divider,
  Paper,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Save as SaveIcon,
  AutoAwesome as SparklesIcon,
  Search as SeoIcon,
  Settings as SettingsIcon,
  QuestionAnswer as ChatIcon,
  History as HistoryIcon,
  CheckCircle as CheckCircleIcon,
  Send as SendIcon,
} from '@mui/icons-material';
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
];

const BlogEditor = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [blog, setBlog] = useState(null);
  const [content, setContent] = useState('');
  const [title, setTitle] = useState('');
  const [metaDesc, setMetaDesc] = useState('');
  const [loading, setLoading] = useState(true);

  // Tabs state
  const [tabVal, setTabVal] = useState(0);
  const [previewMode, setPreviewMode] = useState(false);


  // AI Sidebar State
  const [selectedText, setSelectedText] = useState('');
  const [rewriteTone, setRewriteTone] = useState('PROFESSIONAL');
  const [aiLoading, setAiLoading] = useState(false);
  const [aiResult, setAiResult] = useState('');

  // SEO Tab State
  const [seoReport, setSeoReport] = useState(null);
  const [seoLoading, setSeoLoading] = useState(false);
  const [seoKeyword, setSeoKeyword] = useState('');

  // Headlines Tab State
  const [headlines, setHeadlines] = useState([]);
  const [headlineLoading, setHeadlineLoading] = useState(false);

  // Chat Assistant State
  const [chatQuestion, setChatQuestion] = useState('');
  const [chatMessages, setChatMessages] = useState([]);
  const [chatLoading, setChatLoading] = useState(false);

  // Versions State
  const [versions, setVersions] = useState([]);
  const [versionNote, setVersionNote] = useState('');

  // Auto-save timer reference
  const autoSaveTimer = useRef(null);

  useEffect(() => {
    loadBlog();
    loadVersions();
  }, [id]);

  const loadBlog = async () => {
    setLoading(true);
    try {
      const res = await blogApi.getById(id);
      setBlog(res.data);
      setTitle(res.data.title);
      const loadedContent = res.data.content || '';
      setContent(loadedContent);
      setMetaDesc(res.data.metaDescription || '');
      setSeoKeyword(res.data.keywords || '');
      // Auto-preview if content contains styled HTML headings
      if (loadedContent.includes('<h1') || loadedContent.includes('<h2')) {
        setPreviewMode(true);
      }

    } catch (err) {
      toast.error('Failed to load article details.');
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const loadVersions = async () => {
    try {
      const res = await blogApi.getVersions(id);
      setVersions(res.data || []);
    } catch (err) {
      console.warn('Failed to load version snapshots');
    }
  };

  // Detect selection for inline actions
  const handleEditorChange = (e) => {
    const value = e.target.value;
    setContent(value);
    const selection = window.getSelection().toString().trim();
    if (selection) {
      setSelectedText(selection);
    }

    // Auto-save logic (trigger 3 seconds after last typing)
    if (autoSaveTimer.current) clearTimeout(autoSaveTimer.current);
    autoSaveTimer.current = setTimeout(() => {
      triggerSilentSave(value);
    }, 3000);
  };

  // Capture selected text from textarea
  const handleEditorSelect = (e) => {
    const { selectionStart, selectionEnd, value } = e.target;
    const selected = value.slice(selectionStart, selectionEnd).trim();
    if (selected) setSelectedText(selected);
  };

  const triggerSilentSave = async (updatedContent) => {
    try {
      await blogApi.update(id, { content: updatedContent, title, metaDescription: metaDesc });
    } catch (err) {
      console.warn('Autosave failed');
    }
  };

  const handleManualSave = async () => {
    try {
      toast.loading('Saving changes...', { id: 'save-act' });
      await blogApi.update(id, { content, title, metaDescription: metaDesc, keywords: seoKeyword });
      toast.dismiss('save-act');
      toast.success('Changes saved successfully!');
    } catch (err) {
      toast.dismiss('save-act');
      toast.error('Failed to save content changes.');
    }
  };

  const handleSaveVersionSnapshot = async () => {
    if (!versionNote.trim()) {
      toast.error('Please enter a short version description note');
      return;
    }
    try {
      await blogApi.saveVersion(id, versionNote);
      setVersionNote('');
      loadVersions();
      toast.success('Version snapshot saved!');
    } catch (err) {
      toast.error('Failed to capture snapshot.');
    }
  };

  // Inline AI Action Calls
  const executeAiAction = async (action) => {
    const textToProcess = selectedText || content.replace(/<[^>]*>/g, '').substring(0, 1000);
    if (!textToProcess.trim()) {
      toast.error('No text selected or available to edit');
      return;
    }

    setAiLoading(true);
    try {
      const res = await aiApi.inlineAction({
        text: textToProcess,
        action,
        keywords: seoKeyword,
        blogId: parseInt(id),
      });
      setAiResult(res.data.message);
      toast.success('AI processing complete!');
    } catch (err) {
      toast.error('AI command execution failed.');
    } finally {
      setAiLoading(false);
    }
  };

  const executeToneRewrite = async () => {
    const textToProcess = selectedText || content.replace(/<[^>]*>/g, '').substring(0, 1000);
    if (!textToProcess.trim()) {
      toast.error('Please highlight or select text to rewrite');
      return;
    }

    setAiLoading(true);
    try {
      const res = await aiApi.rewrite({
        text: textToProcess,
        tone: rewriteTone,
        blogId: parseInt(id),
      });
      setAiResult(res.data.message);
      toast.success('Text rewritten!');
    } catch (err) {
      toast.error('AI tone rewrite failed.');
    } finally {
      setAiLoading(false);
    }
  };

  // Algorithmic SEO Analysis
  const runSeoCheck = async () => {
    setSeoLoading(true);
    try {
      const res = await seoApi.analyze({
        blogId: parseInt(id),
        targetKeyword: seoKeyword,
      });
      setSeoReport(res.data);
      // Update overall blog object
      setBlog((prev) => ({ ...prev, seoScore: res.data.overallScore }));
      toast.success('SEO check complete!');
    } catch (err) {
      toast.error('SEO Analysis failed.');
    } finally {
      setSeoLoading(false);
    }
  };

  // AI Headline Generator
  const generateHeadlines = async () => {
    setHeadlineLoading(true);
    try {
      const res = await headlineApi.generate(id);
      setHeadlines(res.data || []);
      toast.success('Headlines generated!');
    } catch (err) {
      toast.error('AI Headline variants failed.');
    } finally {
      setHeadlineLoading(false);
    }
  };

  const selectHeadline = async (variantId) => {
    try {
      const res = await headlineApi.select(variantId);
      setTitle(res.data.message);
      toast.success('Blog post title updated!');
      loadBlog();
    } catch (err) {
      toast.error('Failed to change title.');
    }
  };

  // Context-aware Q&A Assistant Chat
  const sendChatMsg = async () => {
    if (!chatQuestion.trim()) return;

    const currentMsg = { role: 'user', text: chatQuestion };
    setChatMessages((prev) => [...prev, currentMsg]);
    setChatQuestion('');
    setChatLoading(true);

    try {
      const res = await aiApi.ask({
        blogId: parseInt(id),
        question: chatQuestion,
      });
      setChatMessages((prev) => [...prev, { role: 'ai', text: res.data.message }]);
    } catch (err) {
      setChatMessages((prev) => [...prev, { role: 'system', text: 'Error fetching AI response.' }]);
    } finally {
      setChatLoading(false);
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
      {/* Editor Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography className="page-title">{title}</Typography>
          <Typography variant="body2" color="text.muted">
            Word count: <strong>{blog?.wordCount || 0} words</strong> | SEO Score:{' '}
            <strong style={{ color: blog?.seoScore >= 80 ? '#10b981' : '#fbbf24' }}>
              {blog?.seoScore || 'Not Analyzed'}
            </strong>
          </Typography>
        </Box>
        <Stack direction="row" spacing={2}>
          <Button variant="outlined" onClick={() => navigate('/dashboard')}>
            Exit
          </Button>
          <Button variant="contained" className="btn-brand" startIcon={<SaveIcon />} onClick={handleManualSave}>
            Save Changes
          </Button>
        </Stack>
      </Box>

      <Grid container spacing={3}>
        {/* Editor Main Content Panel */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Card sx={{ p: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>
            <TextField
              fullWidth
              label="Article Title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              variant="outlined"
            />
            <TextField
              fullWidth
              multiline
              rows={2}
              label="Meta Description (Target: 150-160 chars)"
              value={metaDesc}
              onChange={(e) => setMetaDesc(e.target.value)}
              variant="outlined"
            />

            {/* Article Body Editor with Edit/Preview toggle */}
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                  Article Body Content
                </Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    size="small"
                    variant={!previewMode ? 'contained' : 'outlined'}
                    onClick={() => setPreviewMode(false)}
                    sx={{ minWidth: 80, fontSize: '0.75rem' }}
                  >
                    ✏️ Edit
                  </Button>
                  <Button
                    size="small"
                    variant={previewMode ? 'contained' : 'outlined'}
                    onClick={() => setPreviewMode(true)}
                    sx={{ minWidth: 80, fontSize: '0.75rem' }}
                  >
                    👁 Preview
                  </Button>
                </Box>
              </Box>

              {previewMode ? (
                /* Rendered HTML Preview */
                <Box
                  sx={{
                    width: '100%',
                    minHeight: 480,
                    background: '#0b0b0b',
                    border: '1px solid rgba(139, 92, 246, 0.22)',
                    borderRadius: '10px',
                    padding: '24px',
                    boxSizing: 'border-box',
                    overflowY: 'auto',
                    fontFamily: "'Inter', sans-serif",
                    fontSize: '1rem',
                    lineHeight: 1.8,
                    color: '#e2e0f0',
                    '& h1': { color: '#a78bfa', borderBottom: '2px solid #8b5cf6', paddingBottom: '8px', marginBottom: '16px' },
                    '& h2': { color: '#ec4899', borderLeft: '4px solid #8b5cf6', paddingLeft: '8px', marginTop: '24px', marginBottom: '12px' },
                    '& h3': { color: '#10b981', marginTop: '16px', marginBottom: '8px' },
                    '& strong': { color: '#f0eeff', fontWeight: 700 },
                    '& p': { marginBottom: '12px' },
                    '& ul, & ol': { paddingLeft: '24px', marginBottom: '12px' },
                    '& li': { marginBottom: '6px' },
                    '& blockquote': { borderLeft: '3px solid #8b5cf6', paddingLeft: '16px', color: '#a8a3c4', fontStyle: 'italic' },
                  }}
                  dangerouslySetInnerHTML={{ __html: content.replace(/\n/g, '<br/>') }}
                />
              ) : (
                /* Raw Markdown/HTML Editor textarea */
                <textarea
                  value={content}
                  onChange={handleEditorChange}
                  onSelect={handleEditorSelect}
                  placeholder="Start writing your article content here..."
                  style={{
                    width: '100%',
                    minHeight: 480,
                    background: '#0b0b0b',
                    color: '#f0eeff',
                    border: '1px solid rgba(139, 92, 246, 0.22)',
                    borderRadius: 10,
                    padding: '16px',
                    fontFamily: "'Inter', sans-serif",
                    fontSize: '1rem',
                    lineHeight: 1.8,
                    resize: 'vertical',
                    outline: 'none',
                    boxSizing: 'border-box',
                    transition: 'border-color 0.2s',
                  }}
                  onFocus={(e) => { e.target.style.borderColor = 'rgba(139,92,246,0.55)'; }}
                  onBlur={(e) => { e.target.style.borderColor = 'rgba(139,92,246,0.22)'; }}
                />
              )}
            </Box>
          </Card>
        </Grid>

        {/* Sidebar Toolkits Panels */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <Card sx={{ p: 0, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Tabs
              value={tabVal}
              onChange={(e, val) => setTabVal(val)}
              variant="scrollable"
              scrollButtons="auto"
              sx={{ borderBottom: '1px solid rgba(139, 92, 246, 0.12)' }}
            >
              <Tab icon={<SparklesIcon />} label="AI Edit" sx={{ fontSize: '0.75rem' }} />
              <Tab icon={<SeoIcon />} label="SEO" sx={{ fontSize: '0.75rem' }} />
              <Tab icon={<HistoryIcon />} label="History" sx={{ fontSize: '0.75rem' }} />
            </Tabs>

            <Box sx={{ p: 3, flexGrow: 1, maxHeight: 600, overflowY: 'auto' }}>
              {/* Tab 0: AI Inline Actions */}
              {tabVal === 0 && (
                <Stack spacing={3}>
                  <Box>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                      Selected Text Snippet
                    </Typography>
                    <Paper sx={{ p: 2, background: '#1c1c30', minHeight: 60, maxHeight: 150, overflowY: 'auto' }}>
                      <Typography variant="body2" color={selectedText ? 'text.primary' : 'text.disabled'}>
                        {selectedText || 'Highlight text in the editor to run target AI operations.'}
                      </Typography>
                    </Paper>
                  </Box>

                  {/* AI Quick Actions */}
                  <Box>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5 }}>
                      Quick Formatting Actions
                    </Typography>
                    <Grid container spacing={1}>
                      <Grid size={6}>
                        <Button fullWidth size="small" variant="outlined" onClick={() => executeAiAction('expand')}>
                          Expand
                        </Button>
                      </Grid>
                      <Grid size={6}>
                        <Button fullWidth size="small" variant="outlined" onClick={() => executeAiAction('shorten')}>
                          Shorten
                        </Button>
                      </Grid>
                      <Grid size={6}>
                        <Button fullWidth size="small" variant="outlined" onClick={() => executeAiAction('fix_grammar')}>
                          Grammar
                        </Button>
                      </Grid>
                      <Grid size={6}>
                        <Button fullWidth size="small" variant="outlined" onClick={() => executeAiAction('improve_seo')}>
                          SEO Add
                        </Button>
                      </Grid>
                    </Grid>
                  </Box>

                  {/* Tone Rewriting Option */}
                  <Divider />
                  <Stack spacing={1.5}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                      AI Rewrite Engine
                    </Typography>
                    <TextField
                      fullWidth
                      select
                      size="small"
                      label="Rewrite Tone"
                      value={rewriteTone}
                      onChange={(e) => setRewriteTone(e.target.value)}
                    >
                      {TONES.map((t) => (
                        <MenuItem key={t.value} value={t.value}>
                          {t.label}
                        </MenuItem>
                      ))}
                    </TextField>
                    <Button variant="contained" className="btn-brand" size="small" onClick={executeToneRewrite}>
                      Rewrite Text
                    </Button>
                  </Stack>

                  {/* AI Output Result Box */}
                  {aiLoading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                      <CircularProgress size={24} />
                    </Box>
                  ) : aiResult ? (
                    <Box>
                      <Divider sx={{ my: 2 }} />
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                        AI Generation Output
                      </Typography>
                      <Paper sx={{ p: 2, background: '#13131f', border: '1px solid rgba(139, 92, 246, 0.3)' }}>
                        <Typography variant="body2">{aiResult}</Typography>
                        <Button
                          size="small"
                          color="secondary"
                          sx={{ mt: 1, fontWeight: 700 }}
                          onClick={() => {
                            navigator.clipboard.writeText(aiResult);
                            toast.success('Copied output to clipboard!');
                          }}
                        >
                          Copy Output
                        </Button>
                      </Paper>
                    </Box>
                  ) : null}
                </Stack>
              )}

              {/* Tab 1: SEO Analysis Checklist */}
              {tabVal === 1 && (
                <Stack spacing={3}>
                  <Box sx={{ display: 'flex', gap: 2 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Target Keyword"
                      value={seoKeyword}
                      onChange={(e) => setSeoKeyword(e.target.value)}
                    />
                    <Button variant="contained" className="btn-brand" size="small" onClick={runSeoCheck} disabled={seoLoading}>
                      {seoLoading ? <CircularProgress size={20} /> : 'Check'}
                    </Button>
                  </Box>

                  {seoReport ? (
                    <Stack spacing={2}>
                      <Box sx={{ textAlign: 'center', py: 2 }}>
                        <Typography variant="h4" sx={{ fontWeight: 800, color: seoReport.overallScore >= 80 ? '#10b981' : '#fbbf24' }}>
                          {seoReport.overallScore} / 100
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Calculated SEO Score
                        </Typography>
                      </Box>

                      {/* Scores Breakdown */}
                      <Stack spacing={1}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="caption">Word Length</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700 }}>{seoReport.contentLengthScore}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="caption">Meta Tags</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700 }}>{seoReport.metaDescriptionScore}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="caption">Keyword Density</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700 }}>{seoReport.keywordDensityScore}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="caption">Readability</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700 }}>{seoReport.readabilityScore}</Typography>
                        </Box>
                      </Stack>

                      <Divider />
                      <Box>
                        <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                          Recommendations
                        </Typography>
                        <ul style={{ paddingLeft: 18, fontSize: '0.82rem', color: '#a8a3c4', lineHeight: 1.6 }}>
                          {seoReport.recommendations.split('\n').map((rec, i) => (
                            <li key={i}>{rec}</li>
                          ))}
                        </ul>
                      </Box>
                    </Stack>
                  ) : (
                    <Typography variant="body2" color="text.secondary" align="center">
                      Configure keyword to run SEO quality checklist checks.
                    </Typography>
                  )}
                  
                  {/* Headline Generation Section */}
                  <Divider />
                  <Stack spacing={2}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                      Headline Alternatives
                    </Typography>
                    <Button variant="outlined" size="small" onClick={generateHeadlines} disabled={headlineLoading}>
                      {headlineLoading ? <CircularProgress size={20} /> : 'Generate Headlines'}
                    </Button>
                    <List dense>
                      {headlines.map((hl) => (
                        <ListItem
                          key={hl.id}
                          sx={{
                            borderBottom: '1px solid rgba(255,255,255,0.05)',
                            px: 1,
                            cursor: 'pointer',
                            '&:hover': { background: 'rgba(255,255,255,0.02)' }
                          }}
                          onClick={() => selectHeadline(hl.id)}
                        >
                          <ListItemText
                            primary={hl.headline}
                            secondary={<Chip label={hl.variant} size="small" sx={{ height: 16, fontSize: '0.62rem', mt: 0.5 }} />}
                          />
                        </ListItem>
                      ))}
                    </List>
                  </Stack>
                </Stack>
              )}

              {/* Tab 2: History Snapshots */}
              {tabVal === 2 && (
                <Stack spacing={3}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Snapshot Description"
                      placeholder="e.g. Added introduction section"
                      value={versionNote}
                      onChange={(e) => setVersionNote(e.target.value)}
                    />
                    <Button variant="outlined" size="small" onClick={handleSaveVersionSnapshot}>
                      Save Version Snapshot
                    </Button>
                  </Box>

                  <Divider />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    Saved Versions
                  </Typography>
                  {versions.length === 0 ? (
                    <Typography variant="body2" color="text.secondary" align="center">
                      No snapshots captured yet.
                    </Typography>
                  ) : (
                    <List dense>
                      {versions.map((ver) => (
                        <ListItem
                          key={ver.id}
                          secondaryAction={
                            <Button
                              size="small"
                              onClick={() => {
                                setContent(ver.content);
                                setTitle(ver.title);
                                toast.success(`Version ${ver.versionNumber} loaded!`);
                              }}
                            >
                              Restore
                            </Button>
                          }
                          sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', px: 0 }}
                        >
                          <ListItemText
                            primary={`Version ${ver.versionNumber}`}
                            secondary={ver.changeNote || 'No description'}
                          />
                        </ListItem>
                      ))}
                    </List>
                  )}
                </Stack>
              )}
            </Box>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default BlogEditor;
