import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { CarePanel } from './CarePanel';
import type { CareTeamSummary, GoalSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const goal: GoalSummary = {
  id: 'g-1', patientId: 'p-1', description: 'Lower HbA1c below 7 percent',
  lifecycleStatus: 'active', achievementStatus: null, priority: 'high-priority',
  targetDate: '2026-12-01', addressesConditionId: 'c-12',
};
const team: CareTeamSummary = {
  id: 'ct-1', patientId: 'p-1', name: 'Diabetes team', status: 'active',
  members: [{ practitionerId: 'pr-9', roleCode: '158965000', roleDisplay: 'Physician' }],
};

function render({ goals = [goal], teams = [team] }: { goals?: GoalSummary[]; teams?: CareTeamSummary[] } = {}) {
  const api = fakeClient({
    get: vi.fn().mockImplementation((path: string) =>
      Promise.resolve(path.includes('/teams') ? teams : goals)),
  });
  renderWithQuery(<CarePanel client={api.client} patientId="p-1" />);
  return api;
}

describe('CarePanel', () => {
  it('shows the goals and who is on the team', async () => {
    render();
    expect(await screen.findByText('Lower HbA1c below 7 percent')).toBeInTheDocument();
    expect(screen.getByText('Diabetes team')).toBeInTheDocument();
    expect(screen.getByText(/pr-9 \(Physician\)/)).toBeInTheDocument();
  });

  it('does not close a goal until an outcome is chosen', async () => {
    const user = userEvent.setup();
    const api = render();

    await user.click(await screen.findByRole('button', { name: 'Close goal' }));

    // Asking is not closing: the outcome is the point of the action.
    expect(api.fake.post).not.toHaveBeenCalled();
    expect(screen.getByText(/Close: Lower HbA1c/)).toBeInTheDocument();
  });

  it('sends the chosen outcome when closing', async () => {
    const user = userEvent.setup();
    const api = render();

    await user.click(await screen.findByRole('button', { name: 'Close goal' }));
    await user.click(screen.getByRole('button', { name: 'Not achieved' }));

    await waitFor(() => expect(api.fake.post).toHaveBeenCalledWith(
      expect.stringContaining('outcome=not-achieved')));
    expect(api.fake.post.mock.calls[0][0]).toContain('/api/v1/care/goals/g-1/close');
  });

  it('offers no closing action on a goal that is already closed', async () => {
    render({ goals: [{ ...goal, lifecycleStatus: 'completed', achievementStatus: 'achieved' }] });
    expect(await screen.findByText('Achieved')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Close goal' })).not.toBeInTheDocument();
  });

  it('stands a team down rather than removing it', async () => {
    const user = userEvent.setup();
    const api = render();

    await user.click(await screen.findByRole('button', { name: 'Stand down' }));

    await waitFor(() => expect(api.fake.post).toHaveBeenCalledWith(
      expect.stringContaining('/api/v1/care/teams/ct-1/stand-down')));
  });
});
