package com.example.data.model

data class PharmaSupplier(
    val id: String,
    val name: String,
    val licenseNumber: String, // e.g. MOHAP-SUP-88219
    val country: String,
    val rating: Float,
    val contactEmail: String,
    val leadTimeDays: Int,
    val isGmpCertified: Boolean = true
)

enum class PurchaseOrderStatus(val label: String) {
    DRAFT("Draft PO"),
    SUBMITTED("Submitted to Supplier"),
    PHARMACIST_APPROVED("Pharmacist Signed & Approved"),
    IN_TRANSIT("Dispatched via Cold Logistics"),
    RECEIVED_AND_INSPECTED("Received & QA Inspected"),
    REJECTED_QUARANTINED("Rejected (Cold/Seal Defect)")
}

data class PurchaseOrderItem(
    val medicineId: String,
    val medicineName: String,
    val quantity: Int,
    val unitCost: Double,
    val expectedTempRange: String = "15°C - 25°C"
)

data class PurchaseOrder(
    val id: String,
    val poNumber: String,
    val supplierId: String,
    val supplierName: String,
    val items: List<PurchaseOrderItem>,
    val totalCost: Double,
    val orderDate: String,
    val expectedDeliveryDate: String,
    val status: PurchaseOrderStatus,
    val approvedByPharmacist: String? = null,
    val temperatureAtArrivalC: Float? = null,
    val qaInspectionNotes: String = "Awaiting warehouse reception."
)
