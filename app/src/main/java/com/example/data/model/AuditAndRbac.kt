package com.example.data.model

enum class UserAccessRole(val displayName: String, val clearanceLevel: Int) {
    CUSTOMER("B2C Patient", 1),
    COURIER_AGENT("Cold-Chain Courier", 2),
    INVENTORY_MANAGER("Warehouse & Inventory Manager", 3),
    JV_PARTNER_ADMIN("JV Partner Administrator", 3),
    LICENSED_PHARMACIST("Licensed Clinical Pharmacist", 4),
    COMPLIANCE_OFFICER("Regulatory Compliance Officer", 4),
    SUPER_ADMIN("Global Super Administrator", 5)
}

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val actor: String,
    val role: UserAccessRole,
    val action: String, // e.g. "QUARANTINE_BATCH", "APPROVE_RX", "RELEASE_ESCROW", "PO_ISSUED"
    val resource: String,
    val details: String,
    val ipAddress: String = "192.168.1.42",
    val hashSignature: String
)
