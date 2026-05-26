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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
    viewModel: WamViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    val users by viewModel.allUsers.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val config by viewModel.adminConfig.collectAsState()

    // Config Input Fields (initialized with settings values)
    var editAppName by remember { mutableStateOf(config?.appName ?: "الماهر موني") }
    var editPrimaryHex by remember { mutableStateOf(config?.primaryColorHex ?: "#FFD700") }
    var editSecondaryHex by remember { mutableStateOf(config?.secondaryColorHex ?: "#00D4FF") }
    var editP2pFee by remember { mutableStateOf((config?.p2pFeePercentage ?: 0.5).toString()) }
    var editCryptoEnabled by remember { mutableStateOf(config?.isCryptoEnabled == true) }
    var editSystemFrozen by remember { mutableStateOf(config?.isSystemFrozen == true) }

    // Sync state when config updates
    LaunchedEffect(config) {
        config?.let {
            editAppName = it.appName
            editPrimaryHex = it.primaryColorHex
            editSecondaryHex = it.secondaryColorHex
            editP2pFee = it.p2pFeePercentage.toString()
            editCryptoEnabled = it.isCryptoEnabled
            editSystemFrozen = it.isSystemFrozen
        }
    }

    Scaffold(
        topBar = {
            OptAdminTopBar(
                title = "لوحة المالك (Security WAM)",
                onBack = onNavigateBack,
                toolbarColor = primaryColor
            )
        },
        containerColor = Color(0xFF0A0E17)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Warning
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231414)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "بوابة المالك", tint = Color(0xFFFF5252))
                        Text(
                            text = "تحذير: أنت في واجهة الإدمن الخفية المتكاملة. أي تغيير في القيم ينعكس فوراً وبشكل حقيقي على محرك قاعدة البيانات وسلوك مستخدمي WAM.",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // A. Dynamic Brand & Styling Configuration (تحديث التطبيق)
            item {
                Text(
                    text = "أولاً: إعدادات العلامة التجارية والمظهر والأمان",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editAppName,
                            onValueChange = { editAppName = it },
                            label = { Text("اسم التطبيق المعتمد", color = Color.Gray) },
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editPrimaryHex,
                                onValueChange = { editPrimaryHex = it },
                                label = { Text("اللون رئيسي (Hex)", color = Color.Gray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = editSecondaryHex,
                                onValueChange = { editSecondaryHex = it },
                                label = { Text("لون تفاعلي (Hex)", color = Color.Gray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = secondaryColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = editP2pFee,
                            onValueChange = { editP2pFee = it },
                            label = { Text("رسوم تحويل P2P (%)", color = Color.Gray) },
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

                        // Switches Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تفعيل منصة WAM Crypto للعملات الرقمية", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = editCryptoEnabled,
                                onCheckedChange = { editCryptoEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                            )
                        }

                        Divider(color = Color.DarkGray)

                        // Panic Full System Freeze Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("تجميد وحالة قفل الصيانة الكلية للنظام", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("تجميد مؤقت للتحويلات والمدفوعات فورا", color = Color.Gray, fontSize = 10.sp)
                            }
                            Switch(
                                checked = editSystemFrozen,
                                onCheckedChange = { editSystemFrozen = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFF3B30),
                                    checkedTrackColor = Color(0xFF8C1D1D)
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val fee = editP2pFee.toDoubleOrNull()
                                if (editAppName.isBlank() || fee == null || fee < 0.0) {
                                    Toast.makeText(context, "يرجى التحقق من القيم المدخلة", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.saveAdminThemeSettings(
                                    appName = editAppName,
                                    primaryColor = editPrimaryHex,
                                    secondaryColor = editSecondaryHex,
                                    fee = fee,
                                    isCrypto = editCryptoEnabled,
                                    systemFrozen = editSystemFrozen
                                )
                                Toast.makeText(context, "تم تطبيق التفضيلات وتحديث الهوية البصرية فورياً في القاعدة!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ التفضيلات وتطبيق الهوية الحية", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // B. User Accounts Management List (إدارة حسابات المستخدمين)
            item {
                Text(
                    text = "ثانياً: إدارة وتجميد حسابات المشتركين",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (users.isEmpty()) {
                item {
                    Text("لا يوجد مستخدمون في قاعدة البيانات المحلية.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                items(users) { usr ->
                    UserManagementRow(
                        usr = usr,
                        primaryColor = primaryColor,
                        onFreezeToggle = { isFreeze ->
                            viewModel.setAccountStatus(usr.id, isFreeze)
                            Toast.makeText(context, "تغير حالة الحساب بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // C. Live Audit Logs System (سجلات المراجعة الأمنية)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ثالثاً: سجل التدقيق والمراقبة الأمنية للبلاد",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            viewModel.clearSystemAuditLogs()
                            Toast.makeText(context, "تم تصفير سجل المراقبة وتصويره", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A1D20)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("تصفير السجل", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            if (auditLogs.isEmpty()) {
                item {
                    Text("سجل التدقيق خالي تماماً حالياً.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                items(auditLogs) { log ->
                    AuditLogRowItem(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptAdminTopBar(title: String, onBack: () -> Unit, toolbarColor: Color) {
    TopAppBar(
        title = { Text(title, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = toolbarColor)
    )
}

@Composable
fun UserManagementRow(
    usr: UserAccount,
    primaryColor: Color,
    onFreezeToggle: (Boolean) -> Unit
) {
    val isFrozen = usr.status == "FROZEN"
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF2E2E3E), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = usr.fullName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (usr.isMainUser) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(primaryColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("المالك المصلح", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "رقم الجوال: ${usr.phoneNumber} | رصيد YER: ${formatAmount(usr.balanceYer)} | USD: $${formatAmount(usr.balanceUsd)}",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            Button(
                onClick = { onFreezeToggle(!isFrozen) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFrozen) Color(0xFF00E676) else Color(0xFFFF3B30)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = if (isFrozen) "تنشيط فوري" else "تجميد الحساب",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AuditLogRowItem(log: AuditLog) {
    val dateFmt = SimpleDateFormat("HH:mm:ss (MM-dd)", Locale.getDefault())
    val dateStr = dateFmt.format(Date(log.timestamp))

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF232731), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (log.isSuccess) Color(0xFF00E676) else Color(0xFFFF3B30))
                    )
                    Text(
                        text = "المعرف: ${log.username}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$dateStr | IP: ${log.ipAddress}",
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }

            Text(
                text = log.description,
                color = Color.LightGray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
