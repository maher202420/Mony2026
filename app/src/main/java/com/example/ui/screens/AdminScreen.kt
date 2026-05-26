package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdminConfig
import com.example.data.AuditLog
import com.example.data.UserAccount
import com.example.ui.WamViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: WamViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val allDbUsers by viewModel.allUsers.collectAsState(initial = emptyList())
    val adminConfig by viewModel.adminConfig.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState(initial = emptyList())

    var activeTab by remember { mutableStateOf("config") } // "config", "users", "audits"

    val primaryColor = Color(0xFFFFD700) // Gold
    val secondaryColor = Color(0xFF00D4FF) // Electric Blue
    val darkBgColor = Color(0xFF0A0E17)
    val cardColor = Color(0xFF131722)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "لوحة تحكم الإدارة العليا (WAM Admin)",
                    color = primaryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إشراف المالك: الأستاذ ماهر أحمد الوتاري", // Corrected T6
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        // Administrative Category Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminTabButton(
                title = "النظام العام",
                isActive = activeTab == "config",
                onClick = { activeTab = "config" },
                modifier = Modifier.weight(1f),
                primaryColor = primaryColor
            )
            AdminTabButton(
                title = "الحسابات النشطة",
                isActive = activeTab == "users",
                onClick = { activeTab = "users" },
                modifier = Modifier.weight(1f),
                primaryColor = primaryColor
            )
            AdminTabButton(
                title = "سجل الأمان",
                isActive = activeTab == "audits",
                onClick = { activeTab = "audits" },
                modifier = Modifier.weight(1f),
                primaryColor = primaryColor
            )
        }

        // Content Board depends on selected tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            when (activeTab) {
                "config" -> {
                    adminConfig?.let { currentConf ->
                        AdminConfigPanel(currentConf = currentConf, viewModel = viewModel, primaryColor = primaryColor, secondaryColor = secondaryColor)
                    }
                }
                "users" -> {
                    AdminUsersPanel(users = allDbUsers, viewModel = viewModel, primaryColor = primaryColor, secondaryColor = secondaryColor)
                }
                "audits" -> {
                    AdminLogsPanel(logs = auditLogs, viewModel = viewModel, primaryColor = primaryColor)
                }
            }
        }
    }
}

@Composable
fun AdminTabButton(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) primaryColor else Color.White.copy(alpha = 0.03f)
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            color = if (isActive) Color.Black else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 1. Config edit tab-pane
@Composable
fun AdminConfigPanel(
    currentConf: AdminConfig,
    viewModel: WamViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    var feeStr by remember { mutableStateOf(currentConf.p2pFeePercent.toString()) }
    var welcomeStr by remember { mutableStateOf(currentConf.customWelcomeMessage) }
    var partnersStr by remember { mutableStateOf(currentConf.partnerCompanies) }
    var cryptoEnabled by remember { mutableStateOf(currentConf.isCryptoEnabled) }
    var systemFrozen by remember { mutableStateOf(currentConf.isSystemFrozen) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("تعديل تفضيلات بروتوكول WAM المالي:", color = primaryColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

        // Text fields modifying preferences
        OutlinedTextField(
            value = welcomeStr,
            onValueChange = { welcomeStr = it },
            label = { Text("رسالة الترحيب المخصصة") },
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = partnersStr,
            onValueChange = { partnersStr = it },
            label = { Text("الشركاء والشبكات المصرفية المعتمدة (مفصولة بفاصلة)") },
            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = feeStr,
            onValueChange = { feeStr = it },
            label = { Text("عمولة التحويل P2P المالي (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Switch custom settings
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("تأمين المعاملات والسيولة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("إخضاع العمليات للتقييم الأمني المتطور", color = Color.Gray, fontSize = 9.sp)
            }
            Switch(
                checked = cryptoEnabled,
                onCheckedChange = { cryptoEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("تجميد العمليات المصرفية مؤقتاً", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("يقيد سحب وإيداع الحوالات في حال الطوارئ", color = Color.Gray, fontSize = 9.sp)
            }
            Switch(
                checked = systemFrozen,
                onCheckedChange = { systemFrozen = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Red)
            )
        }

        Button(
            onClick = {
                val feeVal = feeStr.toDoubleOrNull() ?: currentConf.p2pFeePercent
                val updated = currentConf.copy(
                    customWelcomeMessage = welcomeStr,
                    partnerCompanies = partnersStr,
                    p2pFeePercent = feeVal,
                    isCryptoEnabled = cryptoEnabled,
                    isSystemFrozen = systemFrozen
                )
                viewModel.updateAdminConfig(updated)
                Toast.makeText(context, "تم تطبيق تعديلات النظام لـ WAM فوراً بنجاح!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ التحديثات المصرفية", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

// 2. Active User Accounts board
@Composable
fun AdminUsersPanel(
    users: List<UserAccount>,
    viewModel: WamViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "إشراف وتجميد حسابات العملاء (${users.size} حساب):",
            color = primaryColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(users) { account ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = account.fullName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "هاتف: ${account.phoneNumber}", color = Color.Gray, fontSize = 9.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "${String.format("%,.0f", account.balanceYer)} ر.ي", color = primaryColor, fontSize = 9.sp)
                                Text(text = "${String.format("%,.1f", account.balanceUsd)} $", color = secondaryColor, fontSize = 9.sp)
                            }
                        }

                        // Status action button
                        Button(
                            onClick = {
                                viewModel.freezeUserAccount(account.id, !account.isFrozen)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (account.isFrozen) Color(0xFF00E676) else Color(0xFFFF3B30)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (account.isFrozen) "تنشيط الحساب" else "تجميد الحساب",
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. System secure logs board
@Composable
fun AdminLogsPanel(
    logs: List<AuditLog>,
    viewModel: WamViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("سجلات التدقيق الأمني والعمليات:", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = {
                    viewModel.clearAuditLogs()
                    Toast.makeText(context, "تم تصفير السجل المصرفي بنجاح", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Wipe", tint = Color.Red, modifier = Modifier.size(18.dp))
            }
        }

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("السجل فارغ تماماً.", color = Color.Gray, fontSize = 11.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (log.isSuccess) Color.Green else Color.Red)
                        )
                        Column {
                            Text(
                                text = "[${log.user}] ${log.action}",
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                            Text(
                                text = sdf.format(Date(log.timestamp)),
                                color = Color.Gray,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
