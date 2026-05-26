package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AdminConfig
import com.example.data.Transaction
import com.example.data.UserAccount
import com.example.ui.WamViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: WamViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    val mainUser by viewModel.mainUser.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val adminConfig by viewModel.adminConfig.collectAsState()

    var showTransferDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }
    var showRechargeDialog by remember { mutableStateOf(false) }
    var showAgentDialog by remember { mutableStateOf(false) }
    var showNfcDialog by remember { mutableStateOf(false) }
    var showUssdDialog by remember { mutableStateOf(false) }

    val isFrozen = mainUser?.status == "FROZEN" || adminConfig?.isSystemFrozen == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Welcome Statement
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "مرحباً بك في جيل المال الذكي",
                            color = Color(0xFFE0E0E0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = mainUser?.fullName ?: "تحميل الحساب...",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(secondaryColor, primaryColor)))
                            .border(1.5.dp, primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "ملف المستخدم",
                            tint = Color.Black
                        )
                    }
                }
            }

            // System State Warnings
            if (isFrozen) {
                item {
                    val message = if (adminConfig?.isSystemFrozen == true) {
                        "يتعرض النظام حالياً لصيانة طارئة أو تجميد أمان من مالك التطبيق الأستاذ ماهر أحمد الوتاري. يرجى مراجعة الدعم الفني."
                    } else {
                        "تنبيه أمني: تم تجميد حسابك مؤقتاً لمخالفين شروط الاستخدام. يرجى التواصل مع الدعم الفني لمالك التطبيق WAM."
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x33FF3B30)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF3B30), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "قفل النظام", tint = Color(0xFFFF3B30))
                            Text(
                                text = message,
                                color = Color(0xFFFF8A80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Wallet Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("wallet_balance_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حسابك الإلكتروني المعتمد WAM",
                                color = Color(0xFFE0E0E0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "تشفير AES-256 لـ WAM",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large YER Balance
                        Text(
                            text = "${formatAmount(mainUser?.balanceYer ?: 0.0)} ريال يمني",
                            color = primaryColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // USD Balance
                        Text(
                            text = "$${formatAmount(mainUser?.balanceUsd ?: 0.0)} دولار أمريكي",
                            color = secondaryColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الشبكة المعتمدة: بروتوكول WAM المالي الآمن وتحت إشراف المالك",
                                color = Color(0xFF9E9E9E),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "الحالة: ${if (mainUser?.status == "ACTIVE") "نشط وآمن" else "حقوق مجمدة"}",
                                color = if (mainUser?.status == "ACTIVE") Color(0xFF00E676) else Color(0xFFFF3B30),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Operations Grid title
            item {
                Text(
                    text = "الخدمات السريعة والمباشرة",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Quick Operations Grid Row 1 (P2P + Bill)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OperationButton(
                        title = "تحويل P2P فوري",
                        sub = "برقم الهاتف أو الـ QR",
                        icon = Icons.Default.Send,
                        color = primaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isFrozen) {
                                Toast.makeText(context, "الخدمات متوقفة حالياً لدواعي الأمان", Toast.LENGTH_LONG).show()
                            } else {
                                showTransferDialog = true
                            }
                        }
                    )

                    OperationButton(
                        title = "تسديد فواتير WAM",
                        sub = "كهرباء، مياه، ألياف",
                        icon = Icons.Default.Home,
                        color = secondaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isFrozen) {
                                Toast.makeText(context, "الخدمات متوقفة حالياً", Toast.LENGTH_LONG).show()
                            } else {
                                showBillDialog = true
                            }
                        }
                    )
                }
            }

            // Quick Operations Grid Row 2 (Recharge + Agents)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OperationButton(
                        title = "شحن رصيد الجوال",
                        sub = "مباشر لجميع الأرقام",
                        icon = Icons.Default.PhoneAndroid,
                        color = secondaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isFrozen) {
                                Toast.makeText(context, "الخدمة غير متاحة حالياً", Toast.LENGTH_LONG).show()
                            } else {
                                showRechargeDialog = true
                            }
                        }
                    )

                    OperationButton(
                        title = "سحب وإيداع (الوكلاء)",
                        sub = "وكلاء ومصارف WAM المعتمدين",
                        icon = Icons.Default.LocationOn,
                        color = primaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isFrozen) {
                                Toast.makeText(context, "الخدمة غير متاحة", Toast.LENGTH_LONG).show()
                            } else {
                                showAgentDialog = true
                            }
                        }
                    )
                }
            }

            // Quick Operations Grid Row 3 (SoftPOS + USSD)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OperationButton(
                        title = "SoftPOS البطاقات",
                        sub = "استقبال مدفوعات NFC",
                        icon = Icons.Default.Nfc,
                        color = primaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isFrozen) {
                                Toast.makeText(context, "الخدمة معطلة", Toast.LENGTH_LONG).show()
                            } else {
                                showNfcDialog = true
                            }
                        }
                    )

                    OperationButton(
                        title = "استجابة USSD الطارئة",
                        sub = "معاملات دون إنترنت",
                        icon = Icons.Default.Dialpad,
                        color = secondaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = { showUssdDialog = true }
                    )
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "آخر المعاملات والمدفوعات الآمنة",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "كاش باك 1% نشط",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Transactions list
            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "لا يوجد معاملات",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد معاملات منجزة حتى الآن. ابدأ بتحويل رصيد أو سداد فواتير لتفعيل الكاش باك الذكي!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionItem(tx = tx, primaryColor = primaryColor)
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // --- DIALOGS ---

    // 1. P2P Transfer Dialog
    if (showTransferDialog) {
        var recipientPhone by remember { mutableStateOf("") }
        var transferAmount by remember { mutableStateOf("") }
        var selectedCurrency by remember { mutableStateOf("YER") }
        var notes by remember { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showTransferDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, primaryColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تحويل مالي فوري (P2P)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الرسوم المقررة: ${adminConfig?.p2pFeePercentage ?: 0.5}% | كاش باك +1% فوري للمرسل والمستقبل",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("رقم هاتف المستلم (77xxxxxxx)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("مبلغ التحويل", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedCurrency == "YER",
                                onClick = { selectedCurrency = "YER" },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Text("ريال يمني (YER)", color = Color.White, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedCurrency == "USD",
                                onClick = { selectedCurrency = "USD" },
                                colors = RadioButtonDefaults.colors(selectedColor = secondaryColor)
                            )
                            Text("دولار (USD)", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("سبب التحويل أو ملاحظات", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showTransferDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amtNum = transferAmount.toDoubleOrNull()
                                if (recipientPhone.isBlank() || amtNum == null || amtNum <= 0) {
                                    Toast.makeText(context, "يرجى تعبئة كافة الحقول بشكل صحيح", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isProcessing = true
                                viewModel.transferMoney(recipientPhone, amtNum, selectedCurrency, if(notes.isBlank()) "تحويل مالي شخصي WAM" else notes) { success ->
                                    isProcessing = false
                                    if (success) {
                                        Toast.makeText(context, "تم إنجاز التحويل الفوري بنجاح وتحصيل كاش باك رائع!", Toast.LENGTH_LONG).show()
                                        showTransferDialog = false
                                    } else {
                                        Toast.makeText(context, "فشل الإرسال: رصيدك الحالي غير كافٍ لتحقيق الإجراء أو الحساب مجمد", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            } else {
                                Text("إرسال فوري", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. Bill Payment Dialog
    if (showBillDialog) {
        var selectedService by remember { mutableStateOf("كهرباء عامة") }
        var providerId by remember { mutableStateOf("") }
        var billAmount by remember { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showBillDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, secondaryColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تسديد فواتير الخدمات فروع اليمن",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Services Dropdown Row Simulation
                    val services = listOf("كهرباء عامة", "مؤسسة المياه", "الهاتف الثابت", "إنترنت يمن نت ADSL", "ألياف ضوئية FTTH")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        services.take(3).forEach { s ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedService == s) secondaryColor else Color.DarkGray)
                                    .clickable { selectedService = s }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s, color = if(selectedService == s) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        services.takeLast(2).forEach { s ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedService == s) secondaryColor else Color.DarkGray)
                                    .clickable { selectedService = s }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s, color = if(selectedService == s) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = providerId,
                        onValueChange = { providerId = it },
                        label = { Text("رقم الحساب أو الاشتراك للمشترك", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("المبلغ (ريال يمني YER)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showBillDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amt = billAmount.toDoubleOrNull()
                                if (providerId.isBlank() || amt == null || amt <= 0) {
                                    Toast.makeText(context, "الرجاء تعبئة البيانات بالكامل", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isProcessing = true
                                viewModel.payBill(selectedService, amt, "YER", providerId) { success ->
                                    isProcessing = false
                                    if (success) {
                                        Toast.makeText(context, "تم تسديد الفاتورة بنجاح عبر بوابة سداد WAM الآمنة!", Toast.LENGTH_LONG).show()
                                        showBillDialog = false
                                    } else {
                                        Toast.makeText(context, "رصيدك غير كافي للاستمرار مع الرسوم", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            } else {
                                Text("تسديد", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Recharge Phone Dialog
    if (showRechargeDialog) {
        var mobileNum by remember { mutableStateOf("") }
        var selectedOperator by remember { mutableStateOf("يمن موبايل") }
        var rechargeAmt by remember { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showRechargeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, secondaryColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "شحن رصيد وباقات فوري وبثوانٍ",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val operators = listOf("يمن موبايل", "يو YOU", "سبأفون", "واي Y")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        operators.forEach { op ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedOperator == op) secondaryColor else Color.DarkGray)
                                    .clickable { selectedOperator = op }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(op, color = if(selectedOperator == op) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mobileNum,
                        onValueChange = { mobileNum = it },
                        label = { Text("رقم الجوال الشاحن (7xxxxxxxx)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rechargeAmt,
                        onValueChange = { rechargeAmt = it },
                        label = { Text("المبلغ المراد شحنه (YER)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showRechargeDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amt = rechargeAmt.toDoubleOrNull()
                                if (mobileNum.isBlank() || amt == null || amt <= 0) {
                                    Toast.makeText(context, "الرجاء كمال البيانات للرقم والمبلغ", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isProcessing = true
                                viewModel.rechargeMobile(mobileNum, selectedOperator, amt) { success ->
                                    isProcessing = false
                                    if (success) {
                                        Toast.makeText(context, "تم الشحن المباشر وتفعيل باقات $selectedOperator بنجاح!", Toast.LENGTH_LONG).show()
                                        showRechargeDialog = false
                                    } else {
                                        Toast.makeText(context, "فشل الإجراء: رصيدك بالريال لا يكفي", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            } else {
                                Text("اشحن الآن", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Agent Withdrawal / Deposit Dialog
    if (showAgentDialog) {
        var isDeposit by remember { mutableStateOf(true) } // true = deposit, false = withdraw
        var agentPhone by remember { mutableStateOf("771234567") } // Initialized placeholder
        var amountStr by remember { mutableStateOf("") }
        var selectedCurr by remember { mutableStateOf("YER") }
        var isProcessing by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAgentDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, primaryColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "عمليات الوكلاء والصرافين WAM",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Deposit vs Withdrawal Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isDeposit = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if(isDeposit) primaryColor else Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إيداع نقدي WAM", color = if(isDeposit) Color.Black else Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { isDeposit = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if(!isDeposit) primaryColor else Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("سحب نقدي للوكيل", color = if(!isDeposit) Color.Black else Color.White, fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = agentPhone,
                        onValueChange = { agentPhone = it },
                        label = { Text("هوية أو رقم هاتف الوكيل المعتمد", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("المبلغ المطلوب", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedCurr == "YER",
                                onClick = { selectedCurr = "YER" },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Text("YER ريال يمني", color = Color.White, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedCurr == "USD",
                                onClick = { selectedCurr = "USD" },
                                colors = RadioButtonDefaults.colors(selectedColor = secondaryColor)
                            )
                            Text("USD دولار أمريكي", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAgentDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amt = amountStr.toDoubleOrNull()
                                if (agentPhone.isBlank() || amt == null || amt <= 0) {
                                    Toast.makeText(context, "الرجاء إدخال البيانات المطلوبة", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isProcessing = true
                                viewModel.depositOrWithdraw(isDeposit, agentPhone, amt, selectedCurr) { success ->
                                    isProcessing = false
                                    if (success) {
                                        val opStr = if(isDeposit) "الإيداع" else "السحب"
                                        Toast.makeText(context, "تمت عملية $opStr بنجاح والتحقق الفوري!", Toast.LENGTH_LONG).show()
                                        showAgentDialog = false
                                    } else {
                                        Toast.makeText(context, "رصيد الوهاب أو الوكيل غير كافي للمضي", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            } else {
                                Text(if(isDeposit) "إجراء الإيداع" else "إجراء السحب", color = Color.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. SoftPOS NFC Card Dialog Simulation
    if (showNfcDialog) {
        var isScanning by remember { mutableStateOf(true) }
        var scanSuccess by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = showNfcDialog) {
            kotlinx.coroutines.delay(2500)
            isScanning = false
            scanSuccess = true
        }

        Dialog(onDismissRequest = { showNfcDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, primaryColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "محول الهاتف الذكي SoftPOS NFC",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (isScanning) {
                        CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "بانتظار تقريب البطاقة البنكية أو الهاتف الآخر من ظهر الجهاز لقراءة NFC...",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    } else if (scanSuccess) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "قراءة ناجحة", tint = Color.Black)
                        }
                        Text(
                            text = "تم التقاط بطاقة Visa/Mastercard بنجاح! قارئ البطاقات WAM SoftPOS يعمل بشكل مثالي في البيئة الافتراضية للجمهورية اليمنية والخليج.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showNfcDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("العودة للرئيسية", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // 6. USSD Code Dialer Dialog
    if (showUssdDialog) {
        var ussdInput by remember { mutableStateOf("*777644670#") }
        var userPhoneDialResult by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showUssdDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, secondaryColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "طلب أكواد USSD المالية (بدون إنترنت)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الكود المعتمد لـ WAM طوارئ فك القفل عبر SMS هو: *777644670#",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = ussdInput,
                        onValueChange = { ussdInput = it },
                        label = { Text("أدخل كود USSD المطلوب", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (userPhoneDialResult.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111122)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = userPhoneDialResult,
                                color = secondaryColor,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showUssdDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إغلاق", color = Color.White)
                        }

                        Button(
                            onClick = {
                                userPhoneDialResult = viewModel.dialUSSDCode(ussdInput.trim())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("طلب اتصال USSD", color = Color.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OperationButton(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = modifier
            .border(0.5.dp, Color(0xFF2D2D3D), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction, primaryColor: Color) {
    val isDebe = tx.amount < 0
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateStr = format.format(Date(tx.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF2E2E3E), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDebe) Color(0x22FF3B30) else Color(0x2200E676)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when(tx.type) {
                        "P2P" -> Icons.Default.Send
                        "BILL" -> Icons.Default.ReceiptLong
                        "RECHARGE" -> Icons.Default.PhoneAndroid
                        "CASH_IN" -> Icons.Default.AddCircleOutline
                        "CASH_OUT" -> Icons.Default.RemoveCircleOutline
                        "LOAN" -> Icons.Default.AccountBalanceWallet
                        else -> Icons.Default.Paid
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = tx.type,
                        tint = if (isDebe) Color(0xFFFF3B30) else Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = tx.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dateStr | المرجعي: ${tx.reference}",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = "${if (!isDebe) "+" else ""}${formatAmount(tx.amount)} ${tx.currency}",
                color = if (isDebe) Color(0xFFFF8A80) else Color(0xFF69F0AE),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

fun formatAmount(amt: Double): String {
    return if (amt == amt.toLong().toDouble()) {
        String.format(Locale.US, "%,d", amt.toLong())
    } else {
        String.format(Locale.US, "%,.2f", amt)
    }
}
