import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';
import { notificationApi } from '../api';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import toast from 'react-hot-toast';

const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
  const { user, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Fetch initial notifications
  useEffect(() => {
    if (isAuthenticated) {
      loadNotifications();
      fetchUnreadCount();
    } else {
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [isAuthenticated]);

  const loadNotifications = async () => {
    try {
      const res = await notificationApi.getAll();
      setNotifications(res.data.content || []);
    } catch (err) {
      console.error('Failed to load notifications', err);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationApi.getUnreadCount();
      setUnreadCount(res.data || 0);
    } catch (err) {
      console.error('Failed to load unread count', err);
    }
  };

  // WebSocket / STOMP Integration
  useEffect(() => {
    if (!isAuthenticated || !user) return;

    let wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';
    if (wsUrl.startsWith('ws://')) {
      wsUrl = wsUrl.replace('ws://', 'http://');
    } else if (wsUrl.startsWith('wss://')) {
      wsUrl = wsUrl.replace('wss://', 'https://');
    }
    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      debug: (str) => console.log(str),
    });

    client.onConnect = () => {
      console.log('WebSocket Connected successfully');

      // Subscribe to user-specific notifications queue
      client.subscribe(`/user/queue/notifications`, (message) => {
        const notification = JSON.parse(message.body);
        console.log('Received WebSocket notification: ', notification);

        // Prepend new notification to state
        setNotifications((prev) => [notification, ...prev]);
        setUnreadCount((prev) => prev + 1);

        // Display toast to user
        toast(
          (t) => (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <span style={{ fontWeight: 600 }}>{notification.title}</span>
              <span style={{ fontSize: '0.85rem', color: '#a8a3c4' }}>{notification.message}</span>
            </div>
          ),
          {
            icon: '🔔',
            duration: 5000,
          }
        );
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [isAuthenticated, user]);

  const markAsRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read', err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
      toast.success('All notifications marked as read.');
    } catch (err) {
      console.error('Failed to mark all as read', err);
    }
  };

  const deleteNotification = async (id) => {
    try {
      await notificationApi.delete(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      // Re-fetch unread count to be fully accurate
      fetchUnreadCount();
    } catch (err) {
      console.error('Failed to delete notification', err);
    }
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        markAsRead,
        markAllAsRead,
        deleteNotification,
        refresh: loadNotifications,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => useContext(NotificationContext);
