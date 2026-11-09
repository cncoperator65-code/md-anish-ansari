package com.example.data.model

enum class BarcodeVerificationResult(val label: String) {
    GENUINE_VERIFIED("GS1 Verified • Genuine"),
    QUARANTINED_BLOCKED("Quarantined Batch • Prohibited"),
    COUNTERFEIT_SUSPECT("Checksum Mismatch • Counterfeit Alert"),
    EXPIRED_BARCODE("Expired Medication • Sale Blocked"),
    UNREGISTERED("Not Found in National Formulary")
}

data class ScannedBarcodeData(
    val gtin: String,
    val serialNumber: String,
    val batchNumber: String,
    val expiryDate: String,
    val rawPayload: String,
    val verificationResult: BarcodeVerificationResult,
    val medicineName: String,
    val trackAndTraceId: String, // e.g. UAE Tatmeen / US DSCSA ID
    val manufacturer: String,
    val scannedAt: Long = System.currentTimeMillis()
)
