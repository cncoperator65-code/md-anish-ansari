package com.example.data.model

enum class SubscriptionTier(val title: String) {
    FREE_TIER("Basic Customer"),
    B2C_PREMIUM("Premium Customer"),
    B2B_BASE("Pharmacy Base"),
    B2B_PREMIUM_JV("Pharmacy Enterprise JV")
}

data class SubscriptionPlan(
    val id: String,
    val tier: SubscriptionTier,
    val name: String,
    val priceMonthly: String,
    val priceAnnual: String,
    val audienceBadge: String, // "B2C Customers" or "Pharmacies / JV Partners"
    val features: List<String>,
    val isPopular: Boolean = false,
    val colorHex: Long = 0xFF00897B
) {
    fun getLocalizedPrice(country: CountryConfig, isAnnual: Boolean): String {
        return when (tier) {
            SubscriptionTier.FREE_TIER -> "FREE"
            SubscriptionTier.B2C_PREMIUM -> {
                val base = if (isAnnual) 119.0 else 12.99
                val suffix = if (isAnnual) " / yr" else " / mo"
                val converted = country.convertFromUsd(base)
                "${country.formatCurrency(converted.convertedAmount)}$suffix"
            }
            SubscriptionTier.B2B_BASE -> {
                val base = if (isAnnual) 1490.0 else 149.0
                val suffix = if (isAnnual) " / yr" else " / mo"
                val converted = country.convertFromUsd(base)
                "${country.formatCurrency(converted.convertedAmount)}$suffix"
            }
            SubscriptionTier.B2B_PREMIUM_JV -> {
                val base = if (isAnnual) 3990.0 else 399.0
                val suffix = if (isAnnual) " / yr" else " / mo"
                val converted = country.convertFromUsd(base)
                "${country.formatCurrency(converted.convertedAmount)}$suffix"
            }
        }
    }
}

data class PillReminder(
    val id: String,
    val medicineName: String,
    val dosage: String,
    val timeLabel: String,
    val instruction: String,
    val isTakenToday: Boolean = false,
    val isAiScheduled: Boolean = true
)
