package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phoneNumber: String,
    val balanceYer: Double,
    val balanceUsd: Double,
    val status: String, // "ACTIVE" or "FROZEN"
    val isMainUser: Boolean = false
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "P2P", "BILL", "RECHARGE", "CASH_OUT", "LOAN", "SAVING"
    val amount: Double,
    val currency: String, // "YER" or "USD"
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reference: String = ""
)

@Entity(tableName = "saving_pots")
data class SavingPot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val username: String,
    val isSuccess: Boolean,
    val ipAddress: String = "192.168.1.1",
    val description: String
)

@Entity(tableName = "admin_configs")
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val appName: String = "الماهر موني",
    val primaryColorHex: String = "#FFD700", // WAM Gold
    val secondaryColorHex: String = "#00D4FF", // WAM Electric
    val p2pFeePercentage: Double = 0.5,
    val isCryptoEnabled: Boolean = true,
    val isSystemFrozen: Boolean = false,
    val serviceLimitYer: Double = 500000.0
)
