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
import { ErrorNotice, Notice } from '../../design/Feedback';
import { useClinicalContext } from './clinicalContext';

const LOINC = 'http://loinc.org';

/**
 * Laboratory and diagnostic results.
 *
 * A result is always filed against the order that requested it, so the panel
 * starts from the outstanding orders rather than offering a free-standing
 * result form that could be attached to nothing.
 */
export function ResultsPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { practitionerId, ready } = useClinicalContext();
  const [resultFor, setResultFor] = useState<OrderSummary>();

  const results = useQuery({
    queryKey: ['results', patientId],
    queryFn: ({ signal }) => client.get<ResultSummary[]>(
      `/api/v1/orders/results?patientId=${encodeURIComponent(patientId)}`, signal),
  });
  const orders = useQuery({
    queryKey: ['orders', patientId, false],
    queryFn: ({ signal }) => client.get<OrderSummary[]>(
      `/api/v1/orders?patientId=${encodeURIComponent(patientId)}&includeCompleted=false`, signal),
  });

  const fileResult = useMutation({
    mutationFn: ({ orderId, body }: { orderId: string; body: unknown }) =>
      client.post(`/api/v1/orders/${encodeURIComponent(orderId)}/results`, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['results'] });
      void queryClient.invalidateQueries({ queryKey: ['orders'] });
      setResultFor(undefined);
    },
  });

  const awaiting = (orders.data ?? []).filter(order => order.status !== 'completed' && order.status !== 'revoked');

  function submit(event: FormEvent<HTMLFormElement>) {
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

      <Panel title={t('results.awaitingTitle')} subtitle={t('results.awaitingSubtitle')}>
        <ErrorNotice error={orders.error} />
        <DataTable caption={t('results.awaitingTitle')} rows={awaiting} rowKey={row => row.id}
          empty={t('results.noneAwaiting')}
          columns={[
            { header: t('table.order'), cell: row => <span className="primary-cell">{row.display}</span> },
            { header: t('table.category'), cell: row => row.category },
            { header: t('table.priority'), cell: row => <StatusBadge status={row.priority} /> },
            { header: t('table.actions'), align: 'end', cell: row =>
              <Button className="quiet-button" onClick={() => setResultFor(row)}>{t('orders.fileResult')}</Button> },
          ]} />
      </Panel>

      {resultFor ? (
        <Panel title={t('orders.resultTitle', { order: resultFor.display })}>
          <Notice tone="info">{t('results.filedAgainstOrder', { code: resultFor.code })}</Notice>
          <form onSubmit={submit}>
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
            {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
          </form>
          <ErrorNotice error={fileResult.error} />
        </Panel>
      ) : null}
    </div>
  );
}
