package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    suspend fun getAllUsers(): List<UserAccount>

    @Query("SELECT * FROM user_accounts WHERE isMainUser = 1 LIMIT 1")
    fun getMainUserFlow(): Flow<UserAccount?>

    @Query("SELECT * FROM user_accounts WHERE isMainUser = 1 LIMIT 1")
    suspend fun getMainUser(): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE user_accounts SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Int, status: String)

    @Query("UPDATE user_accounts SET balanceYer = :balanceYer, balanceUsd = :balanceUsd WHERE id = :userId")
    suspend fun updateUserBalance(userId: Int, balanceYer: Double, balanceUsd: Double)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface SavingPotDao {
    @Query("SELECT * FROM saving_pots ORDER BY id ASC")
    fun getAllSavingPotsFlow(): Flow<List<SavingPot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingPot(savingPot: SavingPot)

    @Update
    suspend fun updateSavingPot(savingPot: SavingPot)

    @Query("DELETE FROM saving_pots WHERE id = :id")
    suspend fun deleteSavingPot(id: Int)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AuditLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}

@Dao
interface AdminConfigDao {
    @Query("SELECT * FROM admin_configs WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AdminConfig?>

    @Query("SELECT * FROM admin_configs WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AdminConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AdminConfig)

    @Update
    suspend fun updateConfig(config: AdminConfig)
}
