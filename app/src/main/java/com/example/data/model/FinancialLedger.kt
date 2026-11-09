package com.example.data.model

enum class TransactionType(val label: String) {
    CUSTOMER_ESCROW_DEPOSIT("Customer Escrow Deposit"),
    PARTNER_SHARE_PAYOUT("Fulfilling Partner 85% Payout"),
    JV_REFERRAL_COMMISSION("JV Referral 5% Commission"),
    COLD_LOGISTICS_FEE("Cold-Chain Logistics 7% Settlement"),
    PLATFORM_ESCROW_FEE("PharmaX AI 3% Platform Fee"),
    BATCH_DEFECT_REFUND("Batch Defect / Full Refund")
}

data class LedgerTransaction(
    val id: String,
    val orderId: String,
    val timestamp: String,
    val type: TransactionType,
    val sourceAccount: String,
    val destinationAccount: String,
    val grossAmount: Double,
    val netAmount: Double,
    val escrowLocked: Boolean,
    val status: String = "SETTLED"
)

data class PartnerSettlementSummary(
    val partnerId: String,
    val partnerName: String,
    val totalVolumeGross: Double,
    val pendingEscrowLocked: Double,
    val totalSettledPayouts: Double,
    val lastPayoutDate: String,
    val payoutMethod: String = "Stripe Connect Direct Payout"
)
