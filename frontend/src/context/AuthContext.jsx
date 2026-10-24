import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/authService';
import { apiClient } from '../services/apiClient';
import brandService from '../services/brandService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('inkloom_user');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.token) {
          apiClient.setToken(parsed.token);
          return parsed;
        }
      } catch {
        /* ignore */
      }
    }
    return null;
  });

  const [brands, setBrands] = useState([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(false);

  const [activeBrand, setActiveBrand] = useState(() => {
    const savedBrand = localStorage.getItem('inkloom_active_brand');
    if (savedBrand) {
      try {
        return JSON.parse(savedBrand);
      } catch {
        /* ignore */
      }
    }
    return null;
  });

  const logout = useCallback(() => {
    authService.logout();
    apiClient.setToken(null);
    localStorage.removeItem('inkloom_token');
    localStorage.removeItem('inkloom_user');
    localStorage.removeItem('inkloom_active_brand');
    setUser(null);
    setBrands([]);
    setActiveBrand(null);
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
      localStorage.removeItem('inkloom_token');
      apiClient.setToken(null);
    }
  }, [user]);

  // Synchronize activeBrand to localStorage
  useEffect(() => {
    if (activeBrand) {
      localStorage.setItem('inkloom_active_brand', JSON.stringify(activeBrand));
    } else {
      localStorage.removeItem('inkloom_active_brand');
    }
  }, [activeBrand]);

  // Fetch workspaces for current user from backend
  const fetchBrands = useCallback(async () => {
    if (!user) {
      setBrands([]);
      return;
    }
    try {
      setIsLoadingBrands(true);
      const list = await brandService.getBrands();
      const loadedBrands = list || [];
      setBrands(loadedBrands);

      if (loadedBrands.length > 0) {
        setActiveBrand((current) => {
          if (!current || !loadedBrands.some((b) => b.id === current.id)) {
            return loadedBrands[0];
          }
          return current;
        });
      }
    } catch (err) {
      console.warn('Failed to load workspaces from backend:', err.message);
    } finally {
      setIsLoadingBrands(false);
    }
  }, [user]);

  useEffect(() => {
    fetchBrands();
  }, [fetchBrands]);

  const login = async (email, password) => {
    const data = await authService.login(email, password);
    const authenticatedUser = {
      id: data.user.id,
      name: data.user.fullName,
      email: data.user.email,
      role: data.user.role,
      workspaceIds: data.user.workspaceIds || [],
      token: data.token,
    };
    apiClient.setToken(data.token);
    localStorage.setItem('inkloom_user', JSON.stringify(authenticatedUser));
    setUser(authenticatedUser);
    return authenticatedUser;
  };

  const register = async (fullName, email, password, role = 'CONTENT_CREATOR', workspaceName = '') => {
    const data = await authService.register({ fullName, email, password, role, workspaceName });
    const authenticatedUser = {
      id: data.user.id,
      name: data.user.fullName,
      email: data.user.email,
      role: data.user.role,
      workspaceIds: data.user.workspaceIds || [],
      token: data.token,
    };
    apiClient.setToken(data.token);
    localStorage.setItem('inkloom_user', JSON.stringify(authenticatedUser));
    setUser(authenticatedUser);
    return authenticatedUser;
  };

  const switchBrand = (brandId) => {
    const found = brands.find((b) => b.id === brandId);
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
        brands,
        isLoadingBrands,
        refreshBrands: fetchBrands,
        login,
        register,
        logout,
        switchBrand,
        setActiveBrand,
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
