package com.example.data.model

enum class RegulatoryJurisdiction(val countryCode: String, val authorityName: String) {
    UAE_MOHAP("UAE", "Ministry of Health & Prevention (MOHAP / DHA)"),
    US_FDA("USA", "Food and Drug Administration (FDA / DSCSA)"),
    EU_EMA("EU", "European Medicines Agency (EMA / FMD)"),
    INDIA_CDSCO("IND", "Central Drugs Standard Control Org (CDSCO)")
}

data class CountryRegulatoryProfile(
    val jurisdiction: RegulatoryJurisdiction,
    val trackAndTraceSystem: String, // e.g. "UAE Tatmeen", "US DSCSA GS1 DataMatrix"
    val pharmacistVerificationMandatory: Boolean = true,
    val coldChainLogisticsAuditRequired: Boolean = true,
    val maxControlledDaysSupply: Int = 30,
    val requiresDigitalLicenseSignature: Boolean = true,
    val activeFormularyVersion: String = "2026.11-v4"
)

data class PharmacistCredential(
    val licenseNumber: String,
    val pharmacistName: String,
    val jurisdiction: RegulatoryJurisdiction,
    val issuingBody: String,
    val expiryDate: String,
    val isVerifiedActive: Boolean = true,
    val digitalSignatureKey: String = "SHA256:8891-AA-DHA-PHARMACIST-SIG"
)
