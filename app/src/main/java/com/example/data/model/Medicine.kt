package com.example.data.model

enum class MedicineCategory(val label: String) {
    ALL("All Meds"),
    PRESCRIPTION("Rx Required"),
    OVER_THE_COUNTER("OTC"),
    CHRONIC_CARE("Chronic Care"),
    COLD_CHAIN("Cold-Chain (2-8°C)"),
    EMERGENCY("Priority Emergency"),
    RECALLED("Quarantined / Recalled")
}

data class MedicineItem(
    val id: String,
    val name: String,
    val genericName: String,
    val brand: String,
    val strength: String,
    val category: MedicineCategory,
    val price: Double,
    val requiresPrescription: Boolean,
    val description: String,
    val stockTotal: Int,
    val isColdChainRequired: Boolean = false,
    val recommendedTemp: String = "15°C - 25°C",
    val dosageInstructions: String = "Take with water as directed",
    val precautions: String = "Keep away from children. Consult physician.",
    val manufacturer: String = "PharmaGlobal Labs",
    val isRecalledOrQuarantined: Boolean = false,
    val quarantineNotice: String? = null,
    val barcodeGtin: String = "629100201923"
)
