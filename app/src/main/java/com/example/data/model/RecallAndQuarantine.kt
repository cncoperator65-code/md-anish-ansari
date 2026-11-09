package com.example.data.model

enum class QuarantineReason(val label: String) {
    CONTAMINATION_SUSPECT("Quality / Contamination Suspect"),
    TEMPERATURE_EXCURSION("Cold-Chain Temperature Excursion (>8°C)"),
    PACKAGING_DEFECT("Tamper Seal or Packaging Compromised"),
    REGULATORY_RECALL("Health Authority Mandatory Recall (MOHAP/FDA)"),
    EXPIRED_STOCK("Passed Expiration Date")
}

enum class QuarantineStatus(val label: String) {
    ACTIVE_NORMAL("Normal / Cleared"),
    QUARANTINED("Quarantined (Blocked)"),
    RECALLED("Recalled by Manufacturer"),
    DESTROYED("Destroyed / Hazardous Disposal")
}

data class BatchQuarantineRecord(
    val id: String,
    val batchNumber: String,
    val medicineId: String,
    val medicineName: String,
    val manufacturer: String,
    val reason: QuarantineReason,
    val status: QuarantineStatus,
    val quarantinedBy: String,
    val authorityNoticeId: String,
    val quarantinedAt: Long = System.currentTimeMillis(),
    val affectedUnits: Int,
    val patientAlertsDispatched: Int = 0,
    val resolutionNotes: String = "Dispensing and order routing strictly blocked."
)
