import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { ConsentSummary } from '../../lib/api/types';
import { formatDate } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';

/** Resource types a consent provision can cover, matching what the M1 gateway enforces. */
const RESOURCE_TYPES = [
  'Observation', 'Condition', 'AllergyIntolerance', 'MedicationRequest',
  'ServiceRequest', 'DiagnosticReport', 'Composition', 'Encounter',
];

/**
 * This patient's consents.
 *
 * Consent is enforced at the FHIR boundary, so what is recorded here changes
 * what clinicians can read. The panel states the deployment's effective policy,
 * because the same consent means different things under a permit-required
 * policy than under deny-on-explicit.
 */
export function ConsentPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<string[]>([]);

  const consents = useQuery({
    queryKey: ['consents', patientId],
    queryFn: ({ signal }) => client.get<ConsentSummary[]>(
      `/api/v1/consents?patientId=${encodeURIComponent(patientId)}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['consents'] });
  const create = useMutation({
    mutationFn: (body: unknown) => client.post<ConsentSummary>('/api/v1/consents', body),
    onSuccess: () => { invalidate(); setSelected([]); },
  });
  const revoke = useMutation({
    mutationFn: (id: string) => client.post<ConsentSummary>(
      `/api/v1/consents/${encodeURIComponent(id)}/revoke?patientId=${encodeURIComponent(patientId)}`),
    onSuccess: invalidate,
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (selected.length === 0) return;
    const form = new FormData(event.currentTarget);
    const end = String(form.get('end') ?? '');
    create.mutate({
      patientId,
      type: String(form.get('type') ?? 'permit'),
      resourceTypes: selected,
      start: new Date(String(form.get('start') ?? '')).toISOString(),
      end: end ? new Date(end).toISOString() : null,
      policyUri: String(form.get('policyUri') ?? '') || null,
    });
  }

  return (
    <div className="stack">
      <Panel title={t('consent.title')} subtitle={t('consent.subtitle')}>
        <ErrorNotice error={consents.error} />
        <ErrorNotice error={revoke.error} />
        <DataTable caption={t('consent.title')} rows={consents.data ?? []} rowKey={row => row.id}
          empty={t('consent.none')}
          columns={[
            { header: t('consent.type'), cell: row => (
              <span className="primary-cell">{row.type === 'deny' ? t('consent.deny') : t('consent.permit')}</span>) },
            { header: t('consent.covers'), cell: row => row.resourceTypes.join(', ') },
            { header: t('consent.from'), cell: row => formatDate(row.start, i18n.language) },
            { header: t('consent.until'), cell: row => row.end ? formatDate(row.end, i18n.language) : t('consent.openEnded') },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.status === 'active'
              ? <Button className="quiet-button" disabled={revoke.isPending}
                  onClick={() => revoke.mutate(row.id)}>{t('consent.revoke')}</Button>
              : null },
          ]} />
      </Panel>

      <Panel title={t('consent.recordTitle')}>
        <Notice tone="info">{t('consent.enforcementNote')}</Notice>
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field label={t('consent.type')} required>
              {id => <select id={id} name="type" defaultValue="permit">
                <option value="permit">{t('consent.permit')}</option>
                <option value="deny">{t('consent.deny')}</option>
              </select>}
            </Field>
            <Field label={t('consent.from')} required>
              {id => <input id={id} name="start" type="date" required
                defaultValue={new Date().toISOString().slice(0, 10)} />}
            </Field>
            <Field label={t('consent.until')} hint={t('consent.untilHint')}>
              {(id, describedBy) => <input id={id} name="end" type="date" aria-describedby={describedBy} />}
            </Field>
            <Field label={t('consent.policyUri')}>{id => <input id={id} name="policyUri" type="url" />}</Field>
          </div>

          <fieldset className="checkbox-set">
            <legend>{t('consent.covers')}</legend>
            {RESOURCE_TYPES.map(type => (
              <label key={type} className="checkbox-row">
                <input type="checkbox" checked={selected.includes(type)}
                  onChange={event => setSelected(current => event.target.checked
                    ? [...current, type]
                    : current.filter(value => value !== type))} />
                {type}
              </label>
            ))}
          </fieldset>

          <div className="form-actions">
            <Button type="submit" disabled={selected.length === 0 || create.isPending}>{t('common.save')}</Button>
          </div>
          {selected.length === 0 ? <p className="field-hint">{t('consent.selectResource')}</p> : null}
        </form>
        <ErrorNotice error={create.error} />
      </Panel>
    </div>
  );
}
