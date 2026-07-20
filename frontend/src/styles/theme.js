import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#8b5cf6',
      light: '#a78bfa',
      dark: '#6d28d9',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#10b981',
      light: '#34d399',
      dark: '#059669',
      contrastText: '#ffffff',
    },
    error: {
      main: '#ef4444',
      light: '#f87171',
    },
    warning: {
      main: '#f59e0b',
      light: '#fbbf24',
    },
    success: {
      main: '#10b981',
      light: '#34d399',
    },
    background: {
      default: '#000000',
      paper: '#080808',
    },
    text: {
      primary: '#f0eeff',
      secondary: '#a8a3c4',
      disabled: '#3d3a5c',
    },
    divider: 'rgba(139, 92, 246, 0.12)',
  },

  typography: {
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
    h1: { fontFamily: "'Sora', 'Inter', sans-serif", fontWeight: 800 },
    h2: { fontFamily: "'Sora', 'Inter', sans-serif", fontWeight: 700 },
    h3: { fontFamily: "'Sora', 'Inter', sans-serif", fontWeight: 700 },
    h4: { fontFamily: "'Sora', 'Inter', sans-serif", fontWeight: 600 },
    h5: { fontFamily: "'Sora', 'Inter', sans-serif", fontWeight: 600 },
    h6: { fontFamily: "'Sora', 'Inter', sans-serif", fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },

  shape: {
    borderRadius: 10,
  },

  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          paddingTop: 10,
          paddingBottom: 10,
          fontSize: '0.9rem',
          transition: 'all 0.2s ease',
        },
        contained: {
          background: 'linear-gradient(135deg, #7c3aed 0%, #8b5cf6 50%, #a78bfa 100%)',
          boxShadow: '0 0 24px rgba(139, 92, 246, 0.30)',
          '&:hover': {
            background: 'linear-gradient(135deg, #6d28d9 0%, #7c3aed 50%, #8b5cf6 100%)',
            boxShadow: '0 0 32px rgba(139, 92, 246, 0.45)',
            transform: 'translateY(-1px)',
          },
        },
        outlined: {
          borderColor: 'rgba(139, 92, 246, 0.40)',
          color: '#a78bfa',
          '&:hover': {
            borderColor: '#8b5cf6',
            background: 'rgba(139, 92, 246, 0.08)',
          },
        },
      },
    },

    MuiCard: {
      styleOverrides: {
        root: {
          background: '#080808',
          border: '1px solid rgba(139, 92, 246, 0.12)',
          boxShadow: '0 4px 24px rgba(0,0,0,0.40)',
          borderRadius: 16,
          transition: 'border-color 0.25s ease, box-shadow 0.25s ease',
          '&:hover': {
            borderColor: 'rgba(139, 92, 246, 0.25)',
          },
        },
      },
    },

    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            background: '#101010',
            borderRadius: 10,
            '& fieldset': { borderColor: 'rgba(139, 92, 246, 0.22)' },
            '&:hover fieldset': { borderColor: 'rgba(139, 92, 246, 0.40)' },
            '&.Mui-focused fieldset': { borderColor: '#8b5cf6' },
          },
        },
      },
    },

    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          fontSize: '0.78rem',
          fontWeight: 600,
        },
      },
    },

    MuiDialog: {
      styleOverrides: {
        paper: {
          background: '#080808',
          border: '1px solid rgba(139, 92, 246, 0.22)',
          borderRadius: 20,
        },
      },
    },

    MuiDrawer: {
      styleOverrides: {
        paper: {
          background: '#050505',
          borderRight: '1px solid rgba(139, 92, 246, 0.12)',
        },
      },
    },

    MuiAppBar: {
      styleOverrides: {
        root: {
          background: 'rgba(5, 5, 5, 0.85)',
          backdropFilter: 'blur(12px)',
          borderBottom: '1px solid rgba(139, 92, 246, 0.12)',
          boxShadow: 'none',
        },
      },
    },

    MuiMenu: {
      styleOverrides: {
        paper: {
          background: '#101010',
          border: '1px solid rgba(139, 92, 246, 0.22)',
          borderRadius: 12,
          boxShadow: '0 8px 32px rgba(0,0,0,0.50)',
        },
      },
    },

    MuiMenuItem: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          margin: '2px 6px',
          '&:hover': { background: 'rgba(139, 92, 246, 0.12)' },
          '&.Mui-selected': { background: 'rgba(139, 92, 246, 0.20)' },
        },
      },
    },

    MuiLinearProgress: {
      styleOverrides: {
        root: { borderRadius: 99, background: 'rgba(139, 92, 246, 0.12)' },
        bar: { background: 'linear-gradient(90deg, #7c3aed, #8b5cf6)' },
      },
    },

    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          background: '#181818',
          border: '1px solid rgba(139, 92, 246, 0.22)',
          borderRadius: 8,
          fontSize: '0.8rem',
        },
      },
    },

    MuiSkeleton: {
      styleOverrides: {
        root: { background: 'rgba(139, 92, 246, 0.08)' },
      },
    },

    MuiDivider: {
      styleOverrides: {
        root: { borderColor: 'rgba(139, 92, 246, 0.12)' },
      },
    },

    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          margin: '2px 8px',
          '&:hover': { background: 'rgba(139, 92, 246, 0.10)' },
          '&.Mui-selected': {
            background: 'rgba(139, 92, 246, 0.18)',
            borderLeft: '3px solid #8b5cf6',
            '&:hover': { background: 'rgba(139, 92, 246, 0.22)' },
          },
        },
      },
    },
  },
});
