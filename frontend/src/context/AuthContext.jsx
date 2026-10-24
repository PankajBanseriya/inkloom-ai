import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { MOCK_BRANDS, MOCK_TEAM_MEMBERS } from '../mock/mockData';
import { authService } from '../services/authService';
import { apiClient } from '../services/apiClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('inkloom_user');
    if (saved) {
      try { return JSON.parse(saved); } catch { /* ignore */ }
    }
    return {
      id: 'user-02',
      name: 'Sarah Jenkins',
      email: 'sarah.jenkins@acme.internal',
      role: 'REVIEWER',
      token: localStorage.getItem('inkloom_token') || 'demo-jwt-token-reviewer',
    };
  });

  const [activeBrand, setActiveBrand] = useState(() => {
    const savedBrand = localStorage.getItem('inkloom_active_brand');
    if (savedBrand) {
      try { return JSON.parse(savedBrand); } catch { /* ignore */ }
    }
    return MOCK_BRANDS[0];
  });

  const logout = useCallback(() => {
    authService.logout();
    setUser(null);
    localStorage.removeItem('inkloom_token');
    localStorage.removeItem('inkloom_user');
  }, []);

  // Listen to 401 unauthorized session expiry events from apiClient
  useEffect(() => {
    const handleUnauthorized = () => {
      logout();
    };

    window.addEventListener('inkloom_unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('inkloom_unauthorized', handleUnauthorized);
    };
  }, [logout]);

  // Synchronize user to localStorage
  useEffect(() => {
    if (user) {
      localStorage.setItem('inkloom_user', JSON.stringify(user));
      if (user.token) {
        apiClient.setToken(user.token);
      }
    } else {
      localStorage.removeItem('inkloom_user');
    }
  }, [user]);

  useEffect(() => {
    if (activeBrand) {
      localStorage.setItem('inkloom_active_brand', JSON.stringify(activeBrand));
    }
  }, [activeBrand]);

  const login = async (email, password, roleFallback = 'REVIEWER') => {
    try {
      const data = await authService.login(email, password);
      const authenticatedUser = {
        id: data.user.id,
        name: data.user.fullName,
        email: data.user.email,
        role: data.user.role,
        workspaceIds: data.user.workspaceIds,
        token: data.token,
      };
      setUser(authenticatedUser);
      return authenticatedUser;
    } catch (err) {
      // In offline / local mock demo mode, fallback smoothly if backend unreachable
      if (err.message && err.message.includes('Failed to fetch')) {
        const matched = MOCK_TEAM_MEMBERS.find((m) => m.email.toLowerCase() === email.toLowerCase());
        const fallbackUser = {
          id: matched ? matched.id : 'user-custom',
          name: matched ? matched.name : email.split('@')[0],
          email,
          role: matched ? matched.role : roleFallback,
          token: 'demo-jwt-token-' + Date.now(),
        };
        setUser(fallbackUser);
        return fallbackUser;
      }
      throw err;
    }
  };

  const register = async (fullName, email, password, role = 'CONTENT_CREATOR', workspaceName = '') => {
    try {
      const data = await authService.register({ fullName, email, password, role, workspaceName });
      const authenticatedUser = {
        id: data.user.id,
        name: data.user.fullName,
        email: data.user.email,
        role: data.user.role,
        workspaceIds: data.user.workspaceIds,
        token: data.token,
      };
      setUser(authenticatedUser);
      return authenticatedUser;
    } catch (err) {
      if (err.message && err.message.includes('Failed to fetch')) {
        const fallbackUser = {
          id: 'user-' + Date.now().toString(36),
          name: fullName,
          email,
          role,
          token: 'demo-jwt-token-new-' + Date.now(),
        };
        setUser(fallbackUser);
        return fallbackUser;
      }
      throw err;
    }
  };

  const switchBrand = (brandId) => {
    const found = MOCK_BRANDS.find((b) => b.id === brandId);
    if (found) {
      setActiveBrand(found);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        activeBrand,
        brands: MOCK_BRANDS,
        login,
        register,
        logout,
        switchBrand,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
