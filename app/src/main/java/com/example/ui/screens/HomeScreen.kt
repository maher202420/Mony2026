package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.UserAccount
import com.example.ui.WamViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WamViewModel,
    currentUser: UserAccount,
    onNavigateToSavings: () -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val adminConfig by viewModel.adminConfig.collectAsState()

    // Dialog Toggles
    var showTransferDialog by remember { mutableStateOf(false) }
    var showAgentDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }

    // Color definitions
    val primaryColor = Color(0xFFFFD700) // Gold
    val secondaryColor = Color(0xFF00D4FF) // Electric Blue
    val darkBgColor = Color(0xFF0A0E17)
    val cardColor = Color(0xFF131722)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBgColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Greeting
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "أهلاً بك في جيل المال الذكي",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currentUser.fullName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(secondaryColor, primaryColor))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = "Wallet Icon",
                        tint = Color.Black
                    )
                }
            }
        }

        // Wallet Balance Cards (YER & USD)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // YER Balance Column
                    Column {
                        Text(
                            text = "الرصيد الكلي بالريال اليمني",
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = String.format("%,.0f", currentUser.balanceYer),
                                color = primaryColor,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " ريال يمني", // Corrected spelling T4
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                    // USD Balance Column
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الرصيد الموازي بالدولار الأمريكي",
                                color = Color(0xFF9E9E9E),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = String.format("%,.2f", currentUser.balanceUsd),
                                    color = secondaryColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " دولار أمريكي",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                                )
                            }
                        }

                        // Fully secured flag (No letters status as per T3)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "بروتوكول WAM المالي الآمن وتحت إشراف المالك", // Corrected spelling T4 & T6
                                color = primaryColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Operational Actions Grid
        item {
            Text(
                text = "الخدمات المالية الأساسية",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OperationButton(
                    title = "تحويل سريع P2P",
                    sub = "برقم هاتف المستلم مباشرة",
                    icon = Icons.Default.SendToMobile,
                    color = primaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = { showTransferDialog = true }
                )

                OperationButton(
                    title = "سحب وإيداع (الوكلاء)",
                    sub = "الشبكة المصرفية لـ WAM", // Deleted Al-Saifi mention T5
                    icon = Icons.Default.LocationOn,
                    color = secondaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = { showAgentDialog = true }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OperationButton(
                    title = "دفع الفواتير والشحن",
                    sub = "الهاتف الكهرباء والإنترنت",
                    icon = Icons.Default.ReceiptLong,
                    color = secondaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = { showBillDialog = true }
                )

                OperationButton(
                    title = "الأوعية الادخارية",
                    sub = "ادخار ذكي بفائدة صفرية",
                    icon = Icons.Default.Savings,
                    color = primaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSavings
                )
            }
        }

        // Transactions Header
        item {
            Text(
                text = "سجل الحركات المصرفية الأخيرة",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Recent Transactions list
        if (transactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "لا توجد حركات منجزة حالياً. ابدأ بتحويل رصيد أو سداد فواتير لتفعيل كاش باك 1%!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions.take(8)) { tx ->
                TransactionRow(tx = tx, primaryColor = primaryColor, cardColor = cardColor)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- DIALOGS FOR MAIN WORKFLOWS ---

    // 1. Transfer Dialog
    if (showTransferDialog) {
        var recipientPhone by remember { mutableStateOf("") }
        var transferAmount by remember { mutableStateOf("") }
        var transferCurrency by remember { mutableStateOf("YER") }
        var transferNote by remember { mutableStateOf("") }
        var isTransferring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = {
                Text(
                    text = "تحويل أمان مصرفي P2P",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "يمكنك تحويل الأموال فوراً لأي شخص مسجل في WAM برقم هاتفه مباشرة وبدون عمولات إضافية.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("رقم هاتف المستلم (مثال: 771234567)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("recipient_phone_input")
                    )

                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("المبلغ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input")
                    )

                    // YER or USD Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { transferCurrency = "YER" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transferCurrency == "YER") primaryColor else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "ريال يمني", // Corrected spelling T4
                                color = if (transferCurrency == "YER") Color.Black else Color.White
                            )
                        }
                        Button(
                            onClick = { transferCurrency = "USD" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transferCurrency == "USD") secondaryColor else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "دولار أمريكي",
                                color = if (transferCurrency == "USD") Color.Black else Color.White
                            )
                        }
                    }

                    OutlinedTextField(
                        value = transferNote,
                        onValueChange = { transferNote = it },
                        label = { Text("سبب التحويل أو ملاحظة (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = transferAmount.toDoubleOrNull()
                        if (recipientPhone.isBlank() || amountVal == null || amountVal <= 0) {
                            Toast.makeText(context, "الرجاء تعبئة بيانات التحويل بدقة وصحة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isTransferring = true
                        viewModel.performTransfer(
                            recipientPhone = recipientPhone,
                            amount = amountVal,
                            currency = transferCurrency,
                            desc = transferNote
                        ) { success, message ->
                            isTransferring = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            if (success) showTransferDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !isTransferring
                ) {
                    Text(if (isTransferring) "جاري النقل المشفر..." else "إتمام التحويل الفوري", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("إلغاء العملية", color = Color.Gray)
                }
            },
            containerColor = cardColor
        )
    }

    // 2. Deposit & Withdraw Dialog (No Al-Saifi)
    if (showAgentDialog) {
        var selectedAgency by remember { mutableStateOf("شبكة النخبة المصرفية") }
        var transactionAmount by remember { mutableStateOf("") }
        var targetCurrency by remember { mutableStateOf("YER") }
        var isActionDeposit by remember { mutableStateOf(true) } // True = Deposit, False = Withdrawal
        var isProcessing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAgentDialog = false },
            title = {
                Text(
                    text = "سحب وإيداع عبر شبكة الوكلاء",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "اختر أحد وكلائنا المصرفيين المعتمدين لـ WAM لإيداع رصيد نقدي في محفظتك أو سحبه كاش فوراً.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Toggle Action: Deposit vs Withdrawal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isActionDeposit = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActionDeposit) primaryColor else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إيداع كاش", color = if (isActionDeposit) Color.Black else Color.White)
                        }
                        Button(
                            onClick = { isActionDeposit = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isActionDeposit) secondaryColor else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("سحب نقدي كاش", color = if (!isActionDeposit) Color.Black else Color.White)
                        }
                    }

                    // Choose Agent (NO Al-Saifi)
                    val agenciesList = listOf("شبكة النخبة للخدمات المصرفية", "شبكة الأمان للتبادل المالي المعتمد", "بنك التمويل الأصغر WAM")
                    Text("الجهة المعتمدة:", color = Color.White, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        agenciesList.forEach { valAgency ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedAgency == valAgency) primaryColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (selectedAgency == valAgency) primaryColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedAgency = valAgency }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = valAgency.replace("المصرفية","").replace("للخدمات","").replace("للتبادل","").trim(),
                                    color = if (selectedAgency == valAgency) primaryColor else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = transactionAmount,
                        onValueChange = { transactionAmount = it },
                        label = { Text("أدخل مبلغ العملية") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Currency toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { targetCurrency = "YER" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (targetCurrency == "YER") primaryColor else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ريال يمني", color = if (targetCurrency == "YER") Color.Black else Color.White)
                        }
                        Button(
                            onClick = { targetCurrency = "USD" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (targetCurrency == "USD") secondaryColor else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("دولار أمريكي", color = if (targetCurrency == "USD") Color.Black else Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = transactionAmount.toDoubleOrNull()
                        if (amountVal == null || amountVal <= 0) {
                            Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isProcessing = true
                        if (isActionDeposit) {
                            viewModel.performDeposit(amountVal, targetCurrency, selectedAgency) { success, msg ->
                                isProcessing = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) showAgentDialog = false
                            }
                        } else {
                            viewModel.performWithdrawal(amountVal, targetCurrency, selectedAgency) { success, msg ->
                                isProcessing = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) showAgentDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !isProcessing
                ) {
                    Text(if (isProcessing) "جاري التحقق الفوري..." else "تنفيذ المعاملة", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgentDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            containerColor = cardColor
        )
    }

    // 3. Bill Payment Dialog
    if (showBillDialog) {
        var selectedService by remember { mutableStateOf("يمن موبايل (رصيد)") }
        var billingNumber by remember { mutableStateOf("") }
        var billAmount by remember { mutableStateOf("") }
        var isPayingBill by remember { mutableStateOf(false) }

        val services = listOf(
            "يمن موبايل (رصيد)", "يو YOU (شحن مباشرة)", 
            "سبأفون (باقات)", "إنترنت ADSL وفايبر", 
            "الكهرباء الكلية العامة", "المؤسسة العامة للمياه"
        )

        AlertDialog(
            onDismissRequest = { showBillDialog = false },
            title = {
                Text(
                    text = "سداد الفواتير والشحن الفوري",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ادفع فواتير الإنترنت، الاتصالات، الماء، والكهرباء من رصيدك مباشرة واحصل على كاش باك 1% فورياً.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Service lists selection
                    Text("اختر الخدمة أو شبكة الاتصال:", color = Color.White, fontSize = 11.sp)
                    billingNumber = billingNumber // keeping state
                    
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            services.forEach { serviceName ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedService == serviceName) primaryColor.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedService = serviceName }
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (selectedService == serviceName) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                        } else {
                                            Spacer(modifier = Modifier.width(14.dp))
                                        }
                                        Text(text = serviceName, color = if (selectedService == serviceName) primaryColor else Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = billingNumber,
                        onValueChange = { billingNumber = it },
                        label = { Text("رقم الهاتف أو المشترك (مثال: 777644670)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("المبلغ (بالريال اليمني)") }, // Corrected spelling T4
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = billAmount.toDoubleOrNull()
                        if (billingNumber.isBlank() || amountVal == null || amountVal <= 0) {
                            Toast.makeText(context, "الرجاء إدخال البيانات بصورة صحيحة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isPayingBill = true
                        viewModel.performBillPayment(selectedService, billingNumber, amountVal, "YER") { success, msg ->
                            isPayingBill = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) showBillDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !isPayingBill
                ) {
                    Text(if (isPayingBill) "جاري التسديد الآمن..." else "تأكيد الدفع والكاش باك", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBillDialog = false }) {
                    Text("إلغاء سداد الفواتير", color = Color.Gray)
                }
            },
            containerColor = cardColor
        )
    }
}

@Composable
fun OperationButton(
    title: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sub,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    tx: Transaction,
    primaryColor: Color,
    cardColor: Color
) {
    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val formattedDate = fmt.format(Date(tx.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon according to transaction type
            val (icon, tint) = when (tx.type) {
                "DEPOSIT" -> Icons.Default.AddCircle to Color(0xFF00E676)
                "WITHDRAWAL" -> Icons.Default.RemoveCircle to Color(0xFFFF3B30)
                "TRANSFER" -> Icons.Default.ArrowForward to Color(0xFF00D4FF)
                else -> Icons.Default.CheckCircle to Color(0xFFFFD700)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "مرجع: ${tx.reference} • $formattedDate",
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val amountSign = if (tx.type == "WITHDRAWAL") "-" else "+"
                val currencySymOrName = if (tx.currency == "YER") "ر.ي" else "$"
                Text(
                    text = "$amountSign ${String.format(if(tx.currency == "YER") "%,.0f" else "%,.2f", tx.amount)} $currencySymOrName",
                    color = tint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "ناجحة",
                    color = Color(0xFF00E676),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
