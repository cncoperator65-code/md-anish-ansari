package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BatchDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.StockMovementEntity

@Database(
    entities = [
        BatchEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PharmaDatabase : RoomDatabase() {

    abstract fun batchDao(): BatchDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaDatabase? = null

        fun getInstance(context: Context): PharmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaDatabase::class.java,
                    "pharmax_pharmacy.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInMemoryDatabase(context: Context): PharmaDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                PharmaDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
