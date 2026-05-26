package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserAccount
import com.example.ui.WamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: WamViewModel,
    currentUser: UserAccount
) {
    val context = LocalContext.current
    var loanAmount by remember { mutableStateOf("") }
    var loanPeriodMonths by remember { mutableStateOf("6") }
    var isSubmitting by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFFFD700) // Gold
    val secondaryColor = Color(0xFF00D4FF) // Electric Blue
    val darkBgColor = Color(0xFF0A0E17)
    val cardColor = Color(0xFF131722)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Banner Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(secondaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = secondaryColor)
            }
            Column {
                Text(
                    text = "التمويل الأصغر الذكي المتكامل",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "قروض ميسرة وآمنة بدون ضمانات معقدة بدعم المالك ماهر أحمد الوتاري.", // Corrected Owner Name T6
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        // Loan Application Form card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "نموذج طلب التمويل الذكي",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Description of underwriting engine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(secondaryColor.copy(alpha = 0.05f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "يقوم محرك الذكاء الاصطناعي لـ WAM بتحليل سلوك المعاملات والتحقق من الهاتف. الفائدة: 0% تماماً. السداد بمرونة فائقة كلياً عبر الخصم من التحويلات أو الإيداعات القادمة.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }

                OutlinedTextField(
                    value = loanAmount,
                    onValueChange = { loanAmount = it },
                    label = { Text("مبلغ التمويل المطلوب (بالريال اليمني)") }, // Corrected spelling T4
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = secondaryColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Text("فترة سداد القرض المرنة:", color = Color.White, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf("3", "6", "12", "24")
                    periods.forEach { currentM ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (loanPeriodMonths == currentM) secondaryColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                                .border(1.dp, if (loanPeriodMonths == currentM) secondaryColor else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { loanPeriodMonths = currentM }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$currentM أشهر",
                                color = if (loanPeriodMonths == currentM) secondaryColor else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val amountVal = loanAmount.toDoubleOrNull()
                        if (amountVal == null || amountVal <= 0) {
                            Toast.makeText(context, "الرجاء تحديد قيمة تمويل صالحة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amountVal > 1500000.0) {
                            Toast.makeText(context, "الحد الأقصى للتمويل الأصغر الفوري هو 1,500,000 ريال يمني للأمان.", Toast.LENGTH_LONG).show() // Corrected T4
                            return@Button
                        }

                        isSubmitting = true
                        viewModel.performMicroLoan(amountVal, loanPeriodMonths.toInt()) { success, msg ->
                            isSubmitting = false
                            if (success) {
                                Toast.makeText(context, "تهانينا! وافق الذكاء الاصطناعي لـ WAM على طلبك وتم إيداع مبلغ القرض برصيدك فوراً لجيل المال الذكي والآمن!", Toast.LENGTH_LONG).show() // Corrected T4
                                loanAmount = ""
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = if (isSubmitting) "جاري دراسة الأهلية المصرفية بنظام WAM..." else "إرسال طلب التمويل للتقييم الفوري",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Summary facts
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Text("مميزات وثقة نظام WAM الإسلامي:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text("• نسبة الفائدة: 0.0% تماماً وبدون رسوم خفية.", color = Color.LightGray, fontSize = 10.sp)
                Text("• الضمانات: غير مطلوبة كلاسيكياً؛ نعتمد على هوية التطبيق والتقييم الائتماني الذكي.", color = Color.LightGray, fontSize = 10.sp)
                Text("• الإشراف المباشر: الأستاذ ماهر أحمد الوتاري (المصمم والمالك المعتمد).", color = Color.LightGray, fontSize = 10.sp) // Corrected Owner Name T6
            }
        }
    }
}
