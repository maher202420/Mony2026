package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel

@Composable
fun AboutScreen(
    viewModel: WamViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.adminConfig.collectAsState()

    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var showSecretFields by remember { mutableStateOf(false) }

    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }

    viewModel.checkLockout() // Verify locks state
    val lockoutRemaining = viewModel.lockoutTimeRemainingMinutes
    val isLoggedIn = viewModel.isAdminLoggedIn

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "محفظة الإلكترونية لجيل الريادة",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "عن تطبيق WAM المطور",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Visual Logo Branding Card
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(secondaryColor, primaryColor)))
                        .border(1.5.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "W",
                        color = Color.Black,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // App name / details
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = config?.appName ?: "الماهر موني (Al-Maher Money)",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الاسم الداخلي المعتمد: WAM (Wallet Authorized Maher)",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "النسخة المستقرة الأولى: Version 1.0.0 (Build 2026)",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Promotion card container containing click detector (The Trigger!)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 1200) {
                                clickCount++
                            } else {
                                clickCount = 1
                            }
                            lastClickTime = now

                            if (clickCount >= 5) {
                                if (lockoutRemaining > 0) {
                                    Toast
                                        .makeText(
                                            context,
                                            "النظام مقفل احترازياً! يرجى الانتظار $lockoutRemaining دقيقة.",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                } else {
                                    showSecretFields = true
                                    Toast
                                        .makeText(
                                            context,
                                            "🔓 تم العثور على البوابة المشفرة السرية بنجاح!",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                                clickCount = 0
                            }
                        }
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "رمز الدعاية الرسمي والموثق",
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(Color.Black)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "777644670",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        }
                        Text(
                            text = "انقر على رمز الدعاية لمزيد من المعلومات والترقيات المتاحة للمشاركين.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Secret Door Gateway UI (If triggered or logged in)
            item {
                AnimatedVisibility(
                    visible = showSecretFields || isLoggedIn,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, primaryColor, MaterialTheme.shapes.medium)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isLoggedIn) {
                                // Admin is logged in. Show short details and direct access button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = "تسجيل دخول مشرف", tint = Color(0xFF00E676))
                                    Text(
                                        text = "مرحباً بك، المشرف العام للأستاذ ماهر عادل العقبي",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = onNavigateToAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "لوحة التحكم", tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("الدخول إلى لوحة التحكم المشفرة", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.logoutAdmin() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("تسجيل خروج المشرف", color = Color.White)
                                }
                            } else {
                                // Login Form
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = "لوحة تحكم مشفرة", tint = primaryColor)
                                    Text(
                                        text = "الموقع السري لتسجيل دخول المالك المعتمد",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (lockoutRemaining > 0) {
                                    Text(
                                        text = "النظام مقفل حالياً للاحتياط الأمني. يتبقى: $lockoutRemaining دقيقة لتجربة إضافية.",
                                        color = Color(0xFFFF3B30),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = adminUsername,
                                        onValueChange = { adminUsername = it },
                                        label = { Text("معرف المالك (Username)", color = Color.Gray) },
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
                                        value = adminPassword,
                                        onValueChange = { adminPassword = it },
                                        label = { Text("كلمة المرور المشفرة", color = Color.Gray) },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = primaryColor,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (viewModel.failedLoginAttempts > 0) {
                                        Text(
                                            text = "اسم مستخدم أو كلمة مرور خاطئة! محاولات متبقية: ${3 - viewModel.failedLoginAttempts}",
                                            color = Color(0xFFFF8A80),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (adminUsername.isBlank() || adminPassword.isBlank()) {
                                                Toast.makeText(context, "الرجاء كمال حقول الدخول السرية", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val ok = viewModel.attemptAdminLogin(adminUsername, adminPassword)
                                            if (ok) {
                                                Toast.makeText(context, "الدخول مصرح به! مرحباً بك WAM.", Toast.LENGTH_LONG).show()
                                                onNavigateToAdmin()
                                            } else {
                                                Toast.makeText(context, "معلومات دخول خاطئة أو إقفال أمني نشط!", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("فك شفرة الدخول الآمن", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Copyrights Statement
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "حقوق الملكية الفكرية المعتمدة",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "جميع الحقوق محفوظة لصالح الأستاذ ماهر عادل العقبي © 2026.\nصمم التطبيق ليعمل في بيئة مشفرة ومصنفة دولياً تضمن الأمان المالي المطلق وخوارزميات المعاملات المقاضية السريعة.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
