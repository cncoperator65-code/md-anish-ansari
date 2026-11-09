package com.example.data.model

enum class TamperSealStatus(val label: String) {
    SEAL_LOCKED_INTACT("Digital Seal Intact (Active)"),
    TAMPER_ALERT_BREACHED("Tamper Detected / Seal Broken"),
    UNSEALED_RECEIVING("Unsealed for Pharmacist Verification")
}

data class ExcursionEvent(
    val id: String,
    val timestamp: String,
    val peakTempC: Float,
    val durationMinutes: Int,
    val meanKineticTempC: Float,
    val severity: String, // "MINOR", "CRITICAL_BATCH_INVALIDATION"
    val actionTaken: String
)

data class ColdChainTelemetryData(
    val sensorId: String,
    val containerId: String,
    val liveTempC: Float,
    val ambientTempC: Float,
    val targetMinC: Float = 2.0f,
    val targetMaxC: Float = 8.0f,
    val humidityRh: Int = 45,
    val meanKineticTempC: Float = 4.3f,
    val tamperSealStatus: TamperSealStatus = TamperSealStatus.SEAL_LOCKED_INTACT,
    val batteryPercent: Int = 92,
    val excursions: List<ExcursionEvent> = emptyList(),
    val isComplianceValid: Boolean = true
)
