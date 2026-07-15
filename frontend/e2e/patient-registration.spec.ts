import { expect, test } from '@playwright/test';

test('clinician signs in, registers a patient, and opens the chart', async ({ page }) => {
  const suffix = Date.now().toString(36);
  const givenName = `E${suffix}`;
  const familyName = `P${suffix}`;
  await page.goto('/');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.locator('#username').fill('physician_test');
  await page.locator('#password').fill('test');
  await page.locator('#kc-login').click();

  await expect(page.getByRole('heading', { name: 'Clinical overview' })).toBeVisible();
  await page.getByRole('button', { name: 'Patients' }).click();
  await page.getByLabel('Given name').fill(givenName);
  await page.getByLabel('Family name').fill(familyName);
  await page.getByLabel('Birth date', { exact: true }).fill('1984-07-15');
  await page.getByLabel('Local medical record number').fill(`E2E-${suffix}`);
  await page.getByRole('button', { name: 'Register and review duplicates' }).click();

  await expect(page.getByRole('heading', { name: `${givenName} ${familyName}` })).toBeVisible();
  await expect(page.getByRole('tab', { name: 'Encounters' })).toHaveAttribute('aria-selected', 'true');
});
