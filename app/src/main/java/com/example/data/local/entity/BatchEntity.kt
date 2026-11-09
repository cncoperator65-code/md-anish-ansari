package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BatchRecord
import com.example.data.model.BatchStatus

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey
    val batchNumber: String,
    val batchId: String,
    val medicineId: String,
    val medicineName: String,
    val gtin: String,
    val manufacturer: String,
    val mfgDate: String,
    val expiryDate: String,
    val quantity: Int,
    val reservedQuantity: Int,
    val minThreshold: Int,
    val reorderThreshold: Int,
    val storageTempRange: String,
    val currentTempCelsius: Float,
    val warehouseLocation: String,
    val status: String,
    val isQuarantined: Boolean,
    val quarantineReason: String?,
    val isArchived: Boolean,
    val lastAuditedBy: String
) {
    fun toDomain(): BatchRecord {
        val parsedStatus = try {
            BatchStatus.valueOf(status)
        } catch (e: Exception) {
            BatchStatus.OPTIMAL
        }

        return BatchRecord(
            batchId = batchId,
            medicineId = medicineId,
            medicineName = medicineName,
            batchNumber = batchNumber,
            gtin = gtin,
            manufacturer = manufacturer,
            mfgDate = mfgDate,
            expiryDate = expiryDate,
            quantity = quantity,
            reservedQuantity = reservedQuantity,
            minThreshold = minThreshold,
            reorderThreshold = reorderThreshold,
            storageTempRange = storageTempRange,
            currentTempCelsius = currentTempCelsius,
            warehouseLocation = warehouseLocation,
            status = parsedStatus,
            isQuarantined = isQuarantined,
            quarantineReason = quarantineReason,
            isArchived = isArchived,
            lastAuditedBy = lastAuditedBy
        )
    }

    companion object {
        fun fromDomain(record: BatchRecord): BatchEntity {
            return BatchEntity(
                batchNumber = record.batchNumber,
                batchId = record.batchId,
                medicineId = record.medicineId,
                medicineName = record.medicineName,
                gtin = record.gtin,
                manufacturer = record.manufacturer,
                mfgDate = record.mfgDate,
                expiryDate = record.expiryDate,
                quantity = record.quantity,
                reservedQuantity = record.reservedQuantity,
                minThreshold = record.minThreshold,
                reorderThreshold = record.reorderThreshold,
                storageTempRange = record.storageTempRange,
                currentTempCelsius = record.currentTempCelsius,
                warehouseLocation = record.warehouseLocation,
                status = record.status.name,
                isQuarantined = record.isQuarantined,
                quarantineReason = record.quarantineReason,
                isArchived = record.isArchived,
                lastAuditedBy = record.lastAuditedBy
            )
        }
    }
}
