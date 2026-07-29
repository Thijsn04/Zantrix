import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CurrentUser } from '../../lib/api/types';
import { MAX_OPEN_PATIENTS, useOpenCharts } from '../../app/useOpenCharts';
import { PatientTabs } from './PatientTabs';
import { PatientRegistry } from './PatientRegistry';
import { PatientChart } from '../chart/PatientChart';
import { Notice } from '../../design/Feedback';
import { Panel } from '../../design/Panel';

export function PatientWorkspace({ client, user }: { client: ApiClient; user: CurrentUser }) {
  const { t } = useTranslation();
  const charts = useOpenCharts(client);

  return (
    <div className="patient-workspace">
      {charts.open.length > 0 ? (
        <PatientTabs patients={charts.open} activeId={charts.activeId}
          onActivate={charts.activate} onClose={charts.close} onSearch={charts.search} />
      ) : null}

      {/*
        Every open chart stays mounted and is hidden rather than unmounted, so
        switching patients keeps query caches, the selected section, and
        anything already typed into a form. `hidden` also removes the inactive
        chart from the accessibility tree.
      */}
      {charts.open.map(patient => (
        <div key={patient.id} hidden={patient.id !== charts.activeId}>
          <PatientChart client={client} patient={patient} user={user}
            onClear={() => charts.close(patient.id)} />
        </div>
      ))}

      {charts.loading && !charts.activeId
        ? <Panel title={t('common.loading')} level={1}><span /></Panel> : null}

      {!charts.activeId && !charts.loading ? (
        <>
          {charts.limitReached ? (
            <Notice tone="warning">{t('patients.limitReached', { count: MAX_OPEN_PATIENTS })}</Notice>
          ) : null}
          <PatientRegistry client={client} onSelect={charts.openPatient} />
        </>
      ) : null}
    </div>
  );
}
