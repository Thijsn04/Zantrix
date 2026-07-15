import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ApiClient, ApiError } from '../lib/api/client';
import type { AppointmentSummary, ClinicalSummary, CurrentUser, Page, PatientSummary, TaskSummary } from '../lib/api/types';
import { Button } from '../design/Button';

export type WorkspacePage = 'home' | 'patients' | 'schedule' | 'tasks' | 'admin' | 'privacy';

export function ClinicalWorkspace({ client, user, page, selected, onSelect }:
  { client: ApiClient; user?: CurrentUser; page: WorkspacePage; selected?: PatientSummary; onSelect: (patient?: PatientSummary) => void }) {
  if (page === 'patients') return selected
    ? <PatientChart client={client} patient={selected} onClose={() => onSelect(undefined)} />
    : <PatientRegistry client={client} onSelect={onSelect} />;
  if (page === 'schedule') return <Scheduling client={client} patient={selected} />;
  if (page === 'tasks') return <Worklist client={client} patient={selected} />;
  if (page === 'admin') return <Administration client={client} />;
  if (page === 'privacy') return <Privacy client={client} />;
  return <Dashboard client={client} user={user} patient={selected} />;
}

function Panel({ title, children }: { title: string; children: React.ReactNode }) {
  return <section className="panel"><h1>{title}</h1>{children}</section>;
}

function ErrorNotice({ error }: { error: unknown }) {
  const { t } = useTranslation();
  if (!error) return null;
  return <p className="error" role="alert">{error instanceof ApiError ? error.message : t('common.failed')}</p>;
}

function Dashboard({ client, user, patient }: { client: ApiClient; user?: CurrentUser; patient?: PatientSummary }) {
  const { t } = useTranslation();
  const tasks = useQuery({ queryKey: ['tasks', patient?.id], queryFn: ({ signal }) => client.get<TaskSummary[]>(
    `/api/v1/tasks${patient ? `?patientId=${encodeURIComponent(patient.id)}` : ''}`, signal) });
  return <Panel title={t('dashboard.title')}>
    <p>{t('dashboard.welcome', { name: user?.displayName ?? user?.username ?? t('session.signedIn') })}</p>
    <div className="metric-grid"><article><strong>{tasks.data?.length ?? 0}</strong><span>{t('dashboard.openTasks')}</span></article>
      <article><strong>{patient ? '1' : '0'}</strong><span>{t('dashboard.patientContext')}</span></article></div>
    <ErrorNotice error={tasks.error} />
  </Panel>;
}

function PatientRegistry({ client, onSelect }: { client: ApiClient; onSelect: (patient: PatientSummary) => void }) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const patients = useQuery({ queryKey: ['patients', query], queryFn: ({ signal }) => client.get<PatientSummary[]>(
    `/api/v1/patients${query ? `?query=${encodeURIComponent(query)}` : ''}`, signal) });
  const register = useMutation({ mutationFn: (body: unknown) => client.post<PatientSummary>('/api/v1/patients', body),
    onSuccess: patient => { void queryClient.invalidateQueries({ queryKey: ['patients'] }); onSelect(patient); } });
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); const data = new FormData(event.currentTarget);
    register.mutate({ givenName: data.get('givenName'), familyName: data.get('familyName'), birthDate: data.get('birthDate'),
      administrativeGender: data.get('gender'), email: data.get('email') || null, phone: data.get('phone') || null,
      identifierSystem: data.get('identifierValue') ? 'https://zantrix.org/identifier/local-mrn' : null,
      identifierValue: data.get('identifierValue') || null, confirmedUnique: false });
  }
  return <div className="two-column"><Panel title={t('patients.searchTitle')}>
    <label>{t('patients.searchLabel')}<input value={query} onChange={e => setQuery(e.target.value)} type="search" /></label>
    <ErrorNotice error={patients.error} /><div className="card-list">{patients.data?.map(patient =>
      <button className="record-card" key={patient.id} onClick={() => onSelect(patient)}><strong>{patient.displayName}</strong>
        <span>{patient.birthDate} · {patient.identifier ?? patient.id}</span></button>)}</div>
  </Panel><Panel title={t('patients.registerTitle')}><form className="form-grid" onSubmit={submit}>
    <label>{t('patients.given')}<input name="givenName" required maxLength={100} /></label>
    <label>{t('patients.family')}<input name="familyName" required maxLength={100} /></label>
    <label>{t('patients.birthDate')}<input name="birthDate" required type="date" /></label>
    <label>{t('patients.gender')}<select name="gender" required><option value="unknown">{t('patients.unknown')}</option><option value="female">{t('patients.female')}</option><option value="male">{t('patients.male')}</option><option value="other">{t('patients.other')}</option></select></label>
    <label>{t('patients.email')}<input name="email" type="email" /></label><label>{t('patients.phone')}<input name="phone" /></label>
    <label>{t('patients.identifier')}<input name="identifierValue" /></label><Button type="submit" disabled={register.isPending}>{t('patients.register')}</Button>
    <ErrorNotice error={register.error} /></form></Panel></div>;
}

const chartTabs = [
  ['encounters', '/api/v1/encounters?patientId='], ['problems', '/api/v1/problems?patientId='],
  ['allergies', '/api/v1/allergies?patientId='], ['medications', '/api/v1/medications?patientId='],
  ['vitals', '/api/v1/vitals?patientId='], ['orders', '/api/v1/orders?patientId='],
  ['results', '/api/v1/orders/results?patientId='], ['notes', '/api/v1/notes?patientId=']
] as const;

function PatientChart({ client, patient, onClose }: { client: ApiClient; patient: PatientSummary; onClose: () => void }) {
  const { t } = useTranslation(); const queryClient = useQueryClient(); const [tab, setTab] = useState<(typeof chartTabs)[number][0]>('encounters'); const [signerId, setSignerId] = useState('');
  const endpoint = chartTabs.find(value => value[0] === tab)![1];
  const records = useQuery({ queryKey: ['chart', patient.id, tab], queryFn: ({ signal }) => client.get<ClinicalSummary[]>(endpoint + encodeURIComponent(patient.id), signal) });
  return <Panel title={patient.displayName}><div className="patient-demographics"><span>{patient.birthDate}</span><span>{patient.administrativeGender}</span><span>{patient.identifier ?? patient.id}</span><Button className="quiet-button" onClick={onClose}>{t('patients.change')}</Button></div>
    <div className="tabs" role="tablist">{chartTabs.map(([name]) => <button role="tab" aria-selected={tab === name} key={name} onClick={() => setTab(name)}>{t(`chart.${name}`)}</button>)}</div>
    {tab === 'notes' && <label>{t('clinical.signerId')}<input value={signerId} onChange={event => setSignerId(event.target.value)} /></label>}
    <QuickEntry client={client} patient={patient} tab={tab} />
    <ErrorNotice error={records.error} /><Records records={records.data ?? []} empty={t('chart.empty')} action={(record) => {
      const verb = tab === 'encounters' ? (record.status === 'planned' ? 'start' : record.status === 'in-progress' ? 'finish' : null)
        : tab === 'problems' && record.clinicalStatus === 'active' ? 'resolve' : tab === 'notes' && record.status === 'preliminary' ? 'sign' : null;
      if (!verb) return null;
      const params = tab === 'notes' ? `patientId=${patient.id}&signerId=${encodeURIComponent(signerId)}` : `patientId=${patient.id}`;
      return <Button className="quiet-button" disabled={tab === 'notes' && !signerId} onClick={() => void client.post(`/api/v1/${tab}/${record.id}/${verb}?${params}`).then(() => queryClient.invalidateQueries({ queryKey: ['chart', patient.id, tab] }))}>{t(`actions.${verb}`)}</Button>;
    }} />
  </Panel>;
}

function Records({ records, empty, action }: { records: ClinicalSummary[]; empty: string; action?: (record: ClinicalSummary) => React.ReactNode }) {
  if (!records.length) return <p className="muted">{empty}</p>;
  return <div className="record-table" role="table">{records.map(record => <article role="row" key={record.id}>
    <strong>{record.display ?? record.title ?? record.serviceDisplay ?? record.description ?? record.code ?? record.id}</strong>
    <span>{record.status ?? record.dosage ?? record.value ?? ''} {record.unit ?? ''}</span>
    <small>{record.start ?? record.observedAt ?? record.authoredAt ?? ''}</small>{action?.(record)}</article>)}</div>;
}

function QuickEntry({ client, patient, tab }: { client: ApiClient; patient: PatientSummary; tab: string }) {
  const { t } = useTranslation(); const queryClient = useQueryClient(); const [open, setOpen] = useState(false);
  const mutation = useMutation({ mutationFn: ({ path, body }: { path: string; body: unknown }) => client.post(path, body),
    onSuccess: () => { setOpen(false); void queryClient.invalidateQueries({ queryKey: ['chart', patient.id] }); } });
  if (!['encounters', 'problems', 'allergies', 'medications', 'vitals', 'orders', 'results', 'notes'].includes(tab)) return null;
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); const f = new FormData(event.currentTarget); const encounterId = String(f.get('encounterId') ?? '');
    const base = { patientId: patient.id, encounterId, performerId: f.get('practitionerId'), requesterId: f.get('practitionerId'), authorId: f.get('practitionerId') };
    const request = tab === 'encounters' ? { path: '/api/v1/encounters', body: { patientId: patient.id, practitionerId: f.get('practitionerId'), reasonCode: f.get('code'), reasonDisplay: f.get('display'), plannedStart: new Date().toISOString() } }
      : tab === 'problems' ? { path: '/api/v1/problems', body: { ...base, codeSystem: 'http://snomed.info/sct', code: f.get('code'), display: f.get('display'), note: f.get('note') } }
      : tab === 'allergies' ? { path: '/api/v1/allergies', body: { ...base, substanceSystem: 'http://snomed.info/sct', substanceCode: f.get('code'), substanceDisplay: f.get('display'), category: 'medication', criticality: 'unable-to-assess', note: f.get('note') } }
      : tab === 'medications' ? { path: '/api/v1/medications', body: { ...base, rxNormIngredientCode: f.get('code'), medicationDisplay: f.get('display'), dosageText: f.get('note'), doseValue: Number(f.get('value')), doseUnit: f.get('unit'), frequencyPerDay: Number(f.get('frequency')), durationDays: Number(f.get('duration')), dispenseQuantity: Number(f.get('quantity')), repeats: 0, safetyOverrideReason: null } }
      : tab === 'vitals' ? { path: '/api/v1/vitals', body: { ...base, observedAt: new Date().toISOString(), measurements: [{ loincCode: f.get('code'), display: f.get('display'), value: Number(f.get('value')), unit: f.get('unit') }] } }
      : tab === 'orders' ? { path: '/api/v1/orders', body: { ...base, category: 'laboratory', codeSystem: 'http://loinc.org', code: f.get('code'), display: f.get('display'), priority: 'routine', clinicalNote: f.get('note') } }
      : tab === 'results' ? { path: `/api/v1/orders/${encodeURIComponent(String(f.get('orderId')))}/results`, body: { patientId: patient.id, performerId: f.get('practitionerId'), issuedAt: new Date().toISOString(), measurements: [{ codeSystem: 'http://loinc.org', code: f.get('code'), display: f.get('display'), numericValue: Number(f.get('value')), unit: f.get('unit'), textValue: null }], conclusion: f.get('note') } }
      : { path: '/api/v1/notes', body: { ...base, typeSystem: 'http://loinc.org', typeCode: f.get('code'), typeDisplay: f.get('display'), title: f.get('display'), sections: [{ codeSystem: 'http://loinc.org', code: '51847-2', display: t('notes.assessment'), text: f.get('note') }] } };
    mutation.mutate(request);
  }
  return <div className="quick-entry"><Button className="quiet-button" onClick={() => setOpen(!open)}>{open ? t('common.cancel') : t('common.add')}</Button>{open && <form className="inline-form" onSubmit={submit}>
    {tab !== 'encounters' && tab !== 'results' && <label>{t('clinical.encounterId')}<input name="encounterId" required /></label>}
    {tab === 'results' && <label>{t('clinical.orderId')}<input name="orderId" required /></label>}
    <label>{t('clinical.practitionerId')}<input name="practitionerId" required /></label><label>{t('clinical.code')}<input name="code" required /></label>
    <label>{t('clinical.display')}<input name="display" required /></label>{['vitals', 'medications', 'results'].includes(tab) && <><label>{t('clinical.value')}<input name="value" required type="number" step="any" /></label><label>{t('clinical.unit')}<input name="unit" required /></label></>}
    {tab === 'medications' && <><label>{t('clinical.frequency')}<input name="frequency" required type="number" min="1" /></label><label>{t('clinical.duration')}<input name="duration" required type="number" min="1" /></label><label>{t('clinical.quantity')}<input name="quantity" required type="number" min="0.01" step="any" /></label></>}
    {tab !== 'encounters' && <label>{t('clinical.note')}<textarea name="note" /></label>}<Button type="submit" disabled={mutation.isPending}>{t('common.save')}</Button><ErrorNotice error={mutation.error} /></form>}</div>;
}

function Scheduling({ client, patient }: { client: ApiClient; patient?: PatientSummary }) {
  const { t } = useTranslation(); const queryClient = useQueryClient();
  const appointments = useQuery({ enabled: Boolean(patient), queryKey: ['appointments', patient?.id], queryFn: ({ signal }) => client.get<AppointmentSummary[]>(`/api/v1/appointments?patientId=${patient!.id}`, signal) });
  const book = useMutation({ mutationFn: (body: unknown) => client.post('/api/v1/appointments', body), onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['appointments'] }) });
  function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!patient) return; const f = new FormData(event.currentTarget);
    book.mutate({ patientId: patient.id, practitionerId: f.get('practitionerId'), locationId: f.get('locationId') || null, slotId: null,
      start: new Date(String(f.get('start'))).toISOString(), end: new Date(String(f.get('end'))).toISOString(), serviceCode: f.get('serviceCode'), serviceDisplay: f.get('serviceDisplay'), comment: f.get('comment') }); }
  return <Panel title={t('schedule.title')}>{!patient ? <p>{t('schedule.selectPatient')}</p> : <><form className="form-grid" onSubmit={submit}>
    <label>{t('clinical.practitionerId')}<input name="practitionerId" required /></label><label>{t('schedule.location')}<input name="locationId" /></label>
    <label>{t('schedule.start')}<input name="start" type="datetime-local" required /></label><label>{t('schedule.end')}<input name="end" type="datetime-local" required /></label>
    <label>{t('clinical.code')}<input name="serviceCode" required /></label><label>{t('schedule.service')}<input name="serviceDisplay" required /></label>
    <label>{t('clinical.note')}<input name="comment" /></label><Button type="submit">{t('schedule.book')}</Button><ErrorNotice error={book.error} /></form>
    <Records records={appointments.data ?? []} empty={t('chart.empty')} /></>}</Panel>;
}

function Worklist({ client, patient }: { client: ApiClient; patient?: PatientSummary }) {
  const { t } = useTranslation(); const queryClient = useQueryClient();
  const path = `/api/v1/tasks${patient ? `?patientId=${patient.id}` : ''}`;
  const tasks = useQuery({ queryKey: ['worklist', patient?.id], queryFn: ({ signal }) => client.get<TaskSummary[]>(path, signal) });
  const action = useMutation({ mutationFn: ({ id, patientId, verb }: { id: string; patientId: string; verb: string }) => client.post(`/api/v1/tasks/${id}/${verb}?patientId=${patientId}`), onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['worklist'] }) });
  return <Panel title={t('tasks.title')}><ErrorNotice error={tasks.error} /><div className="record-table">{tasks.data?.map(task => <article key={task.id}><strong>{task.description}</strong><span>{task.status} · {task.priority}</span><div><Button className="quiet-button" onClick={() => action.mutate({ id: task.id, patientId: task.patientId, verb: 'start' })}>{t('tasks.start')}</Button> <Button onClick={() => action.mutate({ id: task.id, patientId: task.patientId, verb: 'complete' })}>{t('tasks.complete')}</Button></div></article>)}</div></Panel>;
}

function Administration({ client }: { client: ApiClient }) {
  const { t } = useTranslation(); const queryClient = useQueryClient();
  const flags = useQuery({ queryKey: ['features'], queryFn: ({ signal }) => client.get<{ name: string; enabled: boolean }[]>('/api/v1/admin/features', signal) });
  const toggle = useMutation({ mutationFn: (flag: { name: string; enabled: boolean }) => client.put(`/api/v1/admin/features/${flag.name}?enabled=${!flag.enabled}`), onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['features'] }) });
  return <Panel title={t('admin.title')}><ErrorNotice error={flags.error} /><div className="record-table">{flags.data?.map(flag => <article key={flag.name}><strong>{flag.name}</strong><span>{flag.enabled ? t('common.enabled') : t('common.disabled')}</span><Button className="quiet-button" onClick={() => toggle.mutate(flag)}>{t('common.toggle')}</Button></article>)}</div></Panel>;
}

function Privacy({ client }: { client: ApiClient }) {
  const { t } = useTranslation();
  const reviews = useQuery({ queryKey: ['emergency-reviews'], queryFn: ({ signal }) => client.get<ClinicalSummary[]>('/api/v1/privacy/emergency-reviews', signal) });
  const audit = useQuery({ queryKey: ['audit'], queryFn: ({ signal }) => client.get<Page<Record<string, unknown>>>('/api/v1/audit/events?size=50', signal) });
  return <Panel title={t('privacy.title')}><h2>{t('privacy.emergency')}</h2><Records records={reviews.data ?? []} empty={t('chart.empty')} />
    <h2>{t('privacy.audit')}</h2><div className="record-table">{audit.data?.content.map((event, index) => <article key={String(event.id ?? index)}><strong>{String(event.action ?? '')} {String(event.resourceType ?? '')}</strong><span>{String(event.actor ?? '')} · {String(event.outcome ?? '')}</span><small>{String(event.recordedAt ?? '')}</small></article>)}</div><ErrorNotice error={reviews.error ?? audit.error} /></Panel>;
}
