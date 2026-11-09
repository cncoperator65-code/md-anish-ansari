package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    @Query("SELECT * FROM batches ORDER BY expiryDate ASC")
    fun getAllBatchesFlow(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE isArchived = 0 ORDER BY expiryDate ASC")
    fun getActiveBatchesFlow(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE batchNumber = :batchNumber LIMIT 1")
    suspend fun getBatchByNumber(batchNumber: String): BatchEntity?

    @Query("SELECT * FROM batches WHERE medicineId = :medicineId AND isArchived = 0")
    suspend fun getBatchesForMedicine(medicineId: String): List<BatchEntity>

    @Query("SELECT * FROM batches")
    suspend fun getAllBatches(): List<BatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BatchEntity>)

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("UPDATE batches SET quantity = :newQuantity, status = :newStatus WHERE batchNumber = :batchNumber")
    suspend fun updateQuantity(batchNumber: String, newQuantity: Int, newStatus: String)

    @Query("UPDATE batches SET reservedQuantity = :reservedQty WHERE batchNumber = :batchNumber")
    suspend fun updateReservedQuantity(batchNumber: String, reservedQty: Int)

    @Query("UPDATE batches SET warehouseLocation = :newLocation WHERE batchNumber = :batchNumber")
    suspend fun updateLocation(batchNumber: String, newLocation: String)

    @Query("UPDATE batches SET isArchived = 1 WHERE batchNumber = :batchNumber")
    suspend fun archiveBatch(batchNumber: String)

    @Query("UPDATE batches SET isQuarantined = 1, status = 'RECALLED_QUARANTINED', quarantineReason = :reason WHERE batchNumber = :batchNumber")
    suspend fun quarantineBatch(batchNumber: String, reason: String)

    @Query("SELECT COUNT(*) FROM batches")
    suspend fun getCount(): Int

    @Query("DELETE FROM batches WHERE batchNumber = :batchNumber")
    suspend fun deleteBatch(batchNumber: String)

    @Query("DELETE FROM batches")
    suspend fun clearAll()
}
