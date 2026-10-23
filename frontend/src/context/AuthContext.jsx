import { createContext, useContext, useState, useEffect } from 'react';
import { MOCK_BRANDS, MOCK_TEAM_MEMBERS } from '../mock/mockData';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // Initial default authenticated user for Phase 2 frontend experience
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
      token: 'mock-jwt-token-dev-sarah',
    };
  });

  const [activeBrand, setActiveBrand] = useState(() => {
    const savedBrand = localStorage.getItem('inkloom_active_brand');
    if (savedBrand) {
      try { return JSON.parse(savedBrand); } catch { /* ignore */ }
    }
    return MOCK_BRANDS[0];
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem('inkloom_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('inkloom_user');
    }
  }, [user]);

  useEffect(() => {
    if (activeBrand) {
      localStorage.setItem('inkloom_active_brand', JSON.stringify(activeBrand));
    }
  }, [activeBrand]);

  const login = (email, password, role = 'REVIEWER') => {
    // In Phase 2 frontend, provides realistic interactive authentication
    const matched = MOCK_TEAM_MEMBERS.find((m) => m.email.toLowerCase() === email.toLowerCase());
    const newUser = {
      id: matched ? matched.id : 'user-custom',
      name: matched ? matched.name : email.split('@')[0],
      email,
      role: matched ? matched.role : role,
      token: 'mock-jwt-token-' + Date.now(),
    };
    setUser(newUser);
    return newUser;
  };

  const register = (fullName, email, _password, role = 'CONTENT_CREATOR') => {
    const newUser = {
      id: 'user-' + Date.now().toString(36),
      name: fullName,
      email,
      role,
      token: 'mock-jwt-token-new-' + Date.now(),
    };
    setUser(newUser);
    return newUser;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('inkloom_token');
    localStorage.removeItem('inkloom_user');
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
