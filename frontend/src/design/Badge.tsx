import type { PropsWithChildren } from 'react';
import { toneForStatus, type Tone } from './tones';

export function Badge({ tone = 'neutral', children }: PropsWithChildren<{ tone?: Tone }>) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

/** Renders a backend status code as a toned badge. */
export function StatusBadge({ status }: { status?: string | null }) {
  if (!status) return null;
  return <Badge tone={toneForStatus(status)}>{status}</Badge>;
}
