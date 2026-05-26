package com.example.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [UserAccount::class, Transaction::class, SavingPot::class, AuditLog::class, AdminConfig::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserAccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingPotDao(): SavingPotDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun adminConfigDao(): AdminConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wam_wallet_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
