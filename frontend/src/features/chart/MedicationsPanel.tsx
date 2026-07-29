import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { PrescriptionSummary, SafetyAssessment } from '../../lib/api/types';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';
import { useClinicalContext } from './clinicalContext';

/**
 * Medication management: the active list, prescribing with an interaction
 * check, stopping, administration and dispensing.
 *
 * The safety assessment is run and shown before the prescription is written,
 * not after. A critical issue blocks the plain submit and requires an explicit
 * documented override, which is exactly what the backend enforces.
 */
export function MedicationsPanel({ client, patientId, canPrescribe, canDispense }: {
  client: ApiClient; patientId: string; canPrescribe: boolean; canDispense: boolean;
}) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, practitionerId, ready } = useClinicalContext();
  const [includeStopped, setIncludeStopped] = useState(false);
  const [assessment, setAssessment] = useState<SafetyAssessment>();
  const [pending, setPending] = useState<Record<string, string>>();

  const medications = useQuery({
    queryKey: ['medications', patientId, includeStopped],
    queryFn: ({ signal }) => client.get<PrescriptionSummary[]>(
      `/api/v1/medications?patientId=${encodeURIComponent(patientId)}&includeStopped=${includeStopped}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['medications'] });

  const check = useMutation({
    mutationFn: ({ code, display }: { code: string; display: string }) => client.get<SafetyAssessment>(
      `/api/v1/medications/safety-check?patientId=${encodeURIComponent(patientId)}`
      + `&rxNormIngredientCode=${encodeURIComponent(code)}&display=${encodeURIComponent(display)}`),
    onSuccess: setAssessment,
  });

  const prescribe = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/medications', body),
    onSuccess: () => { invalidate(); setAssessment(undefined); setPending(undefined); },
  });

  const stop = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => client.post(
      `/api/v1/medications/${encodeURIComponent(id)}/stop?patientId=${encodeURIComponent(patientId)}`
      + `&reason=${encodeURIComponent(reason)}`),
    onSuccess: invalidate,
  });

  const administer = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/medications/administrations', body),
    onSuccess: invalidate,
  });

  const reconcile = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/medications/reconciliation', body),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['medications'] }),
  });
  const dispense = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/medications/dispenses', body),
    onSuccess: invalidate,
  });

  function review(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const values = Object.fromEntries(
      [...form.entries()].map(([key, value]) => [key, String(value)])) as Record<string, string>;
    setPending(values);
    check.mutate({ code: values.rxNormIngredientCode, display: values.medicationDisplay });
  }

  function confirm(overrideReason: string | null) {
    if (!pending) return;
    prescribe.mutate({
      patientId, encounterId, requesterId: practitionerId,
      rxNormIngredientCode: pending.rxNormIngredientCode,
      medicationDisplay: pending.medicationDisplay,
      dosageText: pending.dosageText,
      routeSystem: null, routeCode: null, routeDisplay: null,
      doseValue: Number(pending.doseValue), doseUnit: pending.doseUnit,
      frequencyPerDay: Number(pending.frequencyPerDay),
      durationDays: Number(pending.durationDays),
      dispenseQuantity: Number(pending.dispenseQuantity),
      repeats: Number(pending.repeats || 0),
      reason: pending.reason || null,
      safetyOverrideReason: overrideReason,
    });
  }

  const blocking = (assessment?.issues ?? []).some(issue => issue.severity === 'critical');

  return (
    <div className="stack">
      <Panel title={t('chart.medications')}
        actions={<Button className="quiet-button" onClick={() => setIncludeStopped(!includeStopped)}>
          {includeStopped ? t('medications.hideStopped') : t('medications.showStopped')}</Button>}>
        <ErrorNotice error={medications.error} />
        <ErrorNotice error={stop.error} />
        <ErrorNotice error={administer.error} />
        <ErrorNotice error={dispense.error} />
        <DataTable caption={t('chart.medications')} rows={medications.data ?? []} rowKey={row => row.id}
          empty={t('chart.empty')}
          columns={[
            { header: t('table.medication'), cell: row => <span className="primary-cell">{row.medication}</span> },
            { header: t('table.dosage'), cell: row => row.dosage ?? '' },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.status !== 'active' ? null : (
              <div className="button-row">
                <Button className="quiet-button" disabled={!ready || administer.isPending}
                  onClick={() => administer.mutate({
                    patientId, prescriptionId: row.id, encounterId, performerId: practitionerId,
                    occurredAt: new Date().toISOString(), quantity: 1, unit: 'dose', note: null,
                  })}>{t('medications.administer')}</Button>
                {canDispense ? (
                  <Button className="quiet-button" disabled={!ready || dispense.isPending}
                    onClick={() => dispense.mutate({
                      patientId, prescriptionId: row.id, encounterId, performerId: practitionerId,
                      occurredAt: new Date().toISOString(), quantity: 1, unit: 'package', note: null,
                    })}>{t('medications.dispense')}</Button>
                ) : null}
                {canPrescribe ? (
                  <Button className="quiet-button" disabled={stop.isPending}
                    onClick={() => {
                      const reason = window.prompt(t('medications.stopReasonPrompt'));
                      if (reason) stop.mutate({ id: row.id, reason });
                    }}>{t('medications.stop')}</Button>
                ) : null}
              </div>
            ) },
          ]} />
        {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
      </Panel>

      {canPrescribe ? (
        <Panel title={t('medications.prescribeTitle')} subtitle={t('medications.prescribeSubtitle')}>
          <form onSubmit={review}>
            <div className="form-grid">
              <Field label={t('medications.rxNormCode')} required hint={t('medications.rxNormHint')}>
                {(id, describedBy) => <input id={id} name="rxNormIngredientCode" required aria-describedby={describedBy} />}
              </Field>
              <Field label={t('medications.name')} required>{id => <input id={id} name="medicationDisplay" required />}</Field>
              <Field label={t('medications.dosageText')} required>{id => <input id={id} name="dosageText" required />}</Field>
              <Field label={t('medications.doseValue')} required>{id => <input id={id} name="doseValue" type="number" step="any" min="0.01" required />}</Field>
              <Field label={t('medications.doseUnit')} required>{id => <input id={id} name="doseUnit" required />}</Field>
              <Field label={t('clinical.frequency')} required>{id => <input id={id} name="frequencyPerDay" type="number" min="1" required />}</Field>
              <Field label={t('clinical.duration')} required>{id => <input id={id} name="durationDays" type="number" min="1" required />}</Field>
              <Field label={t('clinical.quantity')} required>{id => <input id={id} name="dispenseQuantity" type="number" step="any" min="0.01" required />}</Field>
              <Field label={t('medications.repeats')}>{id => <input id={id} name="repeats" type="number" min="0" defaultValue={0} />}</Field>
              <Field label={t('medications.reason')}>{id => <input id={id} name="reason" />}</Field>
            </div>
            <div className="form-actions">
              <Button type="submit" disabled={!ready || check.isPending}>{t('medications.runSafetyCheck')}</Button>
            </div>
            {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
          </form>
          <ErrorNotice error={check.error} />
          <ErrorNotice error={prescribe.error} />

          {assessment ? (
            <div className="stack">
              <SafetyReport assessment={assessment} />
              <div className="form-actions">
                {blocking ? (
                  <Button className="danger-button" disabled={prescribe.isPending}
                    onClick={() => {
                      const reason = window.prompt(t('medications.overridePrompt'));
                      if (reason) confirm(reason);
                    }}>{t('medications.overrideAndPrescribe')}</Button>
                ) : (
                  <Button disabled={prescribe.isPending} onClick={() => confirm(null)}>{t('medications.confirmPrescribe')}</Button>
                )}
                <Button className="quiet-button" onClick={() => { setAssessment(undefined); setPending(undefined); }}>
                  {t('common.cancel')}
                </Button>
              </div>
            </div>
          ) : null}
        </Panel>
      ) : null}

      <Panel title={t('medications.reconcileTitle')} subtitle={t('medications.reconcileSubtitle')}>
        <form onSubmit={event => {
          event.preventDefault();
          const form = new FormData(event.currentTarget);
          reconcile.mutate({
            patientId, encounterId, performerId: practitionerId,
            rxNormIngredientCode: String(form.get('rxNormIngredientCode') ?? ''),
            medicationDisplay: String(form.get('medicationDisplay') ?? ''),
            status: String(form.get('status') ?? 'active'),
            dosageText: String(form.get('dosageText') ?? '') || null,
            informationSource: String(form.get('informationSource') ?? '') || null,
          });
          event.currentTarget.reset();
        }}>
          <div className="form-grid">
            <Field label={t('medications.rxNormCode')} required hint={t('medications.rxNormHint')}>
              {(id, describedBy) => <input id={id} name="rxNormIngredientCode" required aria-describedby={describedBy} />}
            </Field>
            <Field label={t('medications.name')} required>{id => <input id={id} name="medicationDisplay" required />}</Field>
            <Field label={t('medications.dosageText')}>{id => <input id={id} name="dosageText" />}</Field>
            <Field label={t('table.status')} required>
              {id => <select id={id} name="status" defaultValue="active">
                <option value="active">{t('medications.takingNow')}</option>
                <option value="completed">{t('medications.takenPreviously')}</option>
                <option value="stopped">{t('medications.stopped')}</option>
              </select>}
            </Field>
            <Field label={t('medications.informationSource')} hint={t('medications.informationSourceHint')}>
              {(id, describedBy) => <input id={id} name="informationSource" aria-describedby={describedBy} />}
            </Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={!ready || reconcile.isPending}>{t('medications.recordReported')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <ErrorNotice error={reconcile.error} />
      </Panel>
    </div>
  );
}

/** Shows the interaction result and is explicit about the limits of the knowledge base. */
export function SafetyReport({ assessment }: { assessment: SafetyAssessment }) {
  const { t } = useTranslation();
  return (
    <div className="safety-report">
      {assessment.issues.length === 0
        ? <Notice tone="info">{t('medications.noIssues')}</Notice>
        : assessment.issues.map(issue => (
          <Notice key={`${issue.ruleId}-${issue.existingMedicationId ?? ''}`}
            tone={issue.severity === 'critical' ? 'danger' : 'warning'}>
            <strong>{issue.ruleId}</strong> {issue.summary}
          </Notice>
        ))}
      <p className="field-hint">
        {t('medications.knowledgeBase', {
          name: assessment.knowledgeBase, version: assessment.version, coverage: assessment.coverage,
        })}
      </p>
      {!assessment.comprehensive ? <p className="field-hint">{t('medications.notComprehensive')}</p> : null}
    </div>
  );
}
