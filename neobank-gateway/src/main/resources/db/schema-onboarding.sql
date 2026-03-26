-- Schema for onboarding module
-- Stores user profiles, KYC data, and approval workflow
CREATE SCHEMA IF NOT EXISTS schema_onboarding;

-- User profiles table (business side, separate from auth credentials)
CREATE TABLE IF NOT EXISTS schema_onboarding.user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,  -- Reference to schema_auth.users (no FK for schema isolation)
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    kyc_document_url VARCHAR(500),
    kyc_verified_at TIMESTAMP,
    approved_by UUID,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    metadata JSONB
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON schema_onboarding.user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON schema_onboarding.user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_status ON schema_onboarding.user_profiles(status);

-- Comments
COMMENT ON TABLE schema_onboarding.user_profiles IS 'User onboarding profiles - business side (KYC, status, approval)';
COMMENT ON COLUMN schema_onboarding.user_profiles.user_id IS 'Reference to auth module user (schema_auth.users.id)';
COMMENT ON COLUMN schema_onboarding.user_profiles.status IS 'PENDING, ACTIVE, or SUSPENDED';
