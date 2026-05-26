package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WamRepository(private val db: AppDatabase) {

    val allUsers: Flow<List<UserAccount>> = db.userDao().getAllUsersFlow()
    val allTransactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactionsFlow()
    val adminConfig: Flow<AdminConfig?> = db.adminConfigDao().getConfigFlow()
    val auditLogs: Flow<List<AuditLog>> = db.auditLogDao().getAllLogsFlow()

    suspend fun verifyAndSeedDatabase() {
        // 1. Seed Config if missing
        val existingConfig = db.adminConfigDao().getConfig()
        if (existingConfig == null) {
            db.adminConfigDao().insertConfig(AdminConfig())
        }

        // 2. Seed main owner account if missing
        val existingMain = db.userDao().getMainUser()
        if (existingMain == null) {
            val mainUser = UserAccount(
                fullName = "ماهر أحمد الوتاري",
                phoneNumber = "777644670",
                password = "maher--736462", // Default secure login
                balanceYer = 285400.0,
                balanceUsd = 450.0,
                isFrozen = false
            )
            db.userDao().insertUser(mainUser)

            // Seed other interactive dummy agent/customer account
            val agentUser = UserAccount(
                fullName = "شركة النخبة للخدمات المصرفية (عميل)",
                phoneNumber = "771234567",
                password = "password123",
                balanceYer = 1200000.0,
                balanceUsd = 20000.0,
                isFrozen = false
            )
            db.userDao().insertUser(agentUser)

            // 3. Seed initial transactions
            val transactionDao = db.transactionDao()
            transactionDao.insertTransaction(Transaction(
                type = "DEPOSIT",
                amount = 300000.0,
                currency = "YER",
                title = "إيداع نقدي عبر الشبكة المصرفية المعتمدة لـ WAM",
                reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))
            transactionDao.insertTransaction(Transaction(
                type = "TRANSFER",
                amount = 150.0,
                currency = "USD",
                title = "تحويل أرباح محفظة WAM الذكية",
                reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))

            // Seed audit logs
            addAuditLog("SYSTEM", true, "تم تهيئة قاعدة البيانات الآمنة لـ WAM بنجاح")
        }
    }

    suspend fun addAuditLog(user: String, isSuccess: Boolean, action: String) {
        db.auditLogDao().insertLog(
            AuditLog(
                user = user,
                isSuccess = isSuccess,
                action = action
            )
        )
    }
}
