package com.example.data.model

data class JVPartner(
    val id: String,
    val name: String,
    val licenseNo: String,
    val branchCity: String,
    val distanceKm: Double,
    val sharedStockSkus: Int,
    val reliabilityRating: Float,
    val commissionSharePct: Double = 5.0, // referral fee %
    val isOnline: Boolean = true
)

data class JVSyncResult(
    val requestedMedicine: String,
    val requestingPharmacy: String,
    val fulfillingPartner: JVPartner,
    val stockAvailable: Int,
    val batchNumber: String,
    val estimatedTransferMins: Int,
    val partnerPrice: Double,
    val partnerSplitAmount: Double,
    val platformFeeAmount: Double,
    val isEscrowReserved: Boolean = true
)

data class JVPartnerTransfer(
    val transferId: String,
    val medicineName: String,
    val quantity: Int,
    val sourceStore: String,
    val destinationStore: String,
    val status: String, // "DISPATCHED", "IN_TRANSIT", "COMPLETED"
    val timestamp: String,
    val partnerEarnings: Double
)
