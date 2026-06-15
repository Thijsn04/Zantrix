import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { Lock } from 'lucide-react';
import { useAuth } from 'react-oidc-context';

interface InactivityContextType {
  isLocked: boolean;
  unlock: () => void;
}

const InactivityContext = createContext<InactivityContextType | null>(null);

export const useInactivity = () => {
  const context = useContext(InactivityContext);
  if (!context) throw new Error('useInactivity must be used within InactivityProvider');
  return context;
};

export const InactivityProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const auth = useAuth();
  const [isLocked, setIsLocked] = useState(false);
  const [lastActivity, setLastActivity] = useState(Date.now());
  const TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

  const handleActivity = useCallback(() => {
    if (!isLocked) {
      setLastActivity(Date.now());
    }
  }, [isLocked]);

  useEffect(() => {
    const events = ['mousedown', 'keydown', 'scroll', 'touchstart'];
    events.forEach(event => window.addEventListener(event, handleActivity));

    const interval = setInterval(() => {
      if (auth.isAuthenticated && !isLocked && Date.now() - lastActivity > TIMEOUT_MS) {
        setIsLocked(true);
      }
    }, 10000);

    return () => {
      events.forEach(event => window.removeEventListener(event, handleActivity));
      clearInterval(interval);
    };
  }, [auth.isAuthenticated, isLocked, lastActivity, handleActivity]);

  const unlock = () => {
    // In a real high-security app, you might force a password re-prompt via Keycloak (prompt=login)
    // For now, we just unlock the screen if their token is still valid
    setIsLocked(false);
    setLastActivity(Date.now());
  };

  return (
    <InactivityContext.Provider value={{ isLocked, unlock }}>
      {children}
      {isLocked && auth.isAuthenticated && (
        <div className="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-md bg-white/30">
          <div className="bg-white p-8 rounded-xl shadow-lg border border-slate-200 text-center max-w-sm w-full relative z-50">
            <div className="flex justify-center mb-4">
              <div className="w-16 h-16 bg-zantrix-blue rounded-full flex items-center justify-center text-white">
                <Lock size={32} />
              </div>
            </div>
            <h2 className="text-xl font-semibold mb-2">Session Locked</h2>
            <p className="text-sm text-slate-500 mb-6">For your security, your session was locked due to inactivity.</p>
            <button
              onClick={unlock}
              className="w-full bg-zantrix-blue text-white py-2 px-4 rounded-lg font-medium hover:bg-opacity-90 transition"
            >
              Unlock
            </button>
          </div>
          <div className="absolute inset-0 bg-black/10 z-40"></div>
        </div>
      )}
    </InactivityContext.Provider>
  );
};
