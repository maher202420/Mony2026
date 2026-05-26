package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: WamViewModel,
    onOnboardingComplete: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoginMode by remember { mutableStateOf(true) } // Mode switcher: login vs signup
    
    // Form States
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isOperating by remember { mutableStateOf(false) }

    // Secret elements
    var secretHeaderClickCount by remember { mutableStateOf(0) }
    var showAdminCredentialsDialog by remember { mutableStateOf(false) }

    // Colors
    val primaryColor = Color(0xFFFFD700) // Gold
    val secondaryColor = Color(0xFF00D4FF) // Electric Blue
    val darkBgColor = Color(0xFF0A0E17)
    val cardColor = Color(0xFF131722)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Secret Entrance Header (Taps 5 times on WAM or logo triggers Admin Dialog)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clickable {
                        secretHeaderClickCount++
                        if (secretHeaderClickCount >= 5) {
                            secretHeaderClickCount = 0
                            showAdminCredentialsDialog = true
                        } else if (secretHeaderClickCount > 1) {
                            Toast.makeText(context, "باقي ${5 - secretHeaderClickCount} نقرات للولوج السري للإدارة!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(secondaryColor, primaryColor))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WAM",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "الماهر موني • عالم المال الذكي والآمن", // Corrected spelling T4
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "بروتوكول تحويل متطور تحت إشراف الأستاذ ماهر أحمد الوتاري", // Corrected T6
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Credential Authorization Board
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isLoginMode) "تسجيل الدخول للمحفظة الاستثمارية" else "إنشاء حساب جديد في WAM",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Text fields depends on Mode
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("الاسم الكامل ثلاثي") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_fullname"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("رقم الهاتف الفعال (من 9 أرقام)") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = secondaryColor) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور المشفرة") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_password"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action buttons
                    Button(
                        onClick = {
                            if (phoneNumber.length < 9) {
                                Toast.makeText(context, "الرجاء إدخال رقم هاتف صحيح يتكون من 9 أرقام على الأقل.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (password.isEmpty()) {
                                Toast.makeText(context, "الرجاء تعيين كلمة المرور للاستمرار", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isOperating = true
                            coroutineScope.launch {
                                if (isLoginMode) {
                                    // REQ 2: Log in verified strictly against local Room DB config
                                    val err = viewModel.loginUser(phoneNumber, password)
                                    isOperating = false
                                    if (err != null) {
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "أهلاً بك مجدداً في الجيل المالي الآمن!", Toast.LENGTH_SHORT).show()
                                        onOnboardingComplete()
                                    }
                                } else {
                                    if (fullName.isBlank()) {
                                        Toast.makeText(context, "يرجى كتابة اسمك الثلاثي لإنشاء الحساب", Toast.LENGTH_SHORT).show()
                                        isOperating = false
                                        return@launch
                                    }
                                    
                                    // REQ 2: Register verified with phone uniqueness and 8-char password length
                                    val err = viewModel.registerNewUser(fullName, phoneNumber, password)
                                    isOperating = false
                                    if (err != null) {
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "تم تسجيل حسابك بنجاح! الرجاء تسجيل الدخول الآن.", Toast.LENGTH_LONG).show()
                                        isLoginMode = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_action_btn"),
                        enabled = !isOperating
                    ) {
                        Text(
                            text = if (isOperating) "جاري التحقق والتشفير المالي..." else if (isLoginMode) "دخول آمن للمحفظة" else "إنشاء وتسجيل الحساب",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Mode switch toggle link
                    TextButton(
                        onClick = { isLoginMode = !isLoginMode },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isLoginMode) "ليس لديك حساب؟ سجل حساباً جديداً بنقرة واحدة" else "لديك حساب بالفعل؟ قم بتسجيل الدخول الفوري هنا",
                            color = secondaryColor,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- HIDDEN ADMIN GATEWAY POPUP (T1) ---
        if (showAdminCredentialsDialog) {
            var adminUser by remember { mutableStateOf("") }
            var adminPass by remember { mutableStateOf("") }
            var isVerifyingAdmin by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAdminCredentialsDialog = false },
                title = {
                    Text(
                        text = "بوابة الإشراف العليا المشفرة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
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
                            text = "للدخول إلى لوحة المالك، يرجى تقديم اسم المستخدم الإداري ورمز الأمان ولقطات المرور الخاصة بـ WAM:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = adminUser,
                            onValueChange = { adminUser = it },
                            label = { Text("اسم مستخدم لوحة التحكم") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        OutlinedTextField(
                            value = adminPass,
                            onValueChange = { adminPass = it },
                            label = { Text("كلمة مرور المشرف") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isVerifyingAdmin = true
                            // Correct Credential Matching as per T1:
                            // User: WAM2026
                            // Pass: maher--736462
                            if (adminUser == "WAM2026" && adminPass == "maher--736462") {
                                isVerifyingAdmin = false
                                showAdminCredentialsDialog = false
                                Toast.makeText(context, "تم تخويل المشرف بنجاح! أهلاً بك الأستاذ ماهر الوتاري.", Toast.LENGTH_SHORT).show()
                                onNavigateToAdmin()
                            } else {
                                isVerifyingAdmin = false
                                Toast.makeText(context, "عذراً، رموز المرور الإدارية غير صحيحة. تم رصد الواقعة أمنياً.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isVerifyingAdmin
                    ) {
                        Text("دخول سرّي للإشراف", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminCredentialsDialog = false }) {
                        Text("تراجع", color = Color.Gray)
                    }
                },
                containerColor = cardColor
            )
        }
    }
}
