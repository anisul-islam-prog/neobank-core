package com.neobank.auth;

/**
 * User role enumeration for role-based access control (RBAC).
 * 
 * Roles are organized in a hierarchy for authorization purposes:
 * 
 * Customer Roles:
 * - CUSTOMER_RETAIL: Individual banking customers
 * - CUSTOMER_BUSINESS: Business/corporate banking customers
 * 
 * Operational Roles:
 * - TELLER: Front-line banking staff (cash handling, basic transactions)
 * - RELATIONSHIP_OFFICER: Customer relationship management
 * - MANAGER: Branch/department managers with approval authority
 * 
 * Control Roles:
 * - AUDITOR: Compliance and audit access
 * - SYSTEM_ADMIN: Full system administration
 */
public enum UserRole {
    /**
     * Individual retail banking customer.
     * Access: Personal accounts, transfers, loans, cards.
     */
    CUSTOMER_RETAIL,

    /**
     * Business/corporate banking customer.
     * Access: Business accounts, commercial loans, multi-user management.
     */
    CUSTOMER_BUSINESS,

    /**
     * Front-line banking staff.
     * Access: Customer account lookup, cash transactions, basic operations.
     */
    TELLER,

    /**
     * Customer relationship manager.
     * Access: Customer portfolio, loan recommendations, account modifications.
     */
    RELATIONSHIP_OFFICER,

    /**
     * Branch or department manager.
     * Access: Loan approvals, staff oversight, exception handling.
     */
    MANAGER,

    /**
     * Internal auditor or compliance officer.
     * Access: Audit logs, transaction history, compliance reports.
     */
    AUDITOR,

    /**
     * System administrator with full access.
     * Access: All system functions, user management, configuration.
     */
    SYSTEM_ADMIN
}
