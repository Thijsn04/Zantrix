import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { DirectorySummary, FeatureFlagView, PractitionerSummary } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { Badge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { Tabs } from '../../design/Tabs';
import { PlatformStatus } from './PlatformStatus';

type AdminTab = 'directory' | 'practitioners' | 'features' | 'platform';

/** The administrative control plane: directories, staff, and capability flags. */
export function Administration({ client, canVerifyAudit = false }: {
  client: ApiClient; canVerifyAudit?: boolean;
}) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<AdminTab>('directory');
  return (
    <Panel title={t('admin.title')} level={1} subtitle={t('admin.subtitle')}>
      <Tabs label={t('admin.title')} active={tab} onChange={setTab}
        tabs={[
          { id: 'directory', label: t('admin.directory') },
          { id: 'practitioners', label: t('admin.practitioners') },
          { id: 'features', label: t('admin.features') },
          { id: 'platform', label: t('admin.platform') },
        ]}>
        {tab === 'directory' ? <Directory client={client} /> : null}
        {tab === 'practitioners' ? <Practitioners client={client} /> : null}
        {tab === 'features' ? <FeatureFlags client={client} /> : null}
        {tab === 'platform' ? <PlatformStatus client={client} canVerifyAudit={canVerifyAudit} /> : null}
      </Tabs>
    </Panel>
  );
}

function Directory({ client }: { client: ApiClient }) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [organizationId, setOrganizationId] = useState('');

  const organizations = useQuery({
    queryKey: ['organizations'],
    queryFn: ({ signal }) => client.get<DirectorySummary[]>('/api/v1/admin/organizations', signal),
  });
  const locations = useQuery({
    enabled: Boolean(organizationId),
    queryKey: ['locations', organizationId],
    queryFn: ({ signal }) => client.get<DirectorySummary[]>(
      `/api/v1/admin/locations?organizationId=${encodeURIComponent(organizationId)}`, signal),
  });

  const createOrganization = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/admin/organizations', body),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['organizations'] }),
  });
  const createLocation = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/admin/locations', body),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['locations'] }),
  });

  return (
    <div className="grid-2">
      <div>
        <h3 className="panel-title">{t('admin.organizations')}</h3>
        <ErrorNotice error={organizations.error} />
        <DataTable caption={t('admin.organizations')} rows={organizations.data ?? []} rowKey={row => row.id}
          empty={t('admin.noOrganizations')}
          columns={[
            { header: t('table.name'), cell: row => <span className="primary-cell">{row.name}</span> },
            { header: t('table.actions'), align: 'end', cell: row =>
              <Button className="quiet-button" onClick={() => setOrganizationId(row.id)}>{t('admin.viewLocations')}</Button> },
          ]} />
        <form onSubmit={event => {
          event.preventDefault();
          const form = new FormData(event.currentTarget);
          createOrganization.mutate({ name: String(form.get('name') ?? ''), identifierSystem: null, identifierValue: null });
          event.currentTarget.reset();
        }}>
          <Field label={t('admin.organizationName')} required>{id => <input id={id} name="name" required />}</Field>
          <div className="form-actions"><Button type="submit" disabled={createOrganization.isPending}>{t('common.add')}</Button></div>
        </form>
        <ErrorNotice error={createOrganization.error} />
      </div>

      <div>
        <h3 className="panel-title">{t('admin.locations')}</h3>
        {organizationId ? (
          <>
            <ErrorNotice error={locations.error} />
            <DataTable caption={t('admin.locations')} rows={locations.data ?? []} rowKey={row => row.id}
              empty={t('admin.noLocations')}
              columns={[{ header: t('table.name'), cell: row => <span className="primary-cell">{row.name}</span> }]} />
            <form onSubmit={event => {
              event.preventDefault();
              const form = new FormData(event.currentTarget);
              createLocation.mutate({
                organizationId, name: String(form.get('name') ?? ''),
                typeCode: String(form.get('typeCode') ?? ''), typeDisplay: String(form.get('typeDisplay') ?? ''),
                addressLine: null, city: null, postalCode: null, country: null,
              });
              event.currentTarget.reset();
            }}>
              <div className="form-grid">
                <Field label={t('admin.locationName')} required>{id => <input id={id} name="name" required />}</Field>
                <Field label={t('admin.locationTypeCode')} required hint={t('admin.locationTypeHint')}>
                  {(id, describedBy) => <input id={id} name="typeCode" required aria-describedby={describedBy} defaultValue="HOSP" />}
                </Field>
                <Field label={t('admin.locationTypeDisplay')} required>
                  {id => <input id={id} name="typeDisplay" required defaultValue="Hospital" />}
                </Field>
              </div>
              <div className="form-actions"><Button type="submit" disabled={createLocation.isPending}>{t('common.add')}</Button></div>
            </form>
            <ErrorNotice error={createLocation.error} />
          </>
        ) : <p className="field-hint">{t('admin.selectOrganization')}</p>}
      </div>
    </div>
  );
}

function Practitioners({ client }: { client: ApiClient }) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const practitioners = useQuery({
    queryKey: ['practitioners'],
    queryFn: ({ signal }) => client.get<PractitionerSummary[]>('/api/v1/admin/practitioners', signal),
  });
  const create = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/admin/practitioners', body),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['practitioners'] }),
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    create.mutate({
      identitySubject: String(form.get('identitySubject') ?? ''),
      familyName: String(form.get('familyName') ?? ''),
      givenName: String(form.get('givenName') ?? '') || null,
      roleCode: String(form.get('roleCode') ?? ''),
      roleDisplay: String(form.get('roleDisplay') ?? '') || null,
      organizationId: String(form.get('organizationId') ?? ''),
      locationId: String(form.get('locationId') ?? '') || null,
    });
    event.currentTarget.reset();
  }

  return (
    <>
      <ErrorNotice error={practitioners.error} />
      <DataTable caption={t('admin.practitioners')} rows={practitioners.data ?? []} rowKey={row => row.id}
        empty={t('admin.noPractitioners')}
        columns={[
          { header: t('table.name'), cell: row => <span className="primary-cell">{row.displayName}</span> },
          { header: t('table.role'), cell: row => row.roleDisplay ?? row.roleCode ?? '' },
          { header: t('table.identity'), cell: row => row.identitySubject ?? '' },
          { header: t('table.status'), cell: row => <Badge tone={row.active ? 'success' : 'neutral'}>
            {row.active ? t('common.enabled') : t('common.disabled')}</Badge> },
        ]} />
      <form onSubmit={submit}>
        <div className="form-grid">
          <Field label={t('patients.given')}>{id => <input id={id} name="givenName" />}</Field>
          <Field label={t('patients.family')} required>{id => <input id={id} name="familyName" required />}</Field>
          <Field label={t('admin.identitySubject')} required hint={t('admin.identitySubjectHint')}>
            {(id, describedBy) => <input id={id} name="identitySubject" required aria-describedby={describedBy} />}
          </Field>
          <Field label={t('admin.roleCode')} required>{id => <input id={id} name="roleCode" required />}</Field>
          <Field label={t('admin.roleDisplay')}>{id => <input id={id} name="roleDisplay" />}</Field>
          <Field label={t('admin.organizationId')} required>{id => <input id={id} name="organizationId" required />}</Field>
          <Field label={t('admin.locationId')}>{id => <input id={id} name="locationId" />}</Field>
        </div>
        <div className="form-actions"><Button type="submit" disabled={create.isPending}>{t('common.add')}</Button></div>
      </form>
      <ErrorNotice error={create.error} />
    </>
  );
}

function FeatureFlags({ client }: { client: ApiClient }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const flags = useQuery({
    queryKey: ['features'],
    queryFn: ({ signal }) => client.get<FeatureFlagView[]>('/api/v1/admin/features', signal),
  });
  const toggle = useMutation({
    mutationFn: (flag: FeatureFlagView) => client.put(
      `/api/v1/admin/features/${encodeURIComponent(flag.name)}?enabled=${!flag.enabled}`),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['features'] }),
  });

  return (
    <>
      <ErrorNotice error={flags.error} />
      <ErrorNotice error={toggle.error} />
      <DataTable caption={t('admin.features')} rows={flags.data ?? []} rowKey={row => row.name}
        empty={t('admin.noFeatures')}
        columns={[
          { header: t('table.name'), cell: row => <span className="primary-cell">{row.name}</span> },
          { header: t('table.status'), cell: row => <Badge tone={row.enabled ? 'success' : 'neutral'}>
            {row.enabled ? t('common.enabled') : t('common.disabled')}</Badge> },
          { header: t('table.updated'), cell: row => formatDateTime(row.updatedAt, i18n.language) },
          { header: t('table.actions'), align: 'end', cell: row =>
            <Button className="quiet-button" disabled={toggle.isPending}
              onClick={() => toggle.mutate(row)}>{t('common.toggle')}</Button> },
        ]} />
    </>
  );
}
