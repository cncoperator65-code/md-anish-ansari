package com.example.data.model

enum class StockMovementType(val label: String, val isDeduction: Boolean) {
    GOODS_RECEIPT("Goods Receipt / Inward", false),
    DISPENSE_SALE("Dispensed Order Fulfillment", true),
    FEFO_RESERVATION("FEFO Order Reservation", true),
    CYCLE_COUNT_ADJUSTMENT("Inventory Audit / Adjustment", false),
    WAREHOUSE_TRANSFER("Bay / Warehouse Relocation", false),
    QUARANTINE_LOCK("Health Authority Quarantine", true),
    BATCH_ARCHIVED("Archived / Closed", false)
}

data class StockMovementRecord(
    val id: String,
    val batchNumber: String,
    val medicineName: String,
    val movementType: StockMovementType,
    val quantityDelta: Int, // e.g. +50 or -10
    val balanceAfter: Int,
    val actorName: String,
    val actorRole: String,
    val reason: String,
    val referenceId: String,
    val timestampFormatted: String
)

data class FefoAllocationItem(
    val batchNumber: String,
    val medicineName: String,
    val expiryDate: String,
    val daysUntilExpiry: Long,
    val availableQty: Int,
    val allocatedQty: Int,
    val pickingPriority: Int, // 1 is highest priority (earliest valid expiry)
    val isEligible: Boolean, // false if expired or under quarantine
    val storageCondition: String,
    val warehouseLocation: String
)

enum class ExpiryFilterWindow(val label: String, val maxDays: Long) {
    ALL("All Batches", Long.MAX_VALUE),
    EXPIRING_30D("Critical (< 30 Days)", 30L),
    EXPIRING_60D("Warning (< 60 Days)", 60L),
    EXPIRING_90D("Upcoming (< 90 Days)", 90L)
}

enum class InventorySortOrder(val label: String) {
    FEFO_EXPIRY_ASC("Earliest Expiry (FEFO First)"),
    STOCK_QUANTITY_DESC("Highest Stock Units"),
    STOCK_QUANTITY_ASC("Lowest Stock Units"),
    MEDICINE_NAME_ASC("Medicine Name (A-Z)"),
    BATCH_NUMBER_ASC("Batch Number")
}
