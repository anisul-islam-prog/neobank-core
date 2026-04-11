-- =============================================================================
-- NeoBank — Demo Seed Data
-- =============================================================================
-- Populates the database with 50+ customers, 200+ transactions,
-- 15 AI-approved loans, cards, and KYC records for presentations.
-- Loaded automatically when PostgreSQL initializes (demo profile).
--
-- Usage:
--   docker compose --profile demo up -d   # auto-loads this file
--   OR manually: psql -U postgres -d neobank -f seed-data.sql
-- =============================================================================

-- =============================================================================
-- 1. SCHEMA CREATION (idempotent)
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS schema_auth;
CREATE SCHEMA IF NOT EXISTS schema_onboarding;
CREATE SCHEMA IF NOT EXISTS schema_core;
CREATE SCHEMA IF NOT EXISTS schema_loans;
CREATE SCHEMA IF NOT EXISTS schema_cards;
CREATE SCHEMA IF NOT EXISTS schema_fraud;
CREATE SCHEMA IF NOT EXISTS schema_batch;
CREATE SCHEMA IF NOT EXISTS schema_analytics;

-- =============================================================================
-- 2. AUTH — Users, Roles, Branches (50+ customers + staff)
-- =============================================================================

-- Branches
INSERT INTO schema_auth.branch (id, name, code, address, created_at)
VALUES
  (gen_random_uuid(), 'Head Office', 'HQ', '123 Banking Street, New York, NY 10001', NOW()),
  (gen_random_uuid(), 'Downtown Branch', 'DT', '456 Main Ave, Brooklyn, NY 11201', NOW()),
  (gen_random_uuid(), 'Uptown Branch', 'UT', '789 Park Blvd, Manhattan, NY 10021', NOW()),
  (gen_random_uuid(), 'Midtown Branch', 'MT', '321 5th Avenue, Manhattan, NY 10016', NOW()),
  (gen_random_uuid(), 'Airport Branch', 'AP', 'JFK Terminal 4, Queens, NY 11430', NOW())
ON CONFLICT DO NOTHING;

-- Customers (50 retail customers with realistic names)
INSERT INTO schema_auth.user_entity (id, username, email, password, role, status, branch_id, credit_score,
  monthly_income, monthly_expenses, employment_years, must_change_password, created_at, updated_at)
SELECT
  gen_random_uuid(),
  'customer_' || i,
  'customer_' || i || '@neobank.demo',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- 'password123'
  'CUSTOMER_RETAIL',
  'ACTIVE',
  (SELECT id FROM schema_auth.branch ORDER BY RANDOM() LIMIT 1),
  (RANDOM() * 100)::int,
  (3000 + RANDOM() * 7000)::numeric(10,2),
  (1000 + RANDOM() * 4000)::numeric(10,2),
  (1 + RANDOM() * 20)::int,
  false,
  NOW() - (RANDOM() * 365 || ' days')::interval,
  NOW()
FROM generate_series(1, 50) AS i
ON CONFLICT DO NOTHING;

-- Staff users (5 tellers, 3 managers, 2 ROs, 1 admin, 1 auditor)
INSERT INTO schema_auth.user_entity (id, username, email, password, role, status, branch_id, credit_score,
  monthly_income, monthly_expenses, employment_years, must_change_password, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'teller_alice',    'teller_alice@neobank.demo',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TELLER',              'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'DT'), 0, 3500, 2000, 3, true,  NOW() - 180, NOW()),
  (gen_random_uuid(), 'teller_bob',      'teller_bob@neobank.demo',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TELLER',              'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'MT'), 0, 3500, 2000, 2, true,  NOW() - 150, NOW()),
  (gen_random_uuid(), 'teller_carol',    'teller_carol@neobank.demo',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TELLER',              'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'UT'), 0, 3500, 2000, 1, true,  NOW() - 120, NOW()),
  (gen_random_uuid(), 'teller_david',    'teller_david@neobank.demo',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TELLER',              'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'HQ'), 0, 3800, 2200, 4, true,  NOW() - 200, NOW()),
  (gen_random_uuid(), 'teller_eve',      'teller_eve@neobank.demo',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TELLER',              'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'AP'), 0, 3500, 2000, 1, true,  NOW() - 90,  NOW()),
  (gen_random_uuid(), 'manager_frank',   'manager_frank@neobank.demo',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',             'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'HQ'), 0, 6000, 3500, 8, true,  NOW() - 400, NOW()),
  (gen_random_uuid(), 'manager_grace',   'manager_grace@neobank.demo',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',             'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'DT'), 0, 6000, 3500, 6, true,  NOW() - 300, NOW()),
  (gen_random_uuid(), 'manager_henry',   'manager_henry@neobank.demo',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',             'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'MT'), 0, 6500, 3800, 10, true, NOW() - 500, NOW()),
  (gen_random_uuid(), 'ro_irene',        'ro_irene@neobank.demo',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'RELATIONSHIP_OFFICER', 'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'HQ'), 0, 5500, 3000, 5, true,  NOW() - 250, NOW()),
  (gen_random_uuid(), 'ro_jack',         'ro_jack@neobank.demo',         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'RELATIONSHIP_OFFICER', 'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'UT'), 0, 5500, 3000, 4, true,  NOW() - 200, NOW()),
  (gen_random_uuid(), 'admin_kate',      'admin_kate@neobank.demo',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SYSTEM_ADMIN',        'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'HQ'), 0, 8000, 4000, 12, true, NOW() - 700, NOW()),
  (gen_random_uuid(), 'auditor_leo',     'auditor_leo@neobank.demo',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AUDITOR',             'ACTIVE', (SELECT id FROM schema_auth.branch WHERE code = 'HQ'), 0, 7000, 3500, 7, true,  NOW() - 350, NOW())
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 3. CORE BANKING — Accounts (one per customer)
-- =============================================================================

INSERT INTO schema_core.account (id, user_id, account_type, balance, currency, status, created_at, updated_at)
SELECT
  gen_random_uuid(),
  u.id,
  'SAVINGS',
  (500 + RANDOM() * 50000)::numeric(15,2),
  'USD',
  'ACTIVE',
  u.created_at,
  NOW()
FROM schema_auth.user_entity u
WHERE u.role = 'CUSTOMER_RETAIL'
ON CONFLICT DO NOTHING;

-- Business accounts for managers
INSERT INTO schema_core.account (id, user_id, account_type, balance, currency, status, created_at, updated_at)
SELECT
  gen_random_uuid(),
  u.id,
  'BUSINESS',
  (10000 + RANDOM() * 100000)::numeric(15,2),
  'USD',
  'ACTIVE',
  u.created_at,
  NOW()
FROM schema_auth.user_entity u
WHERE u.role = 'MANAGER'
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 4. CORE BANKING — Transactions (200+ realistic transfers)
-- =============================================================================

INSERT INTO schema_core.transaction (id, from_account_id, to_account_id, amount, currency,
  description, transaction_type, status, created_at, updated_at)
SELECT
  gen_random_uuid(),
  (SELECT id FROM schema_core.account ORDER BY RANDOM() LIMIT 1),
  (SELECT id FROM schema_core.account ORDER BY RANDOM() LIMIT 1),
  (10 + RANDOM() * 5000)::numeric(12,2),
  'USD',
  (ARRAY['Salary payment', 'Online purchase', 'Rent payment', 'Grocery shopping',
         'Utility bill', 'Restaurant bill', 'Transfer to savings', 'Gift transfer',
         'Insurance premium', 'Subscription renewal', 'Medical expense',
         'Education fee', 'Travel booking', 'Freelance payment', 'Refund'])[floor(RANDOM() * 15 + 1)],
  'TRANSFER',
  'COMPLETED',
  NOW() - (RANDOM() * 180 || ' days')::interval,
  NOW()
FROM generate_series(1, 200)
ON CONFLICT DO NOTHING;

-- High-value transactions (for Maker-Checker demo)
INSERT INTO schema_core.transaction (id, from_account_id, to_account_id, amount, currency,
  description, transaction_type, status, created_at, updated_at)
SELECT
  gen_random_uuid(),
  (SELECT id FROM schema_core.account WHERE balance > 20000 ORDER BY RANDOM() LIMIT 1),
  (SELECT id FROM schema_core.account ORDER BY RANDOM() LIMIT 1),
  (6000 + RANDOM() * 44000)::numeric(12,2),
  'USD',
  (ARRAY['Property down payment', 'Business investment', 'Vehicle purchase',
         'Large equipment purchase', 'International wire'])[floor(RANDOM() * 5 + 1)],
  'TRANSFER',
  'PENDING_AUTH',
  NOW() - (RANDOM() * 30 || ' days')::interval,
  NOW()
FROM generate_series(1, 10)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 5. LENDING — Loan Applications (15 AI-approved loans)
-- =============================================================================

INSERT INTO schema_loans.loan_application (id, user_id, amount, term_months, purpose,
  annual_income, employment_years, monthly_debt, status, interest_rate,
  monthly_payment, risk_score, ai_recommendation, created_at, updated_at)
SELECT
  gen_random_uuid(),
  u.id,
  (5000 + RANDOM() * 95000)::numeric(12,2),
  (ARRAY[12, 24, 36, 48, 60])[floor(RANDOM() * 5 + 1)],
  (ARRAY['HOME_IMPROVEMENT', 'PERSONAL', 'EDUCATION', 'BUSINESS', 'MEDICAL',
         'DEBT_CONSOLIDATION', 'VEHICLE', 'WEDDING'])[floor(RANDOM() * 8 + 1)],
  (30000 + RANDOM() * 120000)::numeric(10,2),
  (1 + RANDOM() * 25)::int,
  (500 + RANDOM() * 5000)::numeric(10,2),
  (ARRAY['APPROVED', 'APPROVED', 'APPROVED', 'APPROVED', 'APPROVED',
         'APPROVED', 'APPROVED', 'PENDING', 'UNDER_REVIEW'])[floor(RANDOM() * 9 + 1)],
  (3.5 + RANDOM() * 8.5)::numeric(5,2),
  (100 + RANDOM() * 2000)::numeric(10,2),
  (RANDOM() * 100)::int,
  (ARRAY['APPROVE', 'APPROVE', 'APPROVE', 'REVIEW', 'APPROVE'])[floor(RANDOM() * 5 + 1)],
  NOW() - (RANDOM() * 90 || ' days')::interval,
  NOW()
FROM schema_auth.user_entity u
WHERE u.role = 'CUSTOMER_RETAIL'
ORDER BY RANDOM()
LIMIT 15
ON CONFLICT DO NOTHING;

-- Disburse some approved loans
INSERT INTO schema_loans.loan (id, application_id, user_id, account_id, principal_amount,
  interest_rate, term_months, monthly_payment, remaining_balance, status,
  next_due_date, disbursed_at, created_at)
SELECT
  gen_random_uuid(),
  la.id,
  la.user_id,
  a.id,
  la.amount,
  la.interest_rate,
  la.term_months,
  la.monthly_payment,
  la.amount * (1 + la.interest_rate / 100),
  'ACTIVE',
  NOW() + (30 || ' days')::interval,
  NOW() - (RANDOM() * 60 || ' days')::interval,
  NOW()
FROM schema_loans.loan_application la
JOIN schema_core.account a ON a.user_id = la.user_id
WHERE la.status = 'APPROVED'
LIMIT 8
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 6. CARDS — Virtual & Physical Cards (one per active customer)
-- =============================================================================

INSERT INTO schema_cards.card (id, user_id, account_id, card_number, card_type,
  status, cardholder_name, expiry_month, expiry_year, cvv_hash,
  daily_limit, monthly_limit, created_at, updated_at)
SELECT
  gen_random_uuid(),
  u.id,
  a.id,
  '4' || lpad((RANDOM() * 10000000000000000)::bigint::text, 15, '0'),
  (ARRAY['VIRTUAL', 'PHYSICAL'])[floor(RANDOM() * 2 + 1)],
  (ARRAY['ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'FROZEN'])[floor(RANDOM() * 5 + 1)],
  upper(replace(u.username, '_', ' ')),
  (floor(RANDOM() * 12) + 1),
  2028 + (RANDOM() * 3)::int,
  sha256((floor(RANDOM() * 1000))::text::bytea),
  5000,
  50000,
  u.created_at,
  NOW()
FROM schema_auth.user_entity u
JOIN schema_core.account a ON a.user_id = u.id
WHERE u.role = 'CUSTOMER_RETAIL'
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 7. ONBOARDING — KYC Records for customers
-- =============================================================================

INSERT INTO schema_onboarding.kyc_record (id, user_id, document_type, document_number,
  status, submitted_at, reviewed_at, reviewed_by, notes, created_at)
SELECT
  gen_random_uuid(),
  u.id,
  (ARRAY['PASSPORT', 'DRIVERS_LICENSE', 'NATIONAL_ID'])[floor(RANDOM() * 3 + 1)],
  lpad((RANDOM() * 10000000)::bigint::text, 8, '0'),
  'APPROVED',
  u.created_at,
  u.created_at + (RANDOM() * 3 || ' days')::interval,
  (SELECT id FROM schema_auth.user_entity WHERE role IN ('MANAGER', 'RELATIONSHIP_OFFICER') ORDER BY RANDOM() LIMIT 1),
  'Demo KYC — approved automatically',
  u.created_at
FROM schema_auth.user_entity u
WHERE u.role = 'CUSTOMER_RETAIL'
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 8. ANALYTICS — BI Read Model (denormalized transactions)
-- =============================================================================

INSERT INTO schema_analytics.bi_transaction_history (id, original_transaction_id, from_user_id, to_user_id,
  amount, currency, description, transaction_type, status, created_at)
SELECT
  gen_random_uuid(),
  t.id,
  af.user_id,
  at.user_id,
  t.amount,
  t.currency,
  t.description,
  t.transaction_type,
  t.status,
  t.created_at
FROM schema_core.transaction t
JOIN schema_core.account af ON af.id = t.from_account_id
JOIN schema_core.account at ON at.id = t.to_account_id
LIMIT 100
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 9. FRAUD — Fraud Analysis Records (sample)
-- =============================================================================

INSERT INTO schema_fraud.fraud_entity (id, transaction_id, risk_score, risk_level,
  status, analysis_provider, analysis_result, created_at, updated_at)
SELECT
  gen_random_uuid(),
  t.id,
  (RANDOM() * 100)::int,
  (ARRAY['LOW', 'LOW', 'LOW', 'MEDIUM', 'MEDIUM', 'HIGH'])[floor(RANDOM() * 6 + 1)],
  (ARRAY['CLEARED', 'CLEARED', 'CLEARED', 'UNDER_REVIEW', 'FLAGGED'])[floor(RANDOM() * 5 + 1)],
  (ARRAY['OLLAAMA_LLAMA3', 'OPENAI_GPT4O_MINI'])[floor(RANDOM() * 2 + 1)],
  jsonb_build_object(
    'anomaly_detected', RANDOM() > 0.7,
    'amount_deviation', ROUND((RANDOM() * 5)::numeric, 2),
    'location_risk', (ARRAY['LOW', 'MEDIUM', 'HIGH'])[floor(RANDOM() * 3 + 1)]
  ),
  t.created_at,
  NOW()
FROM schema_core.transaction t
ORDER BY RANDOM()
LIMIT 30
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 10. BATCH — Generate some batch job records
-- =============================================================================

INSERT INTO schema_batch.batch_job_instance (job_instance_id, version, job_name, job_key)
SELECT
  i,
  0,
  'dailyReconciliationJob',
  'date=' || to_string(to_date('2026-01-01', 'YYYY-MM-DD') + (i || ' days')::interval, 'YYYY-MM-DD')
FROM generate_series(1, 30) AS i
ON CONFLICT DO NOTHING;

-- =============================================================================
-- VERIFICATION SUMMARY
-- =============================================================================
DO $$
DECLARE
  v_users int;
  v_accounts int;
  v_transactions int;
  v_loans int;
  v_cards int;
  v_kyc int;
  v_fraud int;
BEGIN
  SELECT count(*) INTO v_users FROM schema_auth.user_entity;
  SELECT count(*) INTO v_accounts FROM schema_core.account;
  SELECT count(*) INTO v_transactions FROM schema_core.transaction;
  SELECT count(*) INTO v_loans FROM schema_loans.loan_application;
  SELECT count(*) INTO v_cards FROM schema_cards.card;
  SELECT count(*) INTO v_kyc FROM schema_onboarding.kyc_record;
  SELECT count(*) INTO v_fraud FROM schema_fraud.fraud_entity;

  RAISE NOTICE '============================================================';
  RAISE NOTICE '  NeoBank Demo Data — Seed Complete';
  RAISE NOTICE '============================================================';
  RAISE NOTICE '  Users:          %', v_users;
  RAISE NOTICE '  Accounts:       %', v_accounts;
  RAISE NOTICE '  Transactions:   %', v_transactions;
  RAISE NOTICE '  Loan Apps:      %', v_loans;
  RAISE NOTICE '  Cards:          %', v_cards;
  RAISE NOTICE '  KYC Records:    %', v_kyc;
  RAISE NOTICE '  Fraud Analyses: %', v_fraud;
  RAISE NOTICE '============================================================';
END $$;
