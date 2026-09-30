import { test, expect } from '@playwright/test';

const policies = [
  {
    id: 'POL-NEAR',
    holderName: 'Alex Johnson',
    planName: 'Near Renewal Plan',
    coverageAmount: 100000,
    status: 'active',
    startDate: '2025-01-01',
    endDate: '2026-10-10',
  },
  {
    id: 'POL-LATER',
    holderName: 'Alex Johnson',
    planName: 'Later Renewal Plan',
    coverageAmount: 200000,
    status: 'active',
    startDate: '2025-01-01',
    endDate: '2026-10-20',
  },
];

const reminders = [
  { policyId: 'POL-NEAR', planName: 'Near Renewal Plan', endDate: '2026-10-10', daysRemaining: 10 },
  { policyId: 'POL-LATER', planName: 'Later Renewal Plan', endDate: '2026-10-20', daysRemaining: 20 },
];

async function mockDashboardApi(page, renewalData) {
  await page.route('**/api/policies/renewals', route => route.fulfill({ json: renewalData }));
  await page.route(/\/api\/policies$/, route => route.fulfill({ json: policies }));
  await page.route(/\/api\/claims$/, route => route.fulfill({ json: [] }));
}

test.describe('Policy renewal reminders', () => {
  test('shows ordered reminders, links to policy details, and dismisses for the current view', async ({ page }) => {
    await mockDashboardApi(page, reminders);
    await page.goto('/');

    const firstReminder = page.getByTestId('renewal-POL-NEAR');
    await expect(firstReminder).toContainText('Policy ID: POL-NEAR');
    await expect(firstReminder).toContainText('10 calendar days remaining');
    await expect(page.getByTestId('renewal-POL-LATER')).toBeVisible();
    await expect(page.locator('.renewal-reminder').first()).toHaveAttribute('data-testid', 'renewal-POL-NEAR');
    await page.getByTestId('renewal-link-POL-LATER').click();
    await expect(page.getByTestId('policy-card')).toContainText('Later Renewal Plan');

    await page.getByTestId('dismiss-renewal-POL-NEAR').click();
    await expect(page.getByTestId('renewal-POL-NEAR')).not.toBeVisible();
    await page.reload();
    await expect(page.getByTestId('renewal-POL-NEAR')).toBeVisible();
  });

  test('shows an empty state when there are no eligible policies', async ({ page }) => {
    await mockDashboardApi(page, []);
    await page.goto('/');

    await expect(page.getByTestId('renewal-empty-state')).toHaveText('No policies are nearing renewal.');
  });

  test('does not show the empty state when authentication is required', async ({ page }) => {
    await page.route('**/api/policies/renewals', route => route.fulfill({ status: 401 }));
    await page.route(/\/api\/policies$/, route => route.fulfill({ json: policies }));
    await page.route(/\/api\/claims$/, route => route.fulfill({ json: [] }));
    await page.goto('/');

    await expect(page.getByTestId('renewal-auth-required')).toHaveText('Sign in to view renewal reminders.');
    await expect(page.getByTestId('renewal-empty-state')).toHaveCount(0);
  });
});
