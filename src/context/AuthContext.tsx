import {
  ReactNode,
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react';
import { authenticate } from '../services/api';

type User = {
  id: string;
  name: string;
  role: 'creator' | 'approver' | 'admin';
  token: string;
};

type AuthContextValue = {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const storageKey = 'aitrimmer-auth';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const stored = localStorage.getItem(storageKey);
    if (stored) {
      try {
        setUser(JSON.parse(stored));
      } catch (error) {
        console.error('Failed to parse auth user', error);
      }
    }
  }, []);

  useEffect(() => {
    if (user) {
      localStorage.setItem(storageKey, JSON.stringify(user));
    } else {
      localStorage.removeItem(storageKey);
    }
  }, [user]);

  const login = async (username: string, password: string) => {
    const authenticated = await authenticate({ username, password });
    setUser(authenticated);
  };

  const logout = () => setUser(null);

  const value = useMemo(() => ({ user, login, logout }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
};
