package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class WamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = WamRepository(db)

    // Exposed flows from database
    val allUsers: Flow<List<UserAccount>> = repository.allUsers
    val allTransactions: Flow<List<Transaction>> = repository.allTransactions
    val adminConfig: StateFlow<AdminConfig?> = repository.adminConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminConfig())
    val auditLogs: Flow<List<AuditLog>> = repository.auditLogs

    // Logged-in / Active user profile state (stored in Flow)
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Chat bot message log: Pair of <MessageText, IsUser>
    val chatMessages = mutableStateListOf<Pair<String, Boolean>>()
    var isChatLoading = MutableStateFlow(false)

    // Savings state (simulated Saving Pots)
    private val _savingPotBalanceYer = MutableStateFlow(0.0)
    val savingPotBalanceYer = _savingPotBalanceYer.asStateFlow()

    init {
        viewModelScope.launch {
            repository.verifyAndSeedDatabase()
            // Set first user in DB as default active user so they are never in a null screen initially,
            // but require onboarding onboarding complete to set customized profile details!
            val mainUser = db.userDao().getMainUser()
            _currentUser.value = mainUser

            if (chatMessages.isEmpty()) {
                chatMessages.add(Pair("مرحباً بك في المحفظة الذكية WAM من الأستاذ ماهر أحمد الوتاري! كيف يمكنني مساعدتك مالياً اليوم؟", false))
            }
        }
    }

    /**
     * Requirement 2: Register a new user with real validations (phone uniqueness and 8-char password length).
     */
    suspend fun registerNewUser(fullName: String, phone: String, password: String): String? {
        if (password.length < 8) {
            return "عذراً، يجب أن تتكون كلمة المرور من 8 خانات على الأقل لضمان الأمان الآمن."
        }
        val existing = db.userDao().getUserByPhone(phone)
        if (existing != null) {
            return "عذراً، رقم الهاتف هذا مسجل بالفعل لحساب آخر. يرجى إدخال رقم مختلف."
        }
        
        val newUser = UserAccount(
            fullName = fullName,
            phoneNumber = phone,
            password = password,
            balanceYer = 285400.0,
            balanceUsd = 450.0,
            isFrozen = false
        )
        db.userDao().insertUser(newUser)
        repository.addAuditLog("SYSTEM", true, "تم تسجيل مستخدم جديد بنجاح: $fullName ($phone)")
        return null // Success
    }

    /**
     * Requirement 2: Verify login strictly against the Room database.
     */
    suspend fun loginUser(phone: String, password: String): String? {
        val user = db.userDao().getUserByPhone(phone)
        if (user == null) {
            repository.addAuditLog(phone, false, "فشل محاولة تسجيل الدخول (الحساب غير موجود)")
            return "عذراً، لم يتم العثور على حساب مسجل بهذا الرقم. يرجى التأكد من الرقم أو إنشاء حساب جديد."
        }
        if (user.password != password) {
            repository.addAuditLog(phone, false, "فشل محاولة تسجيل الدخول (كلمة مرور غير صحيحة)")
            return "تنبيه: كلمة المرور المدخلة غير صحيحة. يرجى المحاولة مجدداً."
        }
        if (user.isFrozen) {
            repository.addAuditLog(phone, false, "محاولة تسجيل دخول لحساب مجمد")
            return "تنبيه أمني: الحساب مجمد حالياً بموجب سياسة أمان المالي. يرجى مراجعة الإدارة."
        }

        _currentUser.value = user
        repository.addAuditLog(user.fullName, true, "تم تسجيل الدخول بنجاح للمحفظة")
        return null // Success
    }

    fun updateMainUserProfile(fullName: String, phoneNumber: String) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updated = current.copy(fullName = fullName, phoneNumber = phoneNumber)
                db.userDao().updateUser(updated)
                _currentUser.value = updated
                repository.addAuditLog("SYSTEM", true, "تم تحديث اسم الحساب الترحيبي إلى $fullName ورقم الهاتف لـ $phoneNumber")
            }
        }
    }

    fun performTransfer(recipientPhone: String, amount: Double, currency: String, desc: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val sender = _currentUser.value
            if (sender == null) {
                onResult(false, "لم يتم العثور على جلسة تسجيل دخول صالحة.")
                return@launch
            }

            if (sender.phoneNumber == recipientPhone) {
                onResult(false, "عذراً، لا يمكنك التحويل إلى رقم التحويل الخاص بك.")
                return@launch
            }

            // Verify if recipient exists in DB
            val recipient = db.userDao().getUserByPhone(recipientPhone)
            if (recipient == null) {
                onResult(false, "فشل عملية التحويل: رقم المستلم غير مسجل لخدمات WAM.")
                repository.addAuditLog(sender.fullName, false, "فشل تحويل لعدم وجود المستلم $recipientPhone")
                return@launch
            }

            // Verify balance
            if (currency == "YER") {
                if (sender.balanceYer < amount) {
                    onResult(false, "عذراً، رصيدك من الريال اليمني غير كافٍ لإتمام التحويل.")
                    return@launch
                }
                val updatedSender = sender.copy(balanceYer = sender.balanceYer - amount)
                val updatedRecipient = recipient.copy(balanceYer = recipient.balanceYer + amount)
                db.userDao().updateUser(updatedSender)
                db.userDao().updateUser(updatedRecipient)
                _currentUser.value = updatedSender
            } else {
                if (sender.balanceUsd < amount) {
                    onResult(false, "عذراً، رصيدك من الدولار غير كافٍ لإتمام التحويل.")
                    return@launch
                }
                val updatedSender = sender.copy(balanceUsd = sender.balanceUsd - amount)
                val updatedRecipient = recipient.copy(balanceUsd = recipient.balanceUsd + amount)
                db.userDao().updateUser(updatedSender)
                db.userDao().updateUser(updatedRecipient)
                _currentUser.value = updatedSender
            }

            // Write transactions
            val ref = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            db.transactionDao().insertTransaction(
                Transaction(
                    type = "TRANSFER",
                    amount = amount,
                    currency = currency,
                    title = "تحويل إلى: ${recipient.fullName}",
                    reference = ref
                )
            )

            repository.addAuditLog(sender.fullName, true, "تم تحويل بنجاح $amount $currency إلى ${recipient.fullName}")
            onResult(true, "تم التحويل الفوري بنجاح! رقم المرجعية: $ref")
        }
    }

    fun performDeposit(amount: Double, currency: String, agencyName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updated = if (currency == "YER") {
                user.copy(balanceYer = user.balanceYer + amount)
            } else {
                user.copy(balanceUsd = user.balanceUsd + amount)
            }

            db.userDao().updateUser(updated)
            _currentUser.value = updated

            val ref = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            db.transactionDao().insertTransaction(
                Transaction(
                    type = "DEPOSIT",
                    amount = amount,
                    currency = currency,
                    title = "إيداع نقدي عبر وكيل: $agencyName",
                    reference = ref
                )
            )

            repository.addAuditLog(user.fullName, true, "إيداع مبلغ $amount $currency عبر $agencyName")
            onResult(true, "تم إيداع الرصيد بنجاح لتطبيقك عبر $agencyName!")
        }
    }

    fun performWithdrawal(amount: Double, currency: String, agencyName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (currency == "YER" && user.balanceYer < amount) {
                onResult(false, "عذراً، الرصيد بعملة ريال يمني غير كافٍ للسحب.")
                return@launch
            }
            if (currency == "USD" && user.balanceUsd < amount) {
                onResult(false, "عذراً، الرصيد بعملة دولار غير كافٍ للسحب.")
                return@launch
            }

            val updated = if (currency == "YER") {
                user.copy(balanceYer = user.balanceYer - amount)
            } else {
                user.copy(balanceUsd = user.balanceUsd - amount)
            }

            db.userDao().updateUser(updated)
            _currentUser.value = updated

            val ref = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            db.transactionDao().insertTransaction(
                Transaction(
                    type = "WITHDRAWAL",
                    amount = amount,
                    currency = currency,
                    title = "سحب نقدي من وكيل: $agencyName",
                    reference = ref
                )
            )

            repository.addAuditLog(user.fullName, true, "سحب رصيد بقيمة $amount $currency عبر $agencyName")
            onResult(true, "تم السحب النقدي بنجاح عبر الوكيل والموافقة الفورية!")
        }
    }

    fun performBillPayment(service: String, billingNumber: String, amount: Double, currency: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (currency == "YER" && user.balanceYer < amount) {
                onResult(false, "الرصيد بعملة ريال يمني غير كافٍ لسداد الفاتورة.")
                return@launch
            }
            if (currency == "USD" && user.balanceUsd < amount) {
                onResult(false, "الرصيد بعملة دولار غير كافٍ لسداد الفاتورة.")
                return@launch
            }

            // Complete deduction with cashback logic (+1% returned)
            val cashback = amount * 0.01
            val updated = if (currency == "YER") {
                user.copy(balanceYer = user.balanceYer - amount + cashback)
            } else {
                user.copy(balanceUsd = user.balanceUsd - amount + cashback)
            }

            db.userDao().updateUser(updated)
            _currentUser.value = updated

            val ref = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            db.transactionDao().insertTransaction(
                Transaction(
                    type = "PAYMENT",
                    amount = amount,
                    currency = currency,
                    title = "سداد فاتورة $service برقم $billingNumber (كاش باك 1%)",
                    reference = ref
                )
            )

            repository.addAuditLog(user.fullName, true, "سداد فاتورة $service بقيمة $amount مع كاش باك")
            onResult(true, "تم دفع الفاتورة فوراً بنجاح! تم قيد كاش باك 1% بقيمة $cashback $currency في رصيدك.")
        }
    }

    fun performMicroLoan(amount: Double, months: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(balanceYer = user.balanceYer + amount)
            db.userDao().updateUser(updated)
            _currentUser.value = updated

            val ref = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            db.transactionDao().insertTransaction(
                Transaction(
                    type = "DEPOSIT",
                    amount = amount,
                    currency = "YER",
                    title = "قرض سريع تمت الموافقة عليه (الذكاء الاصطناعي)",
                    reference = ref
                )
            )

            repository.addAuditLog(user.fullName, true, "تمت الموافقة الآلية على قرض بقيمة $amount ر.ي لسداد مرن")
            onResult(true, "تمت الموافقة الفورية عبر الذكاء الاصطناعي وإيداع $amount ر.ي بدون فوائد!")
        }
    }

    fun submitSavingPotDeposit(amount: Double, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (user.balanceYer < amount) {
                onResult(false, "الرصيد غير كافٍ لعملية الادخار.")
                return@launch
            }

            val updated = user.copy(balanceYer = user.balanceYer - amount)
            db.userDao().updateUser(updated)
            _currentUser.value = updated
            _savingPotBalanceYer.value += amount

            val ref = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
            db.transactionDao().insertTransaction(
                Transaction(
                    type = "TRANSFER",
                    amount = amount,
                    currency = "YER",
                    title = "شحن وعاء ادخار ذكي",
                    reference = ref
                )
            )

            repository.addAuditLog(user.fullName, true, "شحن وعاء ادخاري بمبلغ $amount ر.ي")
            onResult(true, "تم الادخار بنجاح! رصيد وعاءك حالياً: ${_savingPotBalanceYer.value} ر.ي بفائدة 0%")
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun askGemini(prompt: String, apiKey: String) {
        viewModelScope.launch {
            if (prompt.isBlank()) return@launch
            chatMessages.add(Pair(prompt, true))
            isChatLoading.value = true

            val aiAnswer = GeminiService.getChatResponse(prompt, apiKey)
            chatMessages.add(Pair(aiAnswer, false))
            isChatLoading.value = false
        }
    }

    fun updateAdminConfig(updated: AdminConfig) {
        viewModelScope.launch {
            db.adminConfigDao().updateConfig(updated)
            repository.addAuditLog("ADMIN", true, "تم تحديث إعدادات النظام وتعديل التفضيلات")
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            db.auditLogDao().clearLogs()
            repository.addAuditLog("WAM2026", true, "تم تصفير سجل التدقيق بالكامل الآمن.")
        }
    }

    fun freezeUserAccount(userId: Int, isFrozen: Boolean) {
        viewModelScope.launch {
            val user = db.userDao().getUserById(userId)
            if (user != null) {
                val updated = user.copy(isFrozen = isFrozen)
                db.userDao().updateUser(updated)
                // If the frozen user was the currentUser, update it
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updated
                }
                repository.addAuditLog("ADMIN", true, "تحديث حالة الحساب للمستخدم ${user.fullName} إلى: " + if(isFrozen) "مجمد" else "نشط")
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
