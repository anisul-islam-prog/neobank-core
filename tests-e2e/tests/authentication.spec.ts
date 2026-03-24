import { test, expect } from '@playwright/test';

/**
 * Authentication E2E Tests
 * Tests for login, logout, and session management across all portals
 */

test.describe('Authentication', () => {
  const retailAppUrl = process.env.RETAIL_APP_URL || 'http://localhost:3000';
  const staffPortalUrl = process.env.STAFF_PORTAL_URL || 'http://localhost:3001';
  const adminConsoleUrl = process.env.ADMIN_CONSOLE_URL || 'http://localhost:3002';

  test.describe('Retail App Authentication', () => {
    test('should display login form and handle invalid credentials', async ({ page }) => {
      await page.goto(retailAppUrl);

      // Verify login form is displayed
      await expect(page.getByPlaceholder('Username')).toBeVisible();
      await expect(page.getByPlaceholder('Password')).toBeVisible();
      await expect(page.getByRole('button', { name: /sign in/i })).toBeVisible();

      // Try to login with invalid credentials
      await page.getByPlaceholder('Username').fill('invalid_user');
      await page.getByPlaceholder('Password').fill('wrong_password');
      await page.getByRole('button', { name: /sign in/i }).click();

      // Wait for error message
      await expect(page.getByRole('alert')).toBeVisible({ timeout: 5000 });
    });

    test('should redirect to dashboard after successful login', async ({ page }) => {
      // Use demo credentials if available
      const demoUsername = 'customer_john';
      const demoPassword = 'demo123!';

      await page.goto(retailAppUrl);
      await page.getByPlaceholder('Username').fill(demoUsername);
      await page.getByPlaceholder('Password').fill(demoPassword);
      await page.getByRole('button', { name: /sign in/i }).click();

      // Wait for redirect to dashboard
      await expect(page).toHaveURL(/.*dashboard.*/, { timeout: 10000 });

      // Verify dashboard content
      await expect(page.getByText(/account|balance/i)).toBeVisible();
    });

    test('should logout successfully', async ({ page }) => {
      const demoUsername = 'customer_john';
      const demoPassword = 'demo123!';

      // Login first
      await page.goto(retailAppUrl);
      await page.getByPlaceholder('Username').fill(demoUsername);
      await page.getByPlaceholder('Password').fill(demoPassword);
      await page.getByRole('button', { name: /sign in/i }).click();
      await expect(page).toHaveURL(/.*dashboard.*/, { timeout: 10000 });

      // Logout
      await page.getByRole('button', { name: /logout/i }).click();

      // Verify redirected to login
      await expect(page.getByPlaceholder('Username')).toBeVisible({ timeout: 5000 });
    });
  });

  test.describe('Staff Portal Authentication', () => {
    test('should display staff portal branding', async ({ page }) => {
      await page.goto(staffPortalUrl);

      // Verify staff portal branding
      await expect(page.getByText(/staff|portal/i)).toBeVisible();
    });

    test('should handle staff login with demo credentials', async ({ page }) => {
      const staffUsername = 'manager_bob';
      const staffPassword = 'demo123!';

      await page.goto(staffPortalUrl);
      await page.getByPlaceholder('Username').fill(staffUsername);
      await page.getByPlaceholder('Password').fill(staffPassword);
      await page.getByRole('button', { name: /sign in/i }).click();

      // Wait for dashboard
      await expect(page.getByText(/dashboard|customers|loans|kyc/i)).toBeVisible({ timeout: 10000 });
    });
  });

  test.describe('Admin Console Authentication', () => {
    test('should display admin console branding', async ({ page }) => {
      await page.goto(adminConsoleUrl);

      // Verify admin console branding
      await expect(page.getByText(/admin|console/i)).toBeVisible();
    });

    test('should handle admin login with demo credentials', async ({ page }) => {
      const adminUsername = 'admin_alice';
      const adminPassword = 'demo123!';

      await page.goto(adminConsoleUrl);
      await page.getByPlaceholder('Username').fill(adminUsername);
      await page.getByPlaceholder('Password').fill(adminPassword);
      await page.getByRole('button', { name: /sign in/i }).click();

      // Wait for dashboard
      await expect(page.getByText(/admin|audit|users|branches/i)).toBeVisible({ timeout: 10000 });
    });
  });

  test.describe('Session Management', () => {
    test('should preserve session on page refresh', async ({ page }) => {
      const demoUsername = 'customer_john';
      const demoPassword = 'demo123!';

      // Login
      await page.goto(retailAppUrl);
      await page.getByPlaceholder('Username').fill(demoUsername);
      await page.getByPlaceholder('Password').fill(demoPassword);
      await page.getByRole('button', { name: /sign in/i }).click();
      await expect(page).toHaveURL(/.*dashboard.*/, { timeout: 10000 });

      // Refresh page
      await page.reload();

      // Verify still logged in
      await expect(page.getByText(/account|balance/i)).toBeVisible({ timeout: 5000 });
    });

    test('should redirect to login when accessing protected route without auth', async ({ page, context }) => {
      // Clear any existing session
      await context.clearCookies();

      // Try to access dashboard directly
      await page.goto(`${retailAppUrl}/dashboard`);

      // Should redirect to login
      await expect(page.getByPlaceholder('Username')).toBeVisible({ timeout: 5000 });
    });
  });
});
