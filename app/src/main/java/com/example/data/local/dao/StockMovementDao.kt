package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Query("SELECT * FROM stock_movements ORDER BY rowid DESC")
    fun getAllMovementsFlow(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE batchNumber = :batchNumber ORDER BY rowid DESC")
    fun getMovementsForBatchFlow(batchNumber: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY rowid DESC")
    suspend fun getAllMovements(): List<StockMovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)

    @Query("SELECT COUNT(*) FROM stock_movements")
    suspend fun getCount(): Int

    @Query("DELETE FROM stock_movements")
    suspend fun clearAll()
}
