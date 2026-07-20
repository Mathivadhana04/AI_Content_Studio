import React, { useState, useEffect, useRef } from 'react';
import { aiApi, blogApi, socialApi, emailApi } from '../api';
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
  Paper,
  Avatar,
  FormControlLabel,
  Switch,
} from '@mui/material';
import {
  Send as SendIcon,
  SmartToy as RobotIcon,
  Person as PersonIcon,
  AutoAwesome as SparklesIcon,
  Layers as LayersIcon,
  AutoAwesome as SparkleBadgeIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';

const ChatbotPage = () => {
  const [messages, setMessages] = useState([
    {
      role: 'ai',
      text: "Hello! I am your AI Workspace Consultant. Bind me to any generated Blog, Social Post, or Email Campaign from the menu below to rewrite it in-place, analyze its SEO score, or check how well it will trend. Or ask me general questions!",
    },
  ]);
  const [inputMsg, setInputMsg] = useState('');
  const [loading, setLoading] = useState(false);

  // Context Selection
  const [contextType, setContextType] = useState('NONE'); // NONE, BLOG, SOCIAL, EMAIL
  const [availableItems, setAvailableItems] = useState([]);
  const [selectedContextId, setSelectedContextId] = useState('');
  const [loadingItems, setLoadingItems] = useState(false);

  const messagesEndRef = useRef(null);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (contextType !== 'NONE') {
      loadContextItems();
    } else {
      setAvailableItems([]);
      setSelectedContextId('');
    }
  }, [contextType]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadContextItems = async () => {
    setLoadingItems(true);
    const wsId = localStorage.getItem('currentWorkspaceId');
    try {
      if (contextType === 'BLOG') {
        const res = wsId
          ? await blogApi.getWorkspaceBlogs(wsId, { page: 0, size: 20 })
          : await blogApi.getMyBlogs({ page: 0, size: 20 });
        setAvailableItems(res.data.content || []);
      } else if (contextType === 'SOCIAL') {
        const res = await socialApi.getMyPosts({ page: 0, size: 20 });
        setAvailableItems(res.data.content || []);
      } else if (contextType === 'EMAIL') {
        const res = await emailApi.getMyCampaigns({ page: 0, size: 20 });
        setAvailableItems(res.data.content || []);
      }
    } catch (err) {
      toast.error('Failed to load context items.');
    } finally {
      setLoadingItems(false);
    }
  };

  const handleSend = async () => {
    if (!inputMsg.trim()) return;

    const userMessage = { role: 'user', text: inputMsg };
    setMessages((prev) => [...prev, userMessage]);
    setInputMsg('');
    setLoading(true);

    try {
      const payload = {
        message: inputMsg,
        contextType: contextType,
        contextId: selectedContextId ? parseInt(selectedContextId) : null,
      };

      const res = await aiApi.chatbot(payload);
      const data = res.data;

      let responseText = data.reply || '';
      if (data.updatedContent && data.updatedContent !== 'null' && data.updatedContent.trim() !== '') {
        if (!responseText.includes(data.updatedContent.trim())) {
          responseText += (responseText ? '\n\n**Rewritten / Updated Content:**\n' : '') + data.updatedContent;
        }
      }

      // Add conversational reply with rewritten content to chat stream
      setMessages((prev) => [...prev, { role: 'ai', text: responseText }]);

      // Notify if database content was modified in-place
      if (data.contentUpdated) {
        toast.success('Asset modified in-place successfully!');
      }
    } catch (err) {
      console.error(err);
      setMessages((prev) => [
        ...prev,
        { role: 'ai', text: 'Error: Failed to fetch AI response. Please try again.' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const getContextLabel = (item) => {
    if (contextType === 'BLOG') return item.title;
    if (contextType === 'SOCIAL') return `[${item.platform}] ${item.content.substring(0, 40)}...`;
    if (contextType === 'EMAIL') return `[${item.emailType}] ${item.subject}`;
    return '';
  };

  return (
    <Box className="page-container" sx={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 120px)' }}>
      {/* Header */}
      <Box className="page-header" sx={{ mb: 2 }}>
        <Typography className="page-title text-gradient">AI Workspace Chatbot</Typography>
        <Typography className="page-subtitle">Broad-minded Q&A consultant capable of editing and rewriting content in-place.</Typography>
      </Box>

      <Grid container spacing={3} sx={{ flexGrow: 1, minHeight: 0 }}>
        {/* Context Controls Sidebar */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Card className="card-neon" sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column', gap: 3 }}>
            <Typography variant="h6" sx={{ fontFamily: 'Sora', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 1 }}>
              <LayersIcon sx={{ color: '#8b5cf6' }} /> Chat Context
            </Typography>

            <TextField
              fullWidth
              select
              label="Select Content Context"
              value={contextType}
              onChange={(e) => setContextType(e.target.value)}
            >
              <MenuItem value="NONE">General AI Assistant</MenuItem>
              <MenuItem value="BLOG">Blog Post Article</MenuItem>
              <MenuItem value="SOCIAL">Social Media Post</MenuItem>
              <MenuItem value="EMAIL">Email Campaign</MenuItem>
            </TextField>

            {contextType !== 'NONE' && (
              <>
                {loadingItems ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                    <CircularProgress size={24} />
                  </Box>
                ) : (
                  <TextField
                    fullWidth
                    select
                    label={`Choose ${contextType.charAt(0) + contextType.slice(1).toLowerCase()}`}
                    value={selectedContextId}
                    onChange={(e) => setSelectedContextId(e.target.value)}
                    disabled={availableItems.length === 0}
                  >
                    {availableItems.map((item) => (
                      <MenuItem key={item.id} value={item.id}>
                        {getContextLabel(item)}
                      </MenuItem>
                    ))}
                  </TextField>
                )}

                {availableItems.length === 0 && !loadingItems && (
                  <Typography variant="caption" color="text.muted" align="center">
                    No items generated yet in this workspace context.
                  </Typography>
                )}
              </>
            )}

            <Divider sx={{ my: 1 }} />

            <Box sx={{ background: 'rgba(139, 92, 246, 0.04)', p: 2, borderRadius: 3, border: '1px solid rgba(139, 92, 246, 0.11)' }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#a8a3c4', mb: 1, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <SparkleBadgeIcon sx={{ fontSize: 16, color: '#fbbf24' }} /> Smart capabilities:
              </Typography>
              <Typography variant="caption" color="text.muted" component="p" sx={{ mb: 1 }}>
                • Ask: "How well will this content trend on LinkedIn?"
              </Typography>
              <Typography variant="caption" color="text.muted" component="p" sx={{ mb: 1 }}>
                • Ask: "Identify SEO improvements for this article."
              </Typography>
              <Typography variant="caption" color="text.muted" component="p">
                • Command: "Rewrite the introduction to sound more energetic." (AI updates it in the database in-place!)
              </Typography>
            </Box>
          </Card>
        </Grid>

        {/* Chat Stream Panel */}
        <Grid size={{ xs: 12, md: 8 }} sx={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
          <Card className="card-neon" sx={{ p: 0, flexGrow: 1, display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
            {/* Scrollable messages list */}
            <Box sx={{ flexGrow: 1, overflowY: 'auto', p: 3, display: 'flex', flexDirection: 'column', gap: 2 }}>
              {messages.map((msg, i) => (
                <Box
                  key={i}
                  sx={{
                    display: 'flex',
                    alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
                    maxWidth: '80%',
                    gap: 1.5,
                  }}
                >
                  {msg.role === 'ai' && (
                    <Avatar sx={{ bgcolor: '#8b5cf6', width: 36, height: 36 }}>
                      <RobotIcon sx={{ fontSize: 20 }} />
                    </Avatar>
                  )}
                  <Paper
                    sx={{
                      p: 2,
                      background: msg.role === 'user' ? 'linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%)' : '#141424',
                      border: msg.role === 'user' ? 'none' : '1px solid rgba(139, 92, 246, 0.15)',
                      borderRadius: msg.role === 'user' ? '16px 16px 2px 16px' : '16px 16px 16px 2px',
                      color: '#f0eeff',
                    }}
                  >
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-line', lineHeight: 1.6 }}>
                      {msg.text}
                    </Typography>
                  </Paper>
                  {msg.role === 'user' && (
                    <Avatar sx={{ bgcolor: '#10b981', width: 36, height: 36 }}>
                      <PersonIcon sx={{ fontSize: 20 }} />
                    </Avatar>
                  )}
                </Box>
              ))}
              {loading && (
                <Box sx={{ display: 'flex', gap: 1.5, alignSelf: 'flex-start' }}>
                  <Avatar sx={{ bgcolor: '#8b5cf6', width: 36, height: 36 }}>
                    <RobotIcon sx={{ fontSize: 20 }} />
                  </Avatar>
                  <Paper sx={{ p: 2, background: '#141424', border: '1px solid rgba(139, 92, 246, 0.15)', borderRadius: '16px 16px 16px 2px', display: 'flex', alignItems: 'center', gap: 1 }}>
                    <CircularProgress size={16} sx={{ color: '#8b5cf6' }} />
                    <Typography variant="caption" color="text.secondary">Thinking...</Typography>
                  </Paper>
                </Box>
              )}
              <div ref={messagesEndRef} />
            </Box>

            {/* Input Bar */}
            <Divider sx={{ borderColor: 'rgba(139, 92, 246, 0.12)' }} />
            <Box sx={{ p: 2, display: 'flex', gap: 1.5 }}>
              <TextField
                fullWidth
                placeholder={contextType !== 'NONE' && selectedContextId ? `Ask question or edit context asset...` : `Ask general questions...`}
                value={inputMsg}
                onChange={(e) => setInputMsg(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                disabled={loading || (contextType !== 'NONE' && !selectedContextId)}
              />
              <IconButton
                color="primary"
                onClick={handleSend}
                disabled={loading || !inputMsg.trim() || (contextType !== 'NONE' && !selectedContextId)}
                sx={{
                  bgcolor: '#8b5cf6',
                  color: 'white',
                  borderRadius: 3,
                  p: 1.5,
                  '&:hover': { bgcolor: '#7c3aed' },
                  '&.Mui-disabled': { bgcolor: 'rgba(255,255,255,0.05)', color: 'rgba(255,255,255,0.3)' },
                }}
              >
                <SendIcon />
              </IconButton>
            </Box>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ChatbotPage;
