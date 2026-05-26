package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

enum class OnboardingStep {
    SIGN_UP,
    KYC,
    LOGIN
}

@Composable
fun OnboardingScreen(
    primaryColor: Color,
    secondaryColor: Color,
    onOnboardingComplete: (fullName: String, phone: String) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(OnboardingStep.SIGN_UP) }

    // User data saved during Sign Up
    var registeredName by remember { mutableStateOf("") }
    var registeredPhone by remember { mutableStateOf("") }
    var registeredPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = currentStep, label = "OnboardingTransition") { step ->
            when (step) {
                OnboardingStep.SIGN_UP -> {
                    SignUpSection(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onSignUpSuccess = { name, phone, pass ->
                            registeredName = name
                            registeredPhone = phone
                            registeredPassword = pass
                            currentStep = OnboardingStep.KYC
                        },
                        onNavigateToLogin = {
                            currentStep = OnboardingStep.LOGIN
                        }
                    )
                }
                OnboardingStep.KYC -> {
                    KycSection(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fullName = registeredName,
                        onKycComplete = {
                            currentStep = OnboardingStep.LOGIN
                        }
                    )
                }
                OnboardingStep.LOGIN -> {
                    LoginSection(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        registeredPhone = registeredPhone,
                        registeredPassword = registeredPassword,
                        onLoginSuccess = { phone ->
                            // Use registered name or fallback to owner's name if it matches the registered state
                            val nameToUse = if (phone == registeredPhone && registeredName.isNotBlank()) registeredName else "ماهر أحمد الوتاري"
                            onOnboardingComplete(nameToUse, phone)
                        },
                        onNavigateToSignUp = {
                            currentStep = OnboardingStep.SIGN_UP
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpSection(
    primaryColor: Color,
    secondaryColor: Color,
    onSignUpSuccess: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.radialGradient(listOf(secondaryColor, primaryColor))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WAM",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "إنشاء حساب جديد كلياً",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("signup_title")
        )

        Text(
            text = "مرحباً بك في عالم المال الذكي المتطور",
            color = Color(0xFFE0E0E0),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("الاسم الكامل ثلاثياً", color = Color.Gray) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
        )

        // Input Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الهاتف (مثل: 777644670)", color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = primaryColor) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("signup_phone_input")
        )

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("إنشاء كلمة المرور", color = Color.Gray) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
        )

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("تأكيد كلمة المرور", color = Color.Gray) },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = primaryColor) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Create Account button
        Button(
            onClick = {
                if (name.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "الرجاء تعبئة كافة الحقول بدقة", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, "تنبيه: كلمتا المرور غير متطابقتين!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onSignUpSuccess(name, phone, password)
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("signup_submit_button")
        ) {
            Text(
                text = "إنشاء حساب WAM",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Link to login screen
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "لديك حساب بالفعل؟ ", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = "تسجيل الدخول",
                color = secondaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun KycSection(
    primaryColor: Color,
    secondaryColor: Color,
    fullName: String,
    onKycComplete: () -> Unit
) {
    val context = LocalContext.current
    var idUploaded by remember { mutableStateOf(false) }
    var selfieUploaded by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "التحقق من الهوية (KYC مبسط)",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "يرجى رفع الوثائق المطلوبة لاعتماد حسابك وتفعيله الفوري",
            color = Color(0xFFE0E0E0),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Upload National ID
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (idUploaded) Color(0xFF00E676) else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { idUploaded = true },
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (idUploaded) Color(0x2200E676) else Color(0x11FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (idUploaded) Icons.Default.CheckCircle else Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (idUploaded) Color(0xFF00E676) else primaryColor
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "رفع صورة الهوية الشخصية",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (idUploaded) "تم اختيار بطاقة الهوية الذكية ✓" else "يرجى النقر لالتقاط/رفع جواز أو بطاقة شخصية",
                        color = if (idUploaded) Color(0xFF00E676) else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Upload Selfie
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (selfieUploaded) Color(0xFF00E676) else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { selfieUploaded = true },
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selfieUploaded) Color(0x2200E676) else Color(0x11FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selfieUploaded) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = if (selfieUploaded) Color(0xFF00E676) else primaryColor
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "رفع صورة شخصية (Selfie)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selfieUploaded) "تم التقاط سيلفي الوضوح العالي ✓" else "يرجى التقاط صورة سيلفي مباشرة لمطابقتها",
                        color = if (selfieUploaded) Color(0xFF00E676) else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isVerifying) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = secondaryColor)
                Text(
                    text = "جاري التحقق من بياناتك...",
                    color = secondaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LaunchedEffect(Unit) {
                delay(2200)
                onKycComplete()
            }
        } else {
            Button(
                onClick = {
                    if (!idUploaded || !selfieUploaded) {
                        Toast.makeText(context, "الرجاء رفع صورة الهوية الشخصية وصورتك الشخصية للاستمرار", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isVerifying = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("kyc_submit_button")
            ) {
                Text(
                    text = "إرسال للتحقق المالي",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSection(
    primaryColor: Color,
    secondaryColor: Color,
    registeredPhone: String,
    registeredPassword: String,
    onLoginSuccess: (phone: String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    var usernameOrPhone by remember { mutableStateOf(registeredPhone) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand Symbol
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.radialGradient(listOf(secondaryColor, primaryColor))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WAM",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "تسجيل الدخول إلى المحفظة",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("login_title")
        )

        Text(
            text = "أهلاً بك مجدداً في جيل المال المشفر والأمن المطلق",
            color = Color(0xFFE0E0E0),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Phone/Email input
        OutlinedTextField(
            value = usernameOrPhone,
            onValueChange = { usernameOrPhone = it },
            label = { Text("رقم الهاتف أو البريد الإلكتروني", color = Color.Gray) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = primaryColor) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("login_username_input")
        )

        // Password input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور", color = Color.Gray) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
        )

        // "Forgot Password" link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "نسيت كلمة المرور؟",
                color = primaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        Toast.makeText(context, "الرجاء التواصل مع الدعم الفني 777644670 لاستعادة كلمة المرور", Toast.LENGTH_LONG).show()
                    }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit Login button
        Button(
            onClick = {
                if (usernameOrPhone.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "الرجاء كمال حقول تسجيل الدخول", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // Permit local login bypassing with default test credentials as well
                if (registeredPhone.isNotBlank() && usernameOrPhone == registeredPhone) {
                    if (password != registeredPassword) {
                        Toast.makeText(context, "تنبيه: كلمة المرور خاطئة!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                }
                onLoginSuccess(usernameOrPhone)
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_submit_button")
        ) {
            Text(
                text = "دخول إلى المحفظة",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // SignUp navigation link
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ليس لديك حساب؟ ", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = "إنشاء حساب جديد",
                color = secondaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToSignUp() }
                    .padding(4.dp)
            )
        }
    }
}
