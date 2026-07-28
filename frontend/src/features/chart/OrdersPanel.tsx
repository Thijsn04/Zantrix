import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { OrderSummary, ResultSummary } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { useClinicalContext } from './clinicalContext';

const LOINC = 'http://loinc.org';

export function OrdersPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, practitionerId, ready } = useClinicalContext();
  const [resultFor, setResultFor] = useState<OrderSummary>();
  const [includeCompleted, setIncludeCompleted] = useState(false);

  const orders = useQuery({
    queryKey: ['orders', patientId, includeCompleted],
    queryFn: ({ signal }) => client.get<OrderSummary[]>(
      `/api/v1/orders?patientId=${encodeURIComponent(patientId)}&includeCompleted=${includeCompleted}`, signal),
  });
  const results = useQuery({
    queryKey: ['results', patientId],
    queryFn: ({ signal }) => client.get<ResultSummary[]>(
      `/api/v1/orders/results?patientId=${encodeURIComponent(patientId)}`, signal),
  });

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['orders'] });
    void queryClient.invalidateQueries({ queryKey: ['results'] });
  };

  const place = useMutation({ mutationFn: (body: unknown) => client.post('/api/v1/orders', body), onSuccess: invalidate });
  const fileResult = useMutation({
    mutationFn: ({ orderId, body }: { orderId: string; body: unknown }) =>
      client.post(`/api/v1/orders/${encodeURIComponent(orderId)}/results`, body),
    onSuccess: () => { invalidate(); setResultFor(undefined); },
  });

  function submitOrder(event: FormEvent<HTMLFormElement>) {
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

  function submitResult(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!resultFor) return;
    const form = new FormData(event.currentTarget);
    const numeric = String(form.get('numericValue') ?? '').trim();
    fileResult.mutate({
      orderId: resultFor.id,
      body: {
        patientId, performerId: practitionerId, issuedAt: new Date().toISOString(),
        measurements: [{
          codeSystem: LOINC, code: resultFor.code, display: resultFor.display,
          numericValue: numeric === '' ? null : Number(numeric),
          unit: String(form.get('unit') ?? '') || null,
          textValue: String(form.get('textValue') ?? '') || null,
        }],
        conclusion: String(form.get('conclusion') ?? '') || null,
      },
    });
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
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.status === 'completed' ? null
              : <Button className="quiet-button" onClick={() => setResultFor(row)}>{t('orders.fileResult')}</Button> },
          ]} />
      </Panel>

      {resultFor ? (
        <Panel title={t('orders.resultTitle', { order: resultFor.display })}>
          <form onSubmit={submitResult}>
            <div className="form-grid">
              <Field label={t('clinical.value')}>{id => <input id={id} name="numericValue" type="number" step="any" />}</Field>
              <Field label={t('clinical.unit')}>{id => <input id={id} name="unit" />}</Field>
              <Field label={t('orders.textValue')}>{id => <input id={id} name="textValue" />}</Field>
              <Field label={t('orders.conclusion')}>{id => <input id={id} name="conclusion" />}</Field>
            </div>
            <div className="form-actions">
              <Button type="submit" disabled={!ready || fileResult.isPending}>{t('common.save')}</Button>
              <Button className="quiet-button" onClick={() => setResultFor(undefined)}>{t('common.cancel')}</Button>
            </div>
          </form>
          <ErrorNotice error={fileResult.error} />
        </Panel>
      ) : null}

      <Panel title={t('chart.results')}>
        <ErrorNotice error={results.error} />
        <DataTable caption={t('chart.results')} rows={results.data ?? []} rowKey={row => row.id} empty={t('chart.empty')}
          columns={[
            { header: t('table.result'), cell: row => <span className="primary-cell">{row.conclusion ?? row.id}</span> },
            { header: t('table.observations'), align: 'end', cell: row => row.observationIds.length },
            { header: t('table.issued'), cell: row => formatDateTime(row.issuedAt, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
          ]} />
      </Panel>

      <Panel title={t('orders.placeTitle')}>
        <form onSubmit={submitOrder}>
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
