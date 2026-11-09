package com.example.data.model

data class ExtractedDrug(
    val drugName: String,
    val matchedMedicineId: String?,
    val strength: String,
    val dosage: String,
    val frequency: String,
    val durationDays: Int,
    val confidenceScore: Float, // e.g. 0.94f
    val isAvailableInStock: Boolean = true
)

enum class VerificationState(val label: String) {
    PENDING_OCR("Processing OCR"),
    AI_EXTRACTED("AI Extracted"),
    PHARMACIST_APPROVED("Pharmacist Verified"),
    REQUIRES_CLARIFICATION("Needs Clarification")
}

data class PrescriptionScan(
    val id: String,
    val patientName: String,
    val doctorName: String,
    val doctorLicense: String,
    val clinicHospital: String,
    val issuedDate: String,
    val rawOcrText: String,
    val extractedDrugs: List<ExtractedDrug>,
    val safetyInteractions: List<String>,
    val verificationState: VerificationState = VerificationState.AI_EXTRACTED,
    val pharmacistNote: String = "AI detected high confidence. Verified against national pharma formulary.",
    val scannedAt: Long = System.currentTimeMillis()
)
