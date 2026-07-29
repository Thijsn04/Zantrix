import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { PatientRegistration, PatientSummary } from '../../lib/api/types';
import { age, formatDate, sexMarker } from '../../lib/format';
import { Button } from '../../design/Button';
import { Field } from '../../design/Field';
import { EmptyState, Panel } from '../../design/Panel';
import { Badge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';

const IDENTIFIER_SYSTEM = 'https://zantrix.org/identifier/local-mrn';

/**
 * Patient search and registration.
 *
 * Registration is deliberately two-stage: the first submit asks the backend for
 * probable duplicates and shows them, and only an explicit confirmation creates
 * a new record. Creating a duplicate patient is a serious clinical error, so it
 * must take a second, informed action.
 */
export function PatientRegistry({ client, onSelect }: { client: ApiClient; onSelect: (patient: PatientSummary) => void }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [draft, setDraft] = useState<PatientRegistration>();

  const patients = useQuery({
    queryKey: ['patients', query],
    queryFn: ({ signal }) => client.get<PatientSummary[]>(
      `/api/v1/patients${query ? `?query=${encodeURIComponent(query)}` : ''}`, signal),
  });

  const duplicates = useMutation({
    mutationFn: (registration: PatientRegistration) => client.post<PatientSummary[]>('/api/v1/patients/duplicates', registration),
  });

  const register = useMutation({
    mutationFn: (registration: PatientRegistration) => client.post<PatientSummary>('/api/v1/patients', registration),
    onSuccess: patient => {
      void queryClient.invalidateQueries({ queryKey: ['patients'] });
      setDraft(undefined);
      duplicates.reset();
      onSelect(patient);
    },
  });

  function review(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const identifierValue = String(form.get('identifierValue') ?? '').trim();
    const registration: PatientRegistration = {
      givenName: String(form.get('givenName') ?? '').trim(),
      familyName: String(form.get('familyName') ?? '').trim(),
      birthDate: String(form.get('birthDate') ?? ''),
      administrativeGender: String(form.get('administrativeGender') ?? 'unknown'),
      email: String(form.get('email') ?? '').trim() || null,
      phone: String(form.get('phone') ?? '').trim() || null,
      identifierSystem: identifierValue ? IDENTIFIER_SYSTEM : null,
      identifierValue: identifierValue || null,
      // Recorded here because the treatment relationship policy resolves access
      // from it, and it cannot be inferred once the record exists.
      managingOrganizationId: String(form.get('managingOrganizationId') ?? '').trim() || null,
      confirmedUnique: false,
    };
    setDraft(registration);
    duplicates.mutate(registration);
  }

  return (
    <div className="grid-2">
      <Panel title={t('patients.searchTitle')} level={1}>
        <Field label={t('patients.searchLabel')}>
          {id => <input id={id} type="search" value={query} onChange={event => setQuery(event.target.value)} />}
        </Field>
        <ErrorNotice error={patients.error} />
        <div className="result-list">
          {patients.data?.length ? patients.data.map(patient => {
            const years = age(patient.birthDate);
            return (
              <button className="result-card" key={patient.id} onClick={() => onSelect(patient)}>
                <strong>{patient.displayName}</strong>
                {patient.duplicateScore > 0 ? <Badge tone="warning">{t('patients.possibleDuplicate')}</Badge> : <span />}
                <span>
                  {formatDate(patient.birthDate, i18n.language)}
                  {years === null ? '' : ` · ${t('patient.ageYears', { count: years })}`}
                  {` · ${sexMarker(patient.administrativeGender)} · ${patient.identifier ?? patient.id}`}
                </span>
              </button>
            );
          }) : <EmptyState>{t('patients.noMatches')}</EmptyState>}
        </div>
      </Panel>

      <Panel title={t('patients.registerTitle')} level={1}>
        <form onSubmit={review}>
          <div className="form-grid">
            <Field label={t('patients.given')} required>
              {id => <input id={id} name="givenName" required maxLength={100} defaultValue={draft?.givenName} />}
            </Field>
            <Field label={t('patients.family')} required>
              {id => <input id={id} name="familyName" required maxLength={100} defaultValue={draft?.familyName} />}
            </Field>
            <Field label={t('patients.birthDate')} required>
              {id => <input id={id} name="birthDate" type="date" required defaultValue={draft?.birthDate} />}
            </Field>
            <Field label={t('patients.gender')} required>
              {id => (
                <select id={id} name="administrativeGender" required defaultValue={draft?.administrativeGender ?? 'unknown'}>
                  <option value="unknown">{t('patients.unknown')}</option>
                  <option value="female">{t('patients.female')}</option>
                  <option value="male">{t('patients.male')}</option>
                  <option value="other">{t('patients.other')}</option>
                </select>
              )}
            </Field>
            <Field label={t('patients.email')}>{id => <input id={id} name="email" type="email" defaultValue={draft?.email ?? ''} />}</Field>
            <Field label={t('patients.phone')}>{id => <input id={id} name="phone" defaultValue={draft?.phone ?? ''} />}</Field>
            <Field label={t('patients.identifier')} hint={t('patients.identifierHint')}>
              {(id, describedBy) => <input id={id} name="identifierValue" aria-describedby={describedBy} defaultValue={draft?.identifierValue ?? ''} />}
            </Field>
            <Field label={t('patients.managingOrganization')} hint={t('patients.managingOrganizationHint')}>
              {(id, describedBy) => <input id={id} name="managingOrganizationId" aria-describedby={describedBy}
                defaultValue={draft?.managingOrganizationId ?? ''} />}
            </Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={duplicates.isPending}>{t('patients.checkDuplicates')}</Button>
          </div>
          <ErrorNotice error={duplicates.error} />
          <ErrorNotice error={register.error} />
        </form>

        {duplicates.isSuccess && draft ? (
          <div className="stack">
            {duplicates.data.length ? (
              <>
                <Notice tone="warning">{t('patients.duplicateWarning', { count: duplicates.data.length })}</Notice>
                <div className="result-list">
                  {duplicates.data.map(candidate => (
                    <button className="result-card" key={candidate.id} onClick={() => onSelect(candidate)}>
                      <strong>{candidate.displayName}</strong>
                      <Badge tone="warning">{t('patients.matchScore', { score: candidate.duplicateScore.toFixed(2) })}</Badge>
                      <span>{formatDate(candidate.birthDate, i18n.language)} · {candidate.identifier ?? candidate.id}</span>
                    </button>
                  ))}
                </div>
              </>
            ) : <Notice tone="info">{t('patients.noDuplicates')}</Notice>}
            <div className="form-actions">
              <Button disabled={register.isPending}
                onClick={() => register.mutate({ ...draft, confirmedUnique: true })}>
                {duplicates.data.length ? t('patients.registerAnyway') : t('patients.register')}
              </Button>
              <Button className="quiet-button" onClick={() => { setDraft(undefined); duplicates.reset(); }}>
                {t('common.cancel')}
              </Button>
            </div>
          </div>
        ) : null}
      </Panel>
    </div>
  );
}
