import React, { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { BarChart2, TrendingUp } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function DwhReports() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [occupancy, setOccupancy] = useState<any>(null);

  useEffect(() => {
    fetchOccupancy();
  }, []);

  const fetchOccupancy = async () => {
    const res = await fetch('http://localhost:8080/api/v1/dwh/reports/bed-occupancy', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setOccupancy(await res.json());
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <BarChart2 className="text-zantrix-blue" />
        Data Warehouse & Analytics
      </h1>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-4 shadow rounded flex items-center gap-4">
          <div className="bg-blue-100 p-3 rounded-full text-zantrix-blue"><TrendingUp /></div>
          <div>
            <div className="text-sm text-gray-500">Total Beds</div>
            <div className="text-2xl font-bold">{occupancy?.totalBeds || '-'}</div>
          </div>
        </div>
        <div className="bg-white p-4 shadow rounded flex items-center gap-4">
          <div className="bg-green-100 p-3 rounded-full text-green-600"><TrendingUp /></div>
          <div>
            <div className="text-sm text-gray-500">Occupied</div>
            <div className="text-2xl font-bold">{occupancy?.occupiedBeds || '-'}</div>
          </div>
        </div>
        <div className="bg-white p-4 shadow rounded flex items-center gap-4">
          <div className="bg-yellow-100 p-3 rounded-full text-yellow-600"><TrendingUp /></div>
          <div>
            <div className="text-sm text-gray-500">Cleaning Needed</div>
            <div className="text-2xl font-bold">{occupancy?.cleaningNeeded || '-'}</div>
          </div>
        </div>
        <div className="bg-white p-4 shadow rounded flex items-center gap-4">
          <div className="bg-purple-100 p-3 rounded-full text-purple-600"><TrendingUp /></div>
          <div>
            <div className="text-sm text-gray-500">Available</div>
            <div className="text-2xl font-bold">{occupancy?.availableBeds || '-'}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
