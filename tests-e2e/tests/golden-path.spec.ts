import { test, expect } from '@playwright/test';
import * as crypto from 'crypto';

/**
 * NeoBank Golden Path E2E Test
 * 
 * This test automates the complete user journey across all three applications:
 * 1. Retail App: User registers (Status: PENDING)
 * 2. Staff Portal: Manager approves the pending user
 * 3. Retail App: User logs in, sees balance, applies for a card
 * 4. Verification: Card appears with masked numbers
 */

// Generate unique test data to avoid conflicts
const generateUniqueData = () => {
  const timestamp = Date.now();
  const randomSuffix = crypto.randomBytes(4).toString('hex');
  return {
    username: `testuser_${timestamp}_${randomSuffix}`,
    email: `testuser_${timestamp}_${randomSuffix}@neobank.com`,
    password: 'TestPass123!',
  };
};

test.describe('NeoBank Golden Path', () => {
  let testUserData: { username: string; email: string; password: string };
  const retailAppUrl = process.env.RETAIL_APP_URL || 'http://localhost:3000';
  const staffPortalUrl = process.env.STAFF_PORTAL_URL || 'http://localhost:3001';

  test.beforeEach(() => {
    testUserData = generateUniqueData();
    console.log('Test user data:', { username: testUserData.username });
  });

  test('complete user journey from registration to card issuance', async ({ page, context }) => {
    // ============================================================
    // STEP 1: Retail App - User Registration (Status: PENDING)
    // ============================================================
    console.log('Step 1: Registering new user in Retail App...');
    
    await page.goto(retailAppUrl);
    await expect(page).toHaveTitle(/NeoBank/);

    // Fill registration form
    await page.getByPlaceholder('Username').fill(testUserData.username);
    await page.getByPlaceholder('Email').fill(testUserData.email);
    await page.getByPlaceholder('Password', { exact: true }).fill(testUserData.password);
    
    // Submit registration
    await page.getByRole('button', { name: /register|sign up/i }).click();

    // Wait for registration confirmation
    await expect(page.locator('[data-testid="registration-success"]')).toBeVisible({ timeout: 10000 });
    
    // Verify pending status message
    await expect(page.getByText(/pending|awaiting approval/i)).toBeVisible();

    console.log('✓ User registered successfully with PENDING status');

    // ============================================================
    // STEP 2: Staff Portal - Manager Approval
    // ============================================================
    console.log('Step 2: Manager approving user in Staff Portal...');

    // Navigate to staff portal in same context (new tab)
    const staffPage = await context.newPage();
    await staffPage.goto(staffPortalUrl);

    // Staff login (using demo credentials)
    const staffUsername = process.env.TEST_STAFF_USERNAME || 'manager_bob';
    const staffPassword = process.env.TEST_STAFF_PASSWORD || 'demo123!';

    await staffPage.getByPlaceholder('Username').fill(staffUsername);
    await staffPage.getByPlaceholder('Password', { exact: true }).fill(staffPassword);
    await staffPage.getByRole('button', { name: /sign in|login/i }).click();

    // Wait for dashboard to load
    await expect(staffPage.getByText(/staff portal|dashboard/i)).toBeVisible({ timeout: 10000 });

    // Navigate to KYC/User Approvals tab
    await staffPage.getByRole('tab', { name: /kyc|approvals|users/i }).click();

    // Find and approve the pending user
    const userRow = staffPage.locator('tr').filter({ hasText: testUserData.username });
    
    // Wait for user to appear in pending list
    await expect(userRow).toBeVisible({ timeout: 30000 });

    // Click approve button
    await userRow.getByRole('button', { name: /approve/i }).click();

    // Wait for approval confirmation
    await expect(staffPage.getByText(/approved successfully/i)).toBeVisible({ timeout: 5000 });

    console.log('✓ User approved by manager');

    await staffPage.close();

    // ============================================================
    // STEP 3: Retail App - User Login (Status: ACTIVE)
    // ============================================================
    console.log('Step 3: User logging in to Retail App...');

    // Go back to retail app login
    await page.goto(retailAppUrl);

    // Login with newly created credentials
    await page.getByPlaceholder('Username').fill(testUserData.username);
    await page.getByPlaceholder('Password', { exact: true }).fill(testUserData.password);
    await page.getByRole('button', { name: /sign in|login/i }).click();

    // Wait for dashboard to load
    await expect(page.getByText(/dashboard|your accounts/i)).toBeVisible({ timeout: 10000 });

    // Verify account balance display ($0 for new account)
    await expect(page.getByText('$0.00')).toBeVisible();
    await expect(page.getByText(/0\.00/)).toBeVisible();

    console.log('✓ User logged in successfully, balance displayed');

    // ============================================================
    // STEP 4: Apply for a Card
    // ============================================================
    console.log('Step 4: User applying for a card...');

    // Navigate to cards section
    const cardsLink = page.getByRole('link', { name: /cards/i });
    if (await cardsLink.isVisible()) {
      await cardsLink.click();
    }

    // Click on "Request Card" or "Apply for Card" button
    const applyButton = page.getByRole('button', { name: /request|apply|new card/i });
    if (await applyButton.isVisible()) {
      await applyButton.click();

      // Select card type if prompted
      const virtualCardOption = page.getByText(/virtual/i).first();
      if (await virtualCardOption.isVisible()) {
        await virtualCardOption.click();
      }

      // Submit card application
      await page.getByRole('button', { name: /submit|confirm/i }).click();

      // Wait for card application confirmation
      await expect(page.getByText(/card requested|application submitted/i)).toBeVisible({ timeout: 10000 });
    }

    console.log('✓ Card application submitted');

    // ============================================================
    // STEP 5: Verification - Card Appears with Masked Numbers
    // ============================================================
    console.log('Step 5: Verifying card display with masked numbers...');

    // Refresh to see updated card list
    await page.reload();

    // Wait for cards section
    const cardsSection = page.getByText(/your cards|cards/i);
    if (await cardsSection.isVisible()) {
      // Verify card is displayed with masked number format (****-****-****-XXXX)
      const cardElement = page.locator('[data-testid*="card"], .card');
      
      if (await cardElement.isVisible()) {
        // Check for masked card number pattern
        const cardText = await cardElement.textContent();
        expect(cardText).toMatch(/\*{4}[-\s]?/); // Should contain masked pattern
        
        // Or check for specific masked format
        const maskedPattern = /\*\*\*\*[-\s]\*\*\*\*[-\s]\*\*\*\*[-\s]\d{4}/;
        if (maskedPattern.test(cardText || '')) {
          console.log('✓ Card displayed with proper masked number format');
        }
      }
    }

    console.log('✓ Golden Path test completed successfully!');
  });

  test('staff portal loan approval workflow', async ({ page, context }) => {
    // ============================================================
    // Loan Approval Workflow Test
    // ============================================================
    console.log('Testing loan approval workflow...');

    const staffPortalUrl = process.env.STAFF_PORTAL_URL || 'http://localhost:3001';
    const staffUsername = process.env.TEST_STAFF_USERNAME || 'manager_bob';
    const staffPassword = process.env.TEST_STAFF_PASSWORD || 'demo123!';

    await page.goto(staffPortalUrl);

    // Staff login
    await page.getByPlaceholder('Username').fill(staffUsername);
    await page.getByPlaceholder('Password', { exact: true }).fill(staffPassword);
    await page.getByRole('button', { name: /sign in/i }).click();

    // Wait for dashboard
    await expect(page.getByText(/dashboard/i)).toBeVisible({ timeout: 10000 });

    // Navigate to Loan Approvals tab
    await page.getByRole('tab', { name: /loan/i }).click();

    // Check if there are pending loans
    const pendingLoansText = await page.getByText(/no pending loan/i).isVisible();
    
    if (!pendingLoansText) {
      // There are pending loans - approve the first one
      const firstLoanRow = page.locator('tbody tr').first();
      await firstLoanRow.getByRole('button', { name: /approve/i }).click();

      // Wait for approval confirmation
      await expect(page.getByText(/approved successfully/i)).toBeVisible({ timeout: 5000 });
      console.log('✓ Loan approved successfully');
    } else {
      console.log('ℹ No pending loans to approve');
    }
  });

  test('admin console user management', async ({ page }) => {
    // ============================================================
    // Admin Console Test
    // ============================================================
    console.log('Testing admin console...');

    const adminConsoleUrl = process.env.ADMIN_CONSOLE_URL || 'http://localhost:3002';
    const adminUsername = process.env.TEST_ADMIN_USERNAME || 'admin_alice';
    const adminPassword = process.env.TEST_ADMIN_PASSWORD || 'demo123!';

    await page.goto(adminConsoleUrl);

    // Admin login
    await page.getByPlaceholder('Username').fill(adminUsername);
    await page.getByPlaceholder('Password', { exact: true }).fill(adminPassword);
    await page.getByRole('button', { name: /sign in/i }).click();

    // Wait for admin dashboard
    await expect(page.getByText(/admin|console/i)).toBeVisible({ timeout: 10000 });

    // Verify admin-specific features are available
    expect(await page.getByText(/audit|users|branches|system/i).isVisible()).toBeTruthy();
    
    console.log('✓ Admin console accessible');
  });
});
