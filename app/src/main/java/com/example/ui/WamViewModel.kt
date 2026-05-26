package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WamViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = WamRepository(db)

    val mainUser: StateFlow<UserAccount?> = repository.mainUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingPots: StateFlow<List<SavingPot>> = repository.allSavingPots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminConfig: StateFlow<AdminConfig?> = repository.adminConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Admin Credentials State
    var failedLoginAttempts by mutableIntStateOf(0)
    var lockoutTimeRemainingMinutes by mutableIntStateOf(0)
    var isAdminLoggedIn by mutableStateOf(false)
    var adminTriggerMessage by mutableStateOf("")

    // Simulated Lockout variables
    private var lockoutTimestamp: Long = 0

    // Chatbot State
    val chatMessages = mutableStateListOf<Pair<String, Boolean>>() // Pair(MessageContent, isUser)
    var isChatLoading by mutableStateOf(false)

    init {
        viewModelScope.launch {
            repository.verifyAndSeedDatabase()
            // Add initial welcome chat message if empty
            if (chatMessages.isEmpty()) {
                chatMessages.add(Pair("مرحباً بك في المحفظة الذكية WAM من الأستاذ ماهر أحمد الوتاري! كيف يمكنني مساعدتك مالياً اليوم؟", false))
            }
        }
    }

    // Check Lockout on Tick/Action
    fun checkLockout() {
        if (lockoutTimeRemainingMinutes > 0) {
            val elapsedMs = System.currentTimeMillis() - lockoutTimestamp
            val remainingMs = (30 * 60 * 1000) - elapsedMs
            if (remainingMs <= 0) {
                lockoutTimeRemainingMinutes = 0
                failedLoginAttempts = 0
            } else {
                lockoutTimeRemainingMinutes = (remainingMs / 60000).toInt() + 1
            }
        }
    }

    // Attempt Secret Login
    fun attemptAdminLogin(user: String, pass: String): Boolean {
        checkLockout()
        if (lockoutTimeRemainingMinutes > 0) {
            viewModelScope.launch {
                repository.addAuditLog("SECURITY", false, "محاولة تسجيل دخول مرفوضة: النظام مقفل احترازياً.")
            }
            return false
        }

        if (user == "WAM2026" && pass == "maher--736462") {
            isAdminLoggedIn = true
            failedLoginAttempts = 0
            viewModelScope.launch {
                repository.addAuditLog("WAM2026", true, "تم تسجيل دخول ناجح إلى واجهة الإدمن الخفية.")
            }
            return true
        } else {
            failedLoginAttempts++
            if (failedLoginAttempts >= 3) {
                lockoutTimeRemainingMinutes = 30
                lockoutTimestamp = System.currentTimeMillis()
                viewModelScope.launch {
                    repository.addAuditLog("WAM2026", false, "تجاوز الحد الأقصى للمحاولات (3). تم قفل لوحة التحكم الخفية 30 دقيقة.")
                }
            } else {
                viewModelScope.launch {
                    repository.addAuditLog("WAM2026", false, "فشل تسجيل الدخول. المحاولة رقم $failedLoginAttempts")
                }
            }
            return false
        }
    }

    fun logoutAdmin() {
        isAdminLoggedIn = false
    }

    // Dial USSD Code action
    fun dialUSSDCode(code: String): String {
        return if (code == "*777644670#") {
            // Secret SMS link generated
            viewModelScope.launch {
                repository.addAuditLog("USSD", true, "تم طلب كود الطوارئ لفك القفل وإرسال رابط SMS احتياطي.")
            }
            "تم إرسال رابط تسجيل الدخول الاحتياطي للوحة التحكم برسالة قصيرة آمنة إلى الرقم المعتمد +967777644670 بنجاح."
        } else {
            "رمز MMI غير صالح أو مشكلة في اتصال شبكة USSD المباشرة."
        }
    }

    // AI Chatbot Service Call
    fun sendChatMessage(message: String) {
        if (message.isBlank() || isChatLoading) return
        chatMessages.add(Pair(message, true))
        isChatLoading = true

        viewModelScope.launch {
            // Prepare chat history to keep token context concise (last 4 turns)
            val history = chatMessages.takeLast(8).dropLast(1).map {
                val who = if (it.second) "USER" else "MODEL"
                Pair(who, it.first)
            }
            val response = GeminiClient.getChatbotResponse(message, history)
            chatMessages.add(Pair(response, false))
            isChatLoading = false
        }
    }

    fun clearChat() {
        chatMessages.clear()
        chatMessages.add(Pair("مرحباً بك مجدداً! كيف يمكنني مساعدتك الآن؟", false))
    }

    // --- FINANCIAL OPERATIONS ---
    fun transferMoney(phone: String, amount: Double, currency: String, note: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.executeP2PTransfer(phone, amount, currency, note)
            onResult(res)
        }
    }

    fun payBill(service: String, amount: Double, currency: String, providerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.executeBillPayment(service, amount, currency, providerId)
            onResult(res)
        }
    }

    fun rechargeMobile(phone: String, operator: String, amount: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.executeRecharge(phone, operator, amount)
            onResult(res)
        }
    }

    fun microLoan(amount: Double, currency: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.applyMicroLoan(amount, currency)
            onResult(res)
        }
    }

    fun depositOrWithdraw(isDeposit: Boolean, agentPhone: String, amount: Double, currency: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.cashDepositOrWithdraw(isDeposit, agentPhone, amount, currency)
            onResult(res)
        }
    }

    // Saving Pot actions
    fun addSavingPot(title: String, target: Double, currency: String) {
        viewModelScope.launch {
            repository.createSavingPot(title, target, currency)
        }
    }

    fun saveToPot(potId: Int, amount: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.saveToPot(potId, amount)
            onResult(res)
        }
    }

    fun deleteSavingPot(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingPot(id)
        }
    }

    // --- ADMIN CONTROL ACTIONS ---
    fun setAccountStatus(userId: Int, isFreeze: Boolean) {
        viewModelScope.launch {
            repository.toggleUserAccountStatus(userId, isFreeze)
            repository.addAuditLog("WAM2026", true, "تغيير حالة حساب المستخدم رقم $userId إلى ${if (isFreeze) "قيد التجميد" else "نشط"}")
        }
    }

    fun saveAdminThemeSettings(appName: String, primaryColor: String, secondaryColor: String, fee: Double, isCrypto: Boolean, systemFrozen: Boolean) {
        viewModelScope.launch {
            repository.updateConfigData(appName, primaryColor, secondaryColor, fee, isCrypto, systemFrozen)
            repository.addAuditLog("WAM2026", true, "تحديث إعدادات المحفظة والسمات المرئية والعمولات بنجاح.")
        }
    }

    fun clearSystemAuditLogs() {
        viewModelScope.launch {
            repository.clearAuditLogs()
            repository.addAuditLog("WAM2026", true, "تم تصفير سجل التدقيق بالكامل.")
        }
    }

    fun updateMainUserProfile(fullName: String, phoneNumber: String) {
        viewModelScope.launch {
            val main = db.userDao().getMainUser()
            if (main != null) {
                val updated = main.copy(fullName = fullName, phoneNumber = phoneNumber)
                db.userDao().updateUser(updated)
                repository.addAuditLog("SYSTEM", true, "تم تحديث اسم الحساب الترحيبي إلى $fullName ورقم الهاتف إلى $phoneNumber")
            }
        }
    }
}

class WamViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WamViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
