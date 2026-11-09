package com.example.data.model

data class CartItem(
    val medicine: MedicineItem,
    val quantity: Int
)

data class EscrowSplit(
    val totalAmount: Double,
    val pharmacyShare: Double,      // ~85%
    val jvReferralShare: Double,    // ~5%
    val coldChainLogistics: Double, // ~7%
    val platformEscrowFee: Double,  // ~3%
    val escrowStatus: String = "ESCROW_LOCKED_PENDING_DELIVERY"
)

enum class PaymentMethod(val title: String, val subtitle: String) {
    CARD_STRIPE("Stripe Connect", "Instant split payout to partner accounts"),
    ESCROW_PAY("PharmaX Escrow Hold", "Released only on Cold-Chain OTP confirmation"),
    APPLE_GOOGLE_PAY("Google Pay", "Fast 1-touch authenticated checkout"),
    JV_CREDIT_LINE("B2B JV Credit Line", "30-Day automated wholesale settlement")
}

enum class DeliveryStage(val title: String, val subtitle: String) {
    CONFIRMED("Prescription & Order Verified", "Pharmacist approved and escrow authorized"),
    PACKAGED_COLD("Cold-Chain Vault Packaged", "Temperature sensor active at 4.2°C"),
    OUT_FOR_DELIVERY("In Transit (Smart Van)", "GPS Courier En-Route"),
    DELIVERED("Delivered & Escrow Released", "Signature and digital receipt recorded")
}

data class Order(
    val id: String,
    val customerName: String,
    val deliveryAddress: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val isJointVentureRoute: Boolean,
    val jvPartnerName: String?,
    val escrowSplit: EscrowSplit,
    val paymentMethod: PaymentMethod,
    val deliveryStage: DeliveryStage,
    val driverName: String = "Capt. Omar Al-Mansoor",
    val driverPhone: String = "+971 50 123 4567",
    val liveTempCelsius: Float = 4.2f,
    val targetTempRange: String = "2°C - 8°C",
    val etaMinutes: Int = 18,
    val createdAtFormatted: String
)
