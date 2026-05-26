package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
fun SavingsScreen(
    viewModel: WamViewModel,
    currentUser: UserAccount
) {
    val context = LocalContext.current
    val savingPotBalance by viewModel.savingPotBalanceYer.collectAsState()

    var depositAmount by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFFFD700) // Gold
    val secondaryColor = Color(0xFF00D4FF) // Electric Blue
    val darkBgColor = Color(0xFF0A0E17)
    val cardColor = Color(0xFF131722)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = primaryColor)
            }
            Column {
                Text(
                    text = "الأوعية الادخارية المبتكرة (Saving Pots)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ادخر بمرونة وأمان بفوائد صفرية متوافقة مع الشريعة الإسلامية.",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        // Beautiful glassmorphic balance display
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "رصيد الوعاء الادخاري النشط",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format("%,.0f", savingPotBalance),
                        color = primaryColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = " ريال يمني", // Corrected spelling T4
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("معدل العائد", color = Color.Gray, fontSize = 10.sp)
                        Text("0% تماماً", color = secondaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("حالة شرعية", color = Color.Gray, fontSize = 10.sp)
                        Text("متوافق 100%", color = Color(0xFF00E676), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المالك المشرف", color = Color.Gray, fontSize = 10.sp)
                        Text("ماهر الوتاري", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold) // Corrected owner title T6
                    }
                }
            }
        }

        // Add funds card formulation
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "اشحن الوعاء من حسابك الجاري",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    label = { Text("المبلغ (بالريال اليمني)") }, // Corrected T4
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Button(
                    onClick = {
                        val amountVal = depositAmount.toDoubleOrNull()
                        if (amountVal == null || amountVal <= 0) {
                            Toast.makeText(context, "الرجاء تحديد مبلغ الادخار بدقة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        viewModel.submitSavingPotDeposit(amountVal) { success, msg ->
                            isSaving = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                depositAmount = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) "جاري الاستثمار الشرعي لـ WAM..." else "إيداع فوري في الوعاء الادخاري",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Info Alerts Container
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "الأوعية الادخارية المبتكرة مصممة لحفظ قيمة مدخراتك دون شبهة الفوائد الربوية. رصيدك الادخاري مضمون بالكامل ويمكنك سحبه في أي لحظة للرصيد الفعال دون شروط جزئية.",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}
