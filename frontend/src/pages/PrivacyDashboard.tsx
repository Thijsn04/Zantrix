import { useEffect, useState } from 'react';
import { useAuth } from 'react-oidc-context';
import { Shield, Search } from 'lucide-react';

interface AuditLog {
  id: number;
  timestamp: string;
  username: string;
  action: string;
  resource: string;
  breakTheGlass: boolean;
}

export const PrivacyDashboard = () => {
  const auth = useAuth();
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/v1/iam/audit-logs', {
          headers: {
            'Authorization': `Bearer ${auth.user?.access_token}`
          }
        });
        if (!response.ok) {
          if (response.status === 403) throw new Error('Access Denied. You must be a Privacy Officer.');
          throw new Error('Failed to fetch audit logs');
        }
        const data = await response.json();
        setLogs(data.content || []);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (auth.isAuthenticated) {
      fetchLogs();
    }
  }, [auth.isAuthenticated, auth.user?.access_token]);

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center text-white shadow-sm">
          <Shield size={20} />
        </div>
        <div>
          <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Systeem Audit Logs</h1>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
        <div className="px-4 py-3 border-b border-slate-200 flex justify-between items-center bg-slate-50/50">
          <h2 className="font-semibold text-zantrix-text text-sm">System Audit Logs</h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={14} />
            <input 
              type="text" 
              placeholder="Filter logs..." 
              className="pl-8 pr-4 py-1.5 text-sm border border-slate-300 rounded focus:outline-none focus:ring-1 focus:ring-zantrix-blue focus:border-zantrix-blue transition"
            />
          </div>
        </div>

        {loading ? (
          <div className="p-8 text-center text-slate-500">Loading audit trail...</div>
        ) : error ? (
          <div className="p-8 text-center text-semantic-critical">{error}</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-600 border-b border-slate-200">
                <tr>
                  <th className="px-6 py-3 font-medium">Timestamp</th>
                  <th className="px-6 py-3 font-medium">User</th>
                  <th className="px-6 py-3 font-medium">Action</th>
                  <th className="px-6 py-3 font-medium">Resource</th>
                  <th className="px-6 py-3 font-medium">Context</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-slate-500">
                      {new Date(log.timestamp).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-900">{log.username}</td>
                    <td className="px-6 py-4">
                      <span className="font-mono text-xs bg-slate-100 px-2 py-1 rounded text-slate-600">
                        {log.action}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-600">{log.resource}</td>
                    <td className="px-6 py-4">
                      {log.breakTheGlass ? (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-semantic-critical bg-opacity-10 text-semantic-critical">
                          Emergency Mode
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-semantic-positive bg-opacity-10 text-semantic-positive">
                          Standard Access
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
                {logs.length === 0 && (
                  <tr>
                    <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                      No audit logs found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
