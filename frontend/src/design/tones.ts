export type Tone = 'neutral' | 'info' | 'success' | 'warning' | 'danger';

/**
 * Clinical status tones. Colour never carries the meaning on its own: a badge
 * always renders the status text, so the tone only reinforces it.
 */
const STATUS_TONES: Record<string, Tone> = {
  active: 'success', 'in-progress': 'info', booked: 'info', planned: 'neutral',
  arrived: 'warning', fulfilled: 'success', finished: 'success', completed: 'success',
  final: 'success', resolved: 'neutral', inactive: 'neutral', cancelled: 'neutral',
  'entered-in-error': 'danger', stopped: 'danger', 'on-hold': 'warning', draft: 'neutral',
  preliminary: 'warning', requested: 'warning', ready: 'info', accepted: 'info',
  high: 'danger', critical: 'danger', urgent: 'danger', asap: 'danger', stat: 'danger',
  routine: 'neutral', low: 'neutral', open: 'warning', revoked: 'neutral',
  success: 'success', denied: 'danger', failure: 'danger',
};

export function toneForStatus(status?: string | null): Tone {
  return (status ? STATUS_TONES[status.toLowerCase()] : undefined) ?? 'neutral';
}
