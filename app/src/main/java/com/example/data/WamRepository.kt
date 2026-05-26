package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class WamRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val savingPotDao = db.savingPotDao()
    private val auditLogDao = db.auditLogDao()
    private val configDao = db.adminConfigDao()

    val allUsers: Flow<List<UserAccount>> = userDao.getAllUsersFlow()
    val mainUser: Flow<UserAccount?> = userDao.getMainUserFlow()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val allSavingPots: Flow<List<SavingPot>> = savingPotDao.getAllSavingPotsFlow()
    val allAuditLogs: Flow<List<AuditLog>> = auditLogDao.getAllLogsFlow()
    val adminConfig: Flow<AdminConfig?> = configDao.getConfigFlow()

    suspend fun verifyAndSeedDatabase() {
        // Clear or populate database if empty
        val existingMain = userDao.getMainUser()
        if (existingMain == null) {
            // Main user (the app owner / active wallet)
            val mainUser = UserAccount(
                fullName = "ماهر أحمد الوتاري",
                phoneNumber = "777644670",
                balanceYer = 285400.0,
                balanceUsd = 450.0,
                status = "ACTIVE",
                isMainUser = true
            )
            userDao.insertUser(mainUser)

            // Dynamic agent accounts/others for interactive transfers
            val user2 = UserAccount(
                fullName = "شركة النخبة للخدمات المصرفية (عميل)",
                phoneNumber = "771234567",
                balanceYer = 1200000.0,
                balanceUsd = 2000.0,
                status = "ACTIVE"
            )
            userDao.insertUser(user2)

            val user3 = UserAccount(
                fullName = "سوبر ماركت الهدى (نقطة بيع WAM)",
                phoneNumber = "733445566",
                balanceYer = 15000.0,
                balanceUsd = 50.0,
                status = "ACTIVE"
            )
            userDao.insertUser(user3)

            // Seed Admin Config
            val config = AdminConfig()
            configDao.insertConfig(config)

            // Seed Saving Pots
            savingPotDao.insertSavingPot(SavingPot(
                title = "وعاء الادخار الخاص بـ WAM (صندوق المستقبل)",
                targetAmount = 1000.0,
                currentAmount = 250.0,
                currency = "USD"
            ))
            savingPotDao.insertSavingPot(SavingPot(
                title = "توفير لشراء أجهزة نقطة البيع (SoftPOS)",
                targetAmount = 500000.0,
                currentAmount = 120000.0,
                currency = "YER"
            ))

            // Seed historic transactions
            transactionDao.insertTransaction(Transaction(
                type = "DEPOSIT",
                amount = 300000.0,
                currency = "YER",
                title = "إيداع نقدي عبر الشبكة المصرفية المعتمدة لـ WAM",
                reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))
            transactionDao.insertTransaction(Transaction(
                type = "BILL",
                amount = -4500.0,
                currency = "YER",
                title = "تسديد فاتورة الكهرباء العامة عبر WAM",
                reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))
            transactionDao.insertTransaction(Transaction(
                type = "RECHARGE",
                amount = -1000.0,
                currency = "YER",
                title = "شحن رصيد يمن موبايل مباشر",
                reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))
            transactionDao.insertTransaction(Transaction(
                type = "LOAN",
                amount = 20000.0,
                currency = "YER",
                title = "قرض سريع تمت الموافقة عليه (الذكاء الاصطناعي)",
                reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))

            // Initial success audit log
            auditLogDao.insertLog(AuditLog(
                username = "SYSTEM",
                isSuccess = true,
                description = "تهيئة النظام بنجاح وتشغيل قاعدة البيانات المحلية الآمنة AES-256"
            ))
        }
    }

    suspend fun addAuditLog(username: String, isSuccess: Boolean, description: String) {
        auditLogDao.insertLog(AuditLog(
            username = username,
            isSuccess = isSuccess,
            description = description
        ))
    }

    suspend fun executeP2PTransfer(recipientPhone: String, amount: Double, currency: String, title: String): Boolean {
        val config = configDao.getConfig() ?: AdminConfig()
        if (config.isSystemFrozen) return false

        val main = userDao.getMainUser() ?: return false
        if (main.status == "FROZEN") return false

        // Calculate fee
        val fee = amount * (config.p2pFeePercentage / 100.0)
        val totalDebit = amount + fee

        if (currency == "YER") {
            if (main.balanceYer < totalDebit) return false
            val remainingYer = main.balanceYer - totalDebit
            userDao.updateUserBalance(main.id, remainingYer, main.balanceUsd)
        } else {
            if (main.balanceUsd < totalDebit) return false
            val remainingUsd = main.balanceUsd - totalDebit
            userDao.updateUserBalance(main.id, main.balanceYer, remainingUsd)
        }

        // Add to recipient balance if matches an account
        val recipient = userDao.getAllUsers().firstOrNull { it.phoneNumber == recipientPhone }
        if (recipient != null) {
            if (currency == "YER") {
                userDao.updateUserBalance(recipient.id, recipient.balanceYer + amount, recipient.balanceUsd)
            } else {
                userDao.updateUserBalance(recipient.id, recipient.balanceYer, recipient.balanceUsd + amount)
            }
        }

        // Add dynamic cashback!
        val cashback = amount * 0.01 // 1% cashback on typical transaction
        val updatedMain = userDao.getMainUser() ?: return false
        if (currency == "YER") {
            userDao.updateUserBalance(updatedMain.id, updatedMain.balanceYer + cashback, updatedMain.balanceUsd)
        } else {
            userDao.updateUserBalance(updatedMain.id, updatedMain.balanceYer, updatedMain.balanceUsd + (cashback / 250.0)) // simple rate
        }

        // Save transaction
        transactionDao.insertTransaction(Transaction(
            type = "P2P",
            amount = -amount,
            currency = currency,
            title = "$title (رسوم: $fee $currency | كاش باك +$cashback)",
            reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
        ))

        addAuditLog(
            username = main.phoneNumber,
            isSuccess = true,
            description = "تحويل P2P بقيمة $amount $currency إلى $recipientPhone بنجاح"
        )
        return true
    }

    suspend fun executeBillPayment(serviceName: String, amount: Double, currency: String, providerId: String): Boolean {
        val config = configDao.getConfig() ?: AdminConfig()
        if (config.isSystemFrozen) return false

        val main = userDao.getMainUser() ?: return false
        if (main.status == "FROZEN") return false

        if (currency == "YER") {
            if (main.balanceYer < amount) return false
            userDao.updateUserBalance(main.id, main.balanceYer - amount, main.balanceUsd)
        } else {
            if (main.balanceUsd < amount) return false
            userDao.updateUserBalance(main.id, main.balanceYer, main.balanceUsd - amount)
        }

        transactionDao.insertTransaction(Transaction(
            type = "BILL",
            amount = -amount,
            currency = currency,
            title = "سداد فاتورة $serviceName $providerId",
            reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
        ))

        addAuditLog(
            username = main.phoneNumber,
            isSuccess = true,
            description = "تسديد فاتورة $serviceName ($providerId) بمبلغ $amount $currency"
        )
        return true
    }

    suspend fun executeRecharge(phone: String, operator: String, amount: Double): Boolean {
        val main = userDao.getMainUser() ?: return false
        if (main.status == "FROZEN") return false

        if (main.balanceYer < amount) return false
        userDao.updateUserBalance(main.id, main.balanceYer - amount, main.balanceUsd)

        transactionDao.insertTransaction(Transaction(
            type = "RECHARGE",
            amount = -amount,
            currency = "YER",
            title = "شحن رصيد $operator للرقم $phone",
            reference = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
        ))

        addAuditLog(
            username = main.phoneNumber,
            isSuccess = true,
            description = "شحن رصيد $operator للرقم $phone بمبلغ $amount ريال"
        )
        return true
    }

    suspend fun applyMicroLoan(amount: Double, currency: String): Boolean {
        val main = userDao.getMainUser() ?: return false
        if (main.status == "FROZEN") return false

        if (currency == "YER") {
            userDao.updateUserBalance(main.id, main.balanceYer + amount, main.balanceUsd)
        } else {
            userDao.updateUserBalance(main.id, main.balanceYer, main.balanceUsd + amount)
        }

        transactionDao.insertTransaction(Transaction(
            type = "LOAN",
            amount = amount,
            currency = currency,
            title = "قرض فوري ذكي وافق عليه النظام",
            reference = "LX-${UUID.randomUUID().toString().take(8).uppercase()}"
        ))

        addAuditLog(
            username = main.phoneNumber,
            isSuccess = true,
            description = "الحصول على تمويل أصغر بقيمة $amount $currency مستند للذكاء الاصطناعي"
        )
        return true
    }

    suspend fun cashDepositOrWithdraw(isDeposit: Boolean, agentPhone: String, amount: Double, currency: String): Boolean {
        val main = userDao.getMainUser() ?: return false
        if (main.status == "FROZEN") return false

        if (isDeposit) {
            // Find agent
            val agent = userDao.getAllUsers().firstOrNull { it.phoneNumber == agentPhone }
            if (currency == "YER") {
                userDao.updateUserBalance(main.id, main.balanceYer + amount, main.balanceUsd)
                if (agent != null) {
                    userDao.updateUserBalance(agent.id, agent.balanceYer - amount, agent.balanceUsd)
                }
            } else {
                userDao.updateUserBalance(main.id, main.balanceYer, main.balanceUsd + amount)
                if (agent != null) {
                    userDao.updateUserBalance(agent.id, agent.balanceYer, agent.balanceUsd - amount)
                }
            }

            transactionDao.insertTransaction(Transaction(
                type = "CASH_IN",
                amount = amount,
                currency = currency,
                title = "إيداع نقدي مباشر لدى الوكيل $agentPhone",
                reference = "DX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))
        } else {
            // Withdraw
            if (currency == "YER") {
                if (main.balanceYer < amount) return false
                userDao.updateUserBalance(main.id, main.balanceYer - amount, main.balanceUsd)
            } else {
                if (main.balanceUsd < amount) return false
                userDao.updateUserBalance(main.id, main.balanceYer, main.balanceUsd - amount)
            }

            transactionDao.insertTransaction(Transaction(
                type = "CASH_OUT",
                amount = -amount,
                currency = currency,
                title = "سحب نقدي مباشر لدى الوكيل $agentPhone",
                reference = "WX-${UUID.randomUUID().toString().take(8).uppercase()}"
            ))
        }

        addAuditLog(
            username = main.phoneNumber,
            isSuccess = true,
            description = "معاملة ${if (isDeposit) "إيداع" else "سحب"} بقيمة $amount $currency لدى $agentPhone"
        )
        return true
    }

    suspend fun saveToPot(potId: Int, amount: Double): Boolean {
        val main = userDao.getMainUser() ?: return false
        if (main.status == "FROZEN") return false

        val pots = db.savingPotDao().getAllSavingPotsFlow().firstOrNull() ?: emptyList()
        val pot = pots.firstOrNull { it.id == potId } ?: return false

        if (pot.currency == "YER") {
            if (main.balanceYer < amount) return false
            userDao.updateUserBalance(main.id, main.balanceYer - amount, main.balanceUsd)
        } else {
            if (main.balanceUsd < amount) return false
            userDao.updateUserBalance(main.id, main.balanceYer, main.balanceUsd - amount)
        }

        val updatedPot = pot.copy(currentAmount = pot.currentAmount + amount)
        savingPotDao.updateSavingPot(updatedPot)

        transactionDao.insertTransaction(Transaction(
            type = "SAVING",
            amount = -amount,
            currency = pot.currency,
            title = "تمويل الوعاء الادخاري: ${pot.title}",
            reference = "SX-${UUID.randomUUID().toString().take(8).uppercase()}"
        ))

        addAuditLog(
            username = main.phoneNumber,
            isSuccess = true,
            description = "توفير مبلغ $amount ${pot.currency} في الوعاء '${pot.title}'"
        )
        return true
    }

    suspend fun createSavingPot(title: String, target: Double, currency: String) {
        savingPotDao.insertSavingPot(SavingPot(
            title = title,
            targetAmount = target,
            currentAmount = 0.0,
            currency = currency
        ))
    }

    suspend fun deleteSavingPot(id: Int) {
        savingPotDao.deleteSavingPot(id)
    }

    // --- ADMIN METHODS ---
    suspend fun updateAdminConfig(config: AdminConfig) {
        configDao.updateConfig(config)
    }

    suspend fun toggleUserAccountStatus(userId: Int, isFreeze: Boolean) {
        val status = if (isFreeze) "FROZEN" else "ACTIVE"
        userDao.updateUserStatus(userId, status)
    }

    suspend fun updateConfigData(name: String, primary: String, secondary: String, fee: Double, isCrypto: Boolean, systemFrozen: Boolean) {
        val existing = configDao.getConfig() ?: AdminConfig()
        val updated = existing.copy(
            appName = name,
            primaryColorHex = primary,
            secondaryColorHex = secondary,
            p2pFeePercentage = fee,
            isCryptoEnabled = isCrypto,
            isSystemFrozen = systemFrozen
        )
        configDao.insertConfig(updated)
    }

    suspend fun clearAuditLogs() {
        auditLogDao.clearLogs()
    }
}
