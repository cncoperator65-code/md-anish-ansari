package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StockMovementRecord
import com.example.data.model.StockMovementType

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey
    val id: String,
    val batchNumber: String,
    val medicineName: String,
    val movementType: String,
    val quantityDelta: Int,
    val balanceAfter: Int,
    val actorName: String,
    val actorRole: String,
    val reason: String,
    val referenceId: String,
    val timestampFormatted: String
) {
    fun toDomain(): StockMovementRecord {
        val parsedType = try {
            StockMovementType.valueOf(movementType)
        } catch (e: Exception) {
            StockMovementType.CYCLE_COUNT_ADJUSTMENT
        }

        return StockMovementRecord(
            id = id,
            batchNumber = batchNumber,
            medicineName = medicineName,
            movementType = parsedType,
            quantityDelta = quantityDelta,
            balanceAfter = balanceAfter,
            actorName = actorName,
            actorRole = actorRole,
            reason = reason,
            referenceId = referenceId,
            timestampFormatted = timestampFormatted
        )
    }

    companion object {
        fun fromDomain(record: StockMovementRecord): StockMovementEntity {
            return StockMovementEntity(
                id = record.id,
                batchNumber = record.batchNumber,
                medicineName = record.medicineName,
                movementType = record.movementType.name,
                quantityDelta = record.quantityDelta,
                balanceAfter = record.balanceAfter,
                actorName = record.actorName,
                actorRole = record.actorRole,
                reason = record.reason,
                referenceId = record.referenceId,
                timestampFormatted = record.timestampFormatted
            )
        }
    }
}
