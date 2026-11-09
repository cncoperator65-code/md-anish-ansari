package com.example.data.model

import java.text.SimpleDateFormat
import java.util.*

enum class BatchStatus(val label: String) {
    OPTIMAL("Normal Stock"),
    LOW_STOCK("Low Stock Alert"),
    EXPIRING_SOON("Expiring (< 60d)"),
    CRITICAL_TEMP("Cold-Chain Warning"),
    RECALLED_QUARANTINED("QUARANTINED / RECALLED (BLOCKED)")
}

data class BatchRecord(
    val batchId: String,
    val medicineId: String,
    val medicineName: String,
    val batchNumber: String,
    val gtin: String = "06291002019231",
    val manufacturer: String = "PharmaGlobal Certified",
    val mfgDate: String,
    val expiryDate: String,
    val quantity: Int,
    val reservedQuantity: Int = 0,
    val minThreshold: Int = 20,
    val reorderThreshold: Int = 25,
    val storageTempRange: String = "2°C - 8°C",
    val currentTempCelsius: Float = 4.5f,
    val warehouseLocation: String = "Bay A4 - Cold Unit 2",
    val status: BatchStatus = BatchStatus.OPTIMAL,
    val isQuarantined: Boolean = false,
    val quarantineReason: String? = null,
    val isArchived: Boolean = false,
    val lastAuditedBy: String = "Pharmacist Dr. Salma K."
) {
    val availableQuantity: Int
        get() = maxOf(0, quantity - reservedQuantity)

    // Calculate approximate days until expiration from expiry string (e.g. "28-Feb-2027")
    fun daysUntilExpiry(): Long {
        return try {
            val format = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
            val expiry = format.parse(expiryDate) ?: return 365L
            val now = Date()
            val diffMs = expiry.time - now.time
            diffMs / (1000L * 60 * 60 * 24)
        } catch (e: Exception) {
            180L
        }
    }

    val isFefoEligible: Boolean
        get() = !isQuarantined && !isArchived && daysUntilExpiry() > 0 && availableQuantity > 0
}

data class StockAlert(
    val id: String,
    val title: String,
    val message: String,
    val medicineName: String,
    val severity: String, // "CRITICAL", "WARNING", "INFO"
    val timestamp: String
)

data class DemandForecast(
    val medicineName: String,
    val currentStock: Int,
    val projectedDemand30d: Int,
    val suggestedReorderQty: Int,
    val stockOutRiskScore: Int, // 0-100%
    val seasonalFactor: String
)
