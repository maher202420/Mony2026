package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserAccount?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccount?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getMainUser(): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}

@Dao
interface AdminConfigDao {
    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AdminConfig?>

    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AdminConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AdminConfig)

    @Update
    suspend fun updateConfig(config: AdminConfig)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}
