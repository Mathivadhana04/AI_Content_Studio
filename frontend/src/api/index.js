import api from './client';

export const authApi = {
  register: (data) => api.post('/api/v1/auth/register', data),
  login: (data) => api.post('/api/v1/auth/login', data),
  refresh: (refreshToken) => api.post('/api/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken) => api.post('/api/v1/auth/logout', { refreshToken }),
};

export const userApi = {
  getMe: () => api.get('/api/v1/users/me'),
  updateMe: (data) => api.put('/api/v1/users/me', data),
  changePassword: (data) => api.put('/api/v1/users/me/password', data),
  listUsers: (params) => api.get('/api/v1/users', { params }),
};

export const workspaceApi = {
  create: (data) => api.post('/api/v1/workspaces', data),
  getAll: (params) => api.get('/api/v1/workspaces', { params }),
  getById: (id) => api.get(`/api/v1/workspaces/${id}`),
  update: (id, data) => api.put(`/api/v1/workspaces/${id}`, data),
  delete: (id) => api.delete(`/api/v1/workspaces/${id}`),
  invite: (id, data) => api.post(`/api/v1/workspaces/${id}/members/invite`, data),
  getMembers: (id) => api.get(`/api/v1/workspaces/${id}/members`),
  removeMember: (workspaceId, userId) => api.delete(`/api/v1/workspaces/${workspaceId}/members/${userId}`),
  acceptInvitation: (workspaceId) => api.post(`/api/v1/workspaces/${workspaceId}/members/accept`),
};

export const blogApi = {
  generate: (data) => api.post('/api/v1/blogs/generate', data),
  createDraft: (data, workspaceId) => api.post(`/api/v1/blogs/draft?workspaceId=${workspaceId}`, data),
  getById: (id) => api.get(`/api/v1/blogs/${id}`),
  getMyBlogs: (params) => api.get('/api/v1/blogs/my', { params }),
  getWorkspaceBlogs: (workspaceId, params) => api.get(`/api/v1/blogs/workspace/${workspaceId}`, { params }),
  search: (workspaceId, query, params) =>
    api.get(`/api/v1/blogs/workspace/${workspaceId}/search?query=${query}`, { params }),
  update: (id, data) => api.put(`/api/v1/blogs/${id}`, data),
  publish: (id) => api.post(`/api/v1/blogs/${id}/publish`),
  delete: (id) => api.delete(`/api/v1/blogs/${id}`),
  duplicate: (id) => api.post(`/api/v1/blogs/${id}/duplicate`),
  saveVersion: (id, changeNote) => api.post(`/api/v1/blogs/${id}/version?changeNote=${changeNote || ''}`),
  getVersions: (id) => api.get(`/api/v1/blogs/${id}/versions`),
};

export const aiApi = {
  rewrite: (data) => api.post('/api/v1/ai/rewrite', data),
  inlineAction: (data) => api.post('/api/v1/ai/action', data),
  ask: (data) => api.post('/api/v1/ai/ask', data),
  chatbot: (data) => api.post('/api/v1/ai/chatbot', data),
  adCopy: (data) => api.post('/api/v1/ai/ad-copy', data),
  detect: (data) => api.post('/api/v1/ai/detect', data),
};

export const seoApi = {
  analyze: (data) => api.post('/api/v1/seo/analyze', data),
  getReport: (blogId) => api.get(`/api/v1/seo/report/${blogId}`),
  analyzeText: (data) => api.post('/api/v1/seo/analyze-text', data),
};

export const headlineApi = {
  generate: (blogId) => api.post(`/api/v1/headlines/generate/${blogId}`),
  getAll: (blogId) => api.get(`/api/v1/headlines/${blogId}`),
  select: (variantId) => api.put(`/api/v1/headlines/select/${variantId}`),
};

export const schedulerApi = {
  schedule: (blogId, data) => api.post(`/api/v1/scheduler/schedule/${blogId}`, data),
  reschedule: (blogId, data) => api.put(`/api/v1/scheduler/reschedule/${blogId}`, data),
  cancel: (blogId) => api.delete(`/api/v1/scheduler/cancel/${blogId}`),
  getScheduled: (params) => api.get('/api/v1/scheduler/scheduled', { params }),
};

export const socialApi = {
  generate: (data) => api.post('/api/v1/social/generate', data),
  getMyPosts: (params) => api.get('/api/v1/social/my', { params }),
  getBlogPosts: (blogId) => api.get(`/api/v1/social/blog/${blogId}`),
  delete: (id) => api.delete(`/api/v1/social/${id}`),
};

export const emailApi = {
  generate: (data) => api.post('/api/v1/email/generate', data),
  getMyCampaigns: (params) => api.get('/api/v1/email/my', { params }),
  delete: (id) => api.delete(`/api/v1/email/${id}`),
};

export const collaborationApi = {
  addComment: (blogId, data) => api.post(`/api/v1/blogs/${blogId}/comments`, data),
  getComments: (blogId, params) => api.get(`/api/v1/blogs/${blogId}/comments`, { params }),
  deleteComment: (blogId, commentId) => api.delete(`/api/v1/blogs/${blogId}/comments/${commentId}`),
  resolveComment: (blogId, commentId) => api.put(`/api/v1/blogs/${blogId}/comments/${commentId}/resolve`),
  getActivityLog: (blogId, params) => api.get(`/api/v1/blogs/${blogId}/activity`, { params }),
};

export const notificationApi = {
  getAll: (params) => api.get('/api/v1/notifications', { params }),
  getUnreadCount: () => api.get('/api/v1/notifications/unread-count'),
  markAsRead: (id) => api.put(`/api/v1/notifications/${id}/read`),
  markAllAsRead: () => api.put('/api/v1/notifications/read-all'),
  delete: (id) => api.delete(`/api/v1/notifications/${id}`),
};

export const analyticsApi = {
  getSummary: () => api.get('/api/v1/analytics/summary'),
  getAdminSummary: () => api.get('/api/v1/analytics/admin/summary'),
};
