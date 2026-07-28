import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { OrderSummary } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { useClinicalContext } from './clinicalContext';

const LOINC = 'http://loinc.org';

/** Order entry. Results are filed from the results tab, against the order they answer. */
export function OrdersPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, practitionerId, ready } = useClinicalContext();
  const [includeCompleted, setIncludeCompleted] = useState(false);

  const orders = useQuery({
    queryKey: ['orders', patientId, includeCompleted],
    queryFn: ({ signal }) => client.get<OrderSummary[]>(
      `/api/v1/orders?patientId=${encodeURIComponent(patientId)}&includeCompleted=${includeCompleted}`, signal),
  });

  const place = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/orders', body),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['orders'] }),
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    place.mutate({
      patientId, encounterId, requesterId: practitionerId,
      category: String(form.get('category') ?? 'laboratory'),
      codeSystem: LOINC, code: String(form.get('code') ?? ''), display: String(form.get('display') ?? ''),
      priority: String(form.get('priority') ?? 'routine'),
      clinicalNote: String(form.get('clinicalNote') ?? '') || null,
    });
    event.currentTarget.reset();
  }

  return (
    <div className="stack">
      <Panel title={t('chart.orders')}
        actions={<Button className="quiet-button" onClick={() => setIncludeCompleted(!includeCompleted)}>
          {includeCompleted ? t('orders.hideCompleted') : t('orders.showCompleted')}</Button>}>
        <ErrorNotice error={orders.error} />
        <DataTable caption={t('chart.orders')} rows={orders.data ?? []} rowKey={row => row.id} empty={t('chart.empty')}
          columns={[
            { header: t('table.order'), cell: row => <span className="primary-cell">{row.display}</span> },
            { header: t('table.category'), cell: row => row.category },
            { header: t('table.priority'), cell: row => <StatusBadge status={row.priority} /> },
            { header: t('table.authored'), cell: row => formatDateTime(row.authoredOn, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
          ]} />
      </Panel>

      <Panel title={t('orders.placeTitle')}>
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field label={t('orders.category')} required>
              {id => <select id={id} name="category" defaultValue="laboratory">
                <option value="laboratory">{t('orders.categoryLaboratory')}</option>
                <option value="imaging">{t('orders.categoryImaging')}</option>
                <option value="procedure">{t('orders.categoryProcedure')}</option>
                <option value="referral">{t('orders.categoryReferral')}</option>
              </select>}
            </Field>
            <Field label={t('orders.loincCode')} required hint={t('orders.loincHint')}>
              {(id, describedBy) => <input id={id} name="code" required aria-describedby={describedBy} />}
            </Field>
            <Field label={t('clinical.display')} required>{id => <input id={id} name="display" required />}</Field>
            <Field label={t('table.priority')} required>
              {id => <select id={id} name="priority" defaultValue="routine">
                <option value="routine">{t('orders.priorityRoutine')}</option>
                <option value="urgent">{t('orders.priorityUrgent')}</option>
                <option value="asap">{t('orders.priorityAsap')}</option>
                <option value="stat">{t('orders.priorityStat')}</option>
              </select>}
            </Field>
            <Field label={t('clinical.note')}>{id => <input id={id} name="clinicalNote" />}</Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={!ready || place.isPending}>{t('orders.place')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <ErrorNotice error={place.error} />
      </Panel>
    </div>
  );
}
