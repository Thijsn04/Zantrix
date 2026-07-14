import { useTranslation } from 'react-i18next';

/**
 * Placeholder application root.
 *
 * The previous application shell, pages, and API layer were removed during the
 * Milestone 0 rebuild. The real application shell and design system are built
 * next. See docs/architecture/frontend.md.
 */
export default function App() {
  const { t } = useTranslation();

  return (
    <main className="min-h-screen flex items-center justify-center px-6">
      <div className="max-w-md text-center">
        <h1 className="text-2xl font-semibold tracking-tight">{t('app.title')}</h1>
        <p className="mt-2 text-sm text-slate-500">{t('app.rebuilding')}</p>
      </div>
    </main>
  );
}
