import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { NoteSummary } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';
import { useClinicalContext } from './clinicalContext';

const LOINC = 'http://loinc.org';
const CONSULT_NOTE = '11488-4';
const ASSESSMENT_SECTION = '51847-2';

/**
 * Clinical documentation: draft, sign, and add an addendum.
 *
 * A signed note is immutable in the backend, so the UI stops offering an edit
 * path once a note is final and offers an addendum instead. That is the correct
 * clinical correction mechanism and it keeps the version history honest.
 */
export function NotesPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, practitionerId, ready } = useClinicalContext();
  const [addendumFor, setAddendumFor] = useState<NoteSummary>();

  const notes = useQuery({
    queryKey: ['notes', patientId],
    queryFn: ({ signal }) => client.get<NoteSummary[]>(`/api/v1/notes?patientId=${encodeURIComponent(patientId)}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['notes'] });
  const draft = useMutation({ mutationFn: (body: unknown) => client.post('/api/v1/notes', body), onSuccess: invalidate });
  const sign = useMutation({
    mutationFn: (id: string) => client.post(`/api/v1/notes/${encodeURIComponent(id)}/sign`
      + `?patientId=${encodeURIComponent(patientId)}&signerId=${encodeURIComponent(practitionerId)}`),
    onSuccess: invalidate });
  const addendum = useMutation({
    mutationFn: ({ id, text }: { id: string; text: string }) => client.post(
      `/api/v1/notes/${encodeURIComponent(id)}/addendum?patientId=${encodeURIComponent(patientId)}`
      + `&authorId=${encodeURIComponent(practitionerId)}`, text),
    onSuccess: () => { invalidate(); setAddendumFor(undefined); } });

  function submitDraft(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const title = String(form.get('title') ?? '');
    draft.mutate({
      patientId, encounterId, authorId: practitionerId,
      typeSystem: LOINC, typeCode: CONSULT_NOTE, typeDisplay: t('notes.consultNote'), title,
      sections: [{ codeSystem: LOINC, code: ASSESSMENT_SECTION, display: t('notes.assessment'),
        text: String(form.get('text') ?? '') }],
    });
    event.currentTarget.reset();
  }

  function submitAddendum(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!addendumFor) return;
    const form = new FormData(event.currentTarget);
    addendum.mutate({ id: addendumFor.id, text: String(form.get('text') ?? '') });
  }

  return (
    <div className="stack">
      <Panel title={t('chart.notes')}>
        <ErrorNotice error={notes.error} />
        <ErrorNotice error={sign.error} />
        <DataTable caption={t('chart.notes')} rows={notes.data ?? []} rowKey={row => row.id} empty={t('chart.empty')}
          columns={[
            { header: t('table.note'), cell: row => <span className="primary-cell">{row.title}</span> },
            { header: t('table.version'), align: 'end', cell: row => row.version },
            { header: t('table.authored'), cell: row => formatDateTime(row.date, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => (
              <div className="button-row">
                {row.status === 'preliminary' ? (
                  <Button className="quiet-button" disabled={!practitionerId || sign.isPending}
                    onClick={() => sign.mutate(row.id)}>{t('actions.sign')}</Button>
                ) : null}
                {row.status === 'final' ? (
                  <Button className="quiet-button" onClick={() => setAddendumFor(row)}>{t('notes.addAddendum')}</Button>
                ) : null}
              </div>
            ) },
          ]} />
        {!practitionerId ? <p className="field-hint">{t('clinical.practitionerRequired')}</p> : null}
      </Panel>

      {addendumFor ? (
        <Panel title={t('notes.addendumTitle', { title: addendumFor.title })}>
          <Notice tone="info">{t('notes.addendumHint')}</Notice>
          <form onSubmit={submitAddendum}>
            <Field label={t('notes.addendumText')} required>
              {id => <textarea id={id} name="text" required />}
            </Field>
            <div className="form-actions">
              <Button type="submit" disabled={!practitionerId || addendum.isPending}>{t('common.save')}</Button>
              <Button className="quiet-button" onClick={() => setAddendumFor(undefined)}>{t('common.cancel')}</Button>
            </div>
          </form>
          <ErrorNotice error={addendum.error} />
        </Panel>
      ) : null}

      <Panel title={t('notes.draftTitle')}>
        <form onSubmit={submitDraft}>
          <Field label={t('notes.title')} required>{id => <input id={id} name="title" required />}</Field>
          <Field label={t('notes.assessment')} required>{id => <textarea id={id} name="text" required />}</Field>
          <div className="form-actions">
            <Button type="submit" disabled={!ready || draft.isPending}>{t('notes.saveDraft')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <ErrorNotice error={draft.error} />
      </Panel>
    </div>
  );
}
