package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phoneNumber: String, // Must be unique
    val password: String,
    val balanceYer: Double = 285400.0,
    val balanceUsd: Double = 450.0,
    val isFrozen: Boolean = false
)

@Entity(tableName = "transactions")
@Serializable
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "PAYMENT", "TRANSFER", "DEPOSIT", "WITHDRAWAL"
    val amount: Double,
    val currency: String, // "YER", "USD"
    val title: String,
    val reference: String,
    val isSuccess: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_config")
@Serializable
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val appName: String = "الماهر موني",
    val primaryColor: String = "#FFD700", // Gold
    val secondaryColor: String = "#00D4FF", // Electric Blue
    val p2pFeePercent: Double = 1.0,
    val isCryptoEnabled: Boolean = true,
    val isSystemFrozen: Boolean = false,
    val partnerCompanies: String = "شركة النخبة المصرفية, شبكة الأمان المالية", // WAM approved network partners
    val customWelcomeMessage: String = "مرحباً بك في جيل المال الذكي والآمن"
)

@Entity(tableName = "audit_logs")
@Serializable
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user: String,
    val isSuccess: Boolean,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
