import { useState } from 'react';
import { useAuth } from 'react-oidc-context';
import { Monitor, UserCheck, CreditCard } from 'lucide-react';

export default function KioskTerminal() {
  const auth = useAuth();
  const [docNumber, setDocNumber] = useState('');
  const [status, setStatus] = useState<string | null>(null);

  const handleScan = async () => {
    setStatus("Scanning...");
    const res = await fetch(`http://localhost:8080/api/v1/kiosk/verify-id?documentNumber=${docNumber}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) {
      const data = await res.json();
      if (data.verified) {
        await handleCheckIn(data.patientId);
      } else {
        setStatus("ID Verification Failed");
      }
    }
  };

  const handleCheckIn = async (patientId: string) => {
    const res = await fetch(`http://localhost:8080/api/v1/kiosk/check-in?patientId=${patientId}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) {
      const data = await res.json();
      setStatus(data.message);
    }
  };

  return (
    <div className="min-h-full flex items-center justify-center bg-zantrix-blue p-6">
      <div className="bg-white rounded-xl shadow-2xl p-10 max-w-lg w-full text-center">
        <Monitor size={64} className="text-zantrix-blue mx-auto mb-6" />
        <h1 className="text-3xl font-bold mb-2 text-slate-800">Welcome to Zantrix</h1>
        <p className="text-slate-500 mb-8">Please scan your ID card or passport to check in for your appointment.</p>

        <div className="mb-6 relative">
          <CreditCard className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input 
            type="text" 
            placeholder="Document Number (e.g. simulated scan)" 
            value={docNumber}
            onChange={(e) => setDocNumber(e.target.value)}
            className="w-full pl-10 pr-4 py-3 text-lg border border-slate-300 rounded focus:outline-none focus:border-zantrix-blue"
          />
        </div>

        <button 
          onClick={handleScan}
          disabled={!docNumber}
          className="w-full py-4 bg-zantrix-blue text-white rounded text-lg font-bold flex justify-center items-center gap-2 hover:bg-blue-700 disabled:opacity-50 transition"
        >
          <UserCheck /> Simulate ID Scan & Check In
        </button>

        {status && (
          <div className="mt-8 p-4 bg-green-50 text-green-800 rounded border border-green-200 text-lg font-medium">
            {status}
          </div>
        )}
      </div>
    </div>
  );
}
