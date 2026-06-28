import React, { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { Activity, Server, RefreshCw, AlertTriangle } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function InteropDashboard() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [channels, setChannels] = useState<any[]>([]);
  const [messages, setMessages] = useState<any[]>([]);

  useEffect(() => {
    fetchChannels();
    fetchMessages();
  }, []);

  const fetchChannels = async () => {
    const res = await fetch('http://localhost:8080/api/v1/interop/channels', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setChannels(await res.json());
  };

  const fetchMessages = async () => {
    const res = await fetch('http://localhost:8080/api/v1/interop/messages', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setMessages(await res.json());
  };

  const handleReshoot = async (id: string) => {
    const res = await fetch(`http://localhost:8080/api/v1/interop/messages/${id}/reshoot`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) {
      fetchMessages();
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <Activity className="text-zantrix-blue" />
        {t('interop.dashboard_title', 'Interoperability Dashboard')}
      </h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div className="bg-white p-4 shadow rounded">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
            <Server /> Integration Channels
          </h2>
          {channels.length === 0 ? <p className="text-gray-500">No channels configured.</p> : (
            <ul className="space-y-2">
              {channels.map(ch => (
                <li key={ch.id} className="p-3 border rounded flex justify-between items-center">
                  <div>
                    <div className="font-bold">{ch.name}</div>
                    <div className="text-sm text-gray-500">{ch.protocol} - {ch.direction}</div>
                  </div>
                  <span className={`px-2 py-1 rounded text-xs text-white ${ch.status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-500'}`}>
                    {ch.status}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="bg-white p-4 shadow rounded">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
            <AlertTriangle /> Recent Message Logs
          </h2>
          {messages.length === 0 ? <p className="text-gray-500">No recent messages.</p> : (
            <ul className="space-y-2">
              {messages.map(msg => (
                <li key={msg.id} className="p-3 border rounded flex flex-col gap-2">
                  <div className="flex justify-between items-center">
                    <span className="text-xs text-gray-500">{new Date(msg.timestamp).toLocaleString()}</span>
                    <span className={`px-2 py-1 rounded text-xs text-white ${msg.status === 'ERROR' ? 'bg-red-500' : 'bg-green-500'}`}>
                      {msg.status}
                    </span>
                  </div>
                  <div className="text-sm truncate">{msg.rawPayload}</div>
                  {msg.status === 'ERROR' && (
                    <button 
                      onClick={() => handleReshoot(msg.id)}
                      className="self-end flex items-center gap-1 text-sm bg-blue-100 text-blue-700 px-2 py-1 rounded hover:bg-blue-200"
                    >
                      <RefreshCw size={14} /> Reshoot
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
