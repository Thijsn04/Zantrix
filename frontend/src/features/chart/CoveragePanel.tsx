import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CoverageSummary } from '../../lib/api/types';
import { formatDate } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';

/** HL7 v3 ActCode values for the coverage kinds an outpatient clinic actually records. */
const TYPES = [
  { code: 'EHCPOL', key: 'health' },
  { code: 'PUBLICPOL', key: 'public' },
  { code: 'SUBSIDIZ', key: 'subsidised' },
  { code: 'pay', key: 'selfPay' },
] as const;

const RELATIONSHIPS = ['self', 'spouse', 'child', 'parent', 'common', 'other'] as const;

/**
 * A patient's insurance coverage.
 *
 * This records what the patient has. It is not an eligibility check and does
 * not confirm that any treatment will be paid for, which the panel states
 * rather than leaving to assumption.
 */
export function CoveragePanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [showCancelled, setShowCancelled] = useState(false);

  const coverage = useQuery({
    queryKey: ['coverage', patientId, showCancelled],
    queryFn: ({ signal }) => client.get<CoverageSummary[]>(
      `/api/v1/coverage?patientId=${encodeURIComponent(patientId)}&includeCancelled=${showCancelled}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['coverage'] });
  const add = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/coverage', body),
    onSuccess: invalidate,
  });
  const cancel = useMutation({
    mutationFn: (id: string) => client.post(
      `/api/v1/coverage/${encodeURIComponent(id)}/cancel?patientId=${encodeURIComponent(patientId)}`),
    onSuccess: invalidate,
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const typeCode = String(form.get('typeCode') ?? 'EHCPOL');
    const end = String(form.get('end') ?? '');
    add.mutate({
      patientId,
      payorOrganizationId: String(form.get('payorOrganizationId') ?? ''),
      payorDisplay: String(form.get('payorDisplay') ?? ''),
      typeCode,
      typeDisplay: t(`coverage.type.${TYPES.find(value => value.code === typeCode)?.key ?? 'health'}`),
      relationship: String(form.get('relationship') ?? 'self'),
      subscriberId: String(form.get('subscriberId') ?? ''),
      groupNumber: String(form.get('groupNumber') ?? '') || null,
      start: String(form.get('start') ?? ''),
      end: end || null,
    });
    event.currentTarget.reset();
  }

  return (
    <div className="stack">
      <Panel title={t('chart.coverage')}
        actions={<Button className="quiet-button" onClick={() => setShowCancelled(!showCancelled)}>
          {showCancelled ? t('coverage.hideCancelled') : t('coverage.showCancelled')}</Button>}>
        <Notice tone="info">{t('coverage.notEligibility')}</Notice>
        <ErrorNotice error={coverage.error} />
        <ErrorNotice error={cancel.error} />
        <DataTable caption={t('chart.coverage')} rows={coverage.data ?? []} rowKey={row => row.id}
          empty={t('coverage.none')}
          columns={[
            { header: t('coverage.payor'), cell: row => <span className="primary-cell">{row.payor ?? row.payorOrganizationId}</span> },
            { header: t('coverage.policy'), cell: row => row.subscriberId ?? '' },
            { header: t('coverage.group'), cell: row => row.groupNumber ?? '' },
            { header: t('coverage.relationship'), cell: row => t(`coverage.relation.${row.relationship}`, {
              defaultValue: row.relationship ?? '' }) },
            { header: t('coverage.from'), cell: row => formatDate(row.start, i18n.language) },
            { header: t('coverage.until'), cell: row => row.end ? formatDate(row.end, i18n.language) : t('coverage.openEnded') },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status ?? ''} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.status === 'active'
              ? <Button className="quiet-button" disabled={cancel.isPending}
                  onClick={() => cancel.mutate(row.id)}>{t('coverage.cancel')}</Button>
              : null },
          ]} />
      </Panel>

      <Panel title={t('coverage.addTitle')} subtitle={t('coverage.addSubtitle')}>
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field label={t('coverage.payor')} required>{id => <input id={id} name="payorDisplay" required />}</Field>
            <Field label={t('coverage.payorId')} required hint={t('coverage.payorIdHint')}>
              {(id, describedBy) => <input id={id} name="payorOrganizationId" required aria-describedby={describedBy} />}
            </Field>
            <Field label={t('coverage.kind')} required>
              {id => <select id={id} name="typeCode" defaultValue="EHCPOL">
                {TYPES.map(value => <option key={value.code} value={value.code}>{t(`coverage.type.${value.key}`)}</option>)}
              </select>}
            </Field>
            <Field label={t('coverage.relationship')} required>
              {id => <select id={id} name="relationship" defaultValue="self">
                {RELATIONSHIPS.map(value => <option key={value} value={value}>{t(`coverage.relation.${value}`)}</option>)}
              </select>}
            </Field>
            <Field label={t('coverage.policy')} required>{id => <input id={id} name="subscriberId" required />}</Field>
            <Field label={t('coverage.group')}>{id => <input id={id} name="groupNumber" />}</Field>
            <Field label={t('coverage.from')} required>
              {id => <input id={id} name="start" type="date" required
                defaultValue={new Date().toISOString().slice(0, 10)} />}
            </Field>
            <Field label={t('coverage.until')} hint={t('coverage.untilHint')}>
              {(id, describedBy) => <input id={id} name="end" type="date" aria-describedby={describedBy} />}
            </Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={add.isPending}>{t('coverage.add')}</Button>
          </div>
        </form>
        <ErrorNotice error={add.error} />
      </Panel>
    </div>
  );
}
