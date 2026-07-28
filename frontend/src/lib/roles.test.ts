import { describe, expect, it } from 'vitest';
import { can, primaryRole } from './roles';
import type { CurrentUser } from './api/types';

function user(...roles: string[]): CurrentUser {
  return { subject: 's-1', username: 'test', displayName: 'Test', roles, scopes: ['user/*.cruds'] };
}

describe('capabilities', () => {
  it('matches the backend rule that a pharmacist cannot reach the patient directory', () => {
    expect(can(user('PHARMACIST'), 'patients')).toBe(false);
    expect(can(user('PHARMACIST'), 'medications')).toBe(true);
    expect(can(user('PHARMACIST'), 'dispense')).toBe(true);
  });

  it('allows only a pharmacist to dispense', () => {
    expect(can(user('PHYSICIAN'), 'dispense')).toBe(false);
    expect(can(user('NURSE'), 'dispense')).toBe(false);
  });

  it('allows only a physician or pharmacist to prescribe', () => {
    expect(can(user('PHYSICIAN'), 'prescribe')).toBe(true);
    expect(can(user('PHARMACIST'), 'prescribe')).toBe(true);
    expect(can(user('NURSE'), 'prescribe')).toBe(false);
  });

  it('keeps privacy and administration areas separate', () => {
    expect(can(user('PRIVACY_OFFICER'), 'privacy')).toBe(true);
    expect(can(user('PRIVACY_OFFICER'), 'administration')).toBe(false);
    expect(can(user('ADMIN'), 'administration')).toBe(true);
    expect(can(user('ADMIN'), 'privacy')).toBe(false);
  });

  it('does not grant a privacy officer clinical access', () => {
    expect(can(user('PRIVACY_OFFICER'), 'chart')).toBe(false);
    expect(can(user('PRIVACY_OFFICER'), 'orders')).toBe(false);
    expect(can(user('PRIVACY_OFFICER'), 'patients')).toBe(false);
  });

  it('treats an unknown or absent user as having no capability', () => {
    expect(can(undefined, 'patients')).toBe(false);
    expect(can(user('PATIENT'), 'chart')).toBe(false);
  });
});

describe('primaryRole', () => {
  it('prefers the clinical role when a user holds several', () => {
    expect(primaryRole(user('ADMIN', 'PHYSICIAN'))).toBe('PHYSICIAN');
    expect(primaryRole(user('ADMIN', 'NURSE'))).toBe('NURSE');
  });

  it('falls back to patient when no staff role is present', () => {
    expect(primaryRole(user())).toBe('PATIENT');
  });
});
