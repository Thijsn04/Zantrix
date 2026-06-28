import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { Inbox, FilePlus, ExternalLink } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function ReferralInbox() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [referrals, setReferrals] = useState<any[]>([]);

  useEffect(() => {
    fetchReferrals();
  }, []);

  const fetchReferrals = async () => {
    const res = await fetch('http://localhost:8080/api/v1/referral/inbox', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setReferrals(await res.json());
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <Inbox className="text-zantrix-blue" />
        {t('referral.inbox_title', 'Referral Inbox')}
      </h1>

      <div className="bg-white p-4 shadow rounded max-w-5xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold flex items-center gap-2">
            <FilePlus /> Inbound Service Requests
          </h2>
          <span className="text-sm bg-blue-100 text-blue-800 px-3 py-1 rounded-full font-medium">
            {referrals.length} Active
          </span>
        </div>

        {referrals.length === 0 ? <p className="text-gray-500">No active referrals found.</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-sm">
                  <th className="p-3 font-semibold">Date</th>
                  <th className="p-3 font-semibold">Patient ID</th>
                  <th className="p-3 font-semibold">Requester</th>
                  <th className="p-3 font-semibold">Intent</th>
                  <th className="p-3 font-semibold">Reason</th>
                  <th className="p-3 font-semibold">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {referrals.map(r => (
                  <tr key={r.id} className="hover:bg-slate-50">
                    <td className="p-3 text-sm">{new Date(r.authoredOn).toLocaleDateString()}</td>
                    <td className="p-3 text-sm font-mono text-xs">{r.patientId}</td>
                    <td className="p-3 text-sm">{r.requester}</td>
                    <td className="p-3 text-sm uppercase">{r.intent}</td>
                    <td className="p-3 text-sm truncate max-w-[200px]">{r.reasonCode}</td>
                    <td className="p-3 text-sm">
                      <button className="text-zantrix-blue hover:underline flex items-center gap-1">
                        Review <ExternalLink size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
