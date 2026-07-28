import { describe, expect, it } from 'vitest';
import i18n from './i18n';

// Read every source file through Vite rather than the filesystem, so the test
// needs no Node types and runs in the same environment as the components.
const sources = import.meta.glob('./**/*.{ts,tsx}', { query: '?raw', import: 'default', eager: true });

/** Every `t('some.key')` literal in the source tree, with where it came from. */
function usedKeys(): Map<string, string> {
  const found = new Map<string, string>();
  for (const [path, source] of Object.entries(sources)) {
    if (/\.test\.tsx?$/.test(path)) continue;
    for (const [, key] of String(source).matchAll(/\bt\(\s*'([a-zA-Z][\w.]*\.[\w.]+)'/g)) {
      if (!found.has(key)) found.set(key, path);
    }
  }
  return found;
}

describe('translations', () => {
  /**
   * A missing key renders as the raw key in the interface. Type checking cannot
   * catch that, and it otherwise only surfaces by looking at the screen, which
   * is how a whole block of action labels reached the workspace as `actions.start`.
   */
  it('defines every key the source uses', () => {
    const missing = [...usedKeys()]
      .filter(([key]) => !i18n.exists(key))
      .map(([key, path]) => `${key} (${path})`);
    expect(missing.join('\n')).toBe('');
  });

  it('has no key whose value is the key itself, which renders as one', () => {
    const suspicious = [...usedKeys()]
      .filter(([key]) => i18n.t(key) === key)
      .map(([key, path]) => `${key} (${path})`);
    expect(suspicious.join('\n')).toBe('');
  });

  it('finds the keys it is meant to be checking', () => {
    // Guards the extraction itself: a broken pattern would silently pass.
    const keys = usedKeys();
    expect(keys.size).toBeGreaterThan(100);
    expect(keys.has('chart.snapshot')).toBe(true);
  });
});
