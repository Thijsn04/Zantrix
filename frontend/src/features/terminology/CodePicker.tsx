import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { TermConcept } from '../../lib/api/types';
import { Field } from '../../design/Field';
import { ErrorNotice, Notice } from '../../design/Feedback';

export type SnomedDomain = 'clinical-finding' | 'substance' | 'procedure';

/**
 * SNOMED CT concept search backed by the terminology service.
 *
 * Terminology fails closed: without a licensed RF2 edition loaded, search
 * returns an error and coded entry is unavailable. The picker therefore always
 * shows why it cannot offer concepts rather than silently returning nothing,
 * and it never lets the clinician invent a code.
 */
export function CodePicker({ client, domain, label, value, onChange }: {
  client: ApiClient;
  domain: SnomedDomain;
  label: string;
  value?: TermConcept;
  onChange: (concept: TermConcept | undefined) => void;
}) {
  const { t } = useTranslation();
  const [filter, setFilter] = useState('');
  const active = filter.trim().length >= 3;

  const concepts = useQuery({
    enabled: active && !value,
    queryKey: ['snomed', domain, filter.trim()],
    queryFn: ({ signal }) => client.get<TermConcept[]>(
      `/api/v1/terminology/snomed?domain=${domain}&filter=${encodeURIComponent(filter.trim())}&count=15`, signal),
  });

  if (value) {
    return (
      <div className="field">
        <span className="field-label-static">{label}</span>
        <Notice tone="info">
          {value.display} ({value.code})
          <button type="button" className="link-button" onClick={() => { onChange(undefined); setFilter(''); }}>
            {t('common.change')}
          </button>
        </Notice>
      </div>
    );
  }

  return (
    <div className="code-picker">
      <Field label={label} hint={t('terminology.searchHint')}>
        {(id, describedBy) => (
          <input id={id} type="search" value={filter} aria-describedby={describedBy}
            onChange={event => setFilter(event.target.value)} autoComplete="off" />
        )}
      </Field>
      <ErrorNotice error={concepts.error} />
      {active && concepts.data ? (
        concepts.data.length ? (
          <ul className="concept-list">
            {concepts.data.map(concept => (
              <li key={concept.code}>
                <button type="button" onClick={() => onChange(concept)}>
                  <strong>{concept.display}</strong><span>{concept.code}</span>
                </button>
              </li>
            ))}
          </ul>
        ) : <p className="field-hint">{t('terminology.noConcepts')}</p>
      ) : null}
    </div>
  );
}
