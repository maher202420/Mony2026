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
import androidx.compose.ui.window.Dialog
import com.example.data.SavingPot
import com.example.ui.WamViewModel

@Composable
fun SavingsScreen(
    viewModel: WamViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    val savingPots by viewModel.allSavingPots.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var seedPotId by remember { mutableStateOf<Int?>(null) }
    var seedAmountStr by remember { mutableStateOf("") }

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
            // Header / Intro
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "محفظة الاستثمار الآمن WAM",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "الأوعية الادخارية المبتكرة",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Sharia-Compliance Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x1A00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), MaterialTheme.shapes.medium)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = "توافق شرعي", tint = Color(0xFF00E676))
                        Column {
                            Text(
                                text = "ادخار ذكي بصفر فوائد ربوية",
                                color = Color(0xFF00E676),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الأوعية الادخارية في WAM تدشين للمستقبل وتوفير آمن وحر بنسبة 100% متوافق كلياً مع الشريعة الإسلامية.",
                                color = Color(0xFFE0E0E0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }

            // Add new pot quick button
            item {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "أضف وعاء", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "افتح وعاء ادخاري ذكي جديد", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // List of Saving Pots
            if (savingPots.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "لا يوجد أوعية ادخارية دقيقة",
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا تملك أوعية ادخار نشطة حالياً. خطط لمستقبلك المالي وأنشئ وعائك الأول الآن لتبدأ التوفير السهل!",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(savingPots) { pot ->
                    SavingPotCard(
                        pot = pot,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onAddFunds = { seedPotId = pot.id },
                        onDelete = { viewModel.deleteSavingPot(pot.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Dialog: Create new Saving Pot
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var targetStr by remember { mutableStateOf("") }
        var currency by remember { mutableStateOf("USD") }

        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, primaryColor, MaterialTheme.shapes.large)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "إنشاء وعاء ادخاري ذكي WAM",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("اسم الوعاء (مثال: سداد الرسوم الجامعية)", color = Color.Gray) },
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
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("المبلغ المستهدف ادخاره", color = Color.Gray) },
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
                                selected = currency == "YER",
                                onClick = { currency = "YER" },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Text("YER ريال يمني", color = Color.White, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currency == "USD",
                                onClick = { currency = "USD" },
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
                            onClick = { showCreateDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val target = targetStr.toDoubleOrNull()
                                if (title.isBlank() || target == null || target <= 0) {
                                    Toast.makeText(context, "الرجاء تعبئة الحقول بشكل صحيح", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.addSavingPot(title, target, currency)
                                Toast.makeText(context, "تم إعداد الوعاء المطور بنجاح!", Toast.LENGTH_LONG).show()
                                showCreateDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("أنشئ الآن", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Contribution (Fund Pot)
    if (seedPotId != null) {
        Dialog(onDismissRequest = { seedPotId = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, secondaryColor, MaterialTheme.shapes.large)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تغذية وتمويل الوعاء الحالي",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سيتم اقتطاع المبلغ من رصيدك العام في المحفظة ونقله لوعاء التوفير.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = seedAmountStr,
                        onValueChange = { seedAmountStr = it },
                        label = { Text("المبلغ المقتطع للاستثمار في الوعاء", color = Color.Gray) },
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
                            onClick = { seedPotId = null; seedAmountStr = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val valAmt = seedAmountStr.toDoubleOrNull()
                                val potId = seedPotId
                                if (valAmt == null || valAmt <= 0 || potId == null) {
                                    Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.saveToPot(potId, valAmt) { success ->
                                    if (success) {
                                        Toast.makeText(context, "تم تمويل الوعاء بنجاح وتحويل المال!", Toast.LENGTH_LONG).show()
                                        seedPotId = null
                                        seedAmountStr = ""
                                    } else {
                                        Toast.makeText(context, "فشل التغذية: رصيدك العام في المحفظة غير كافٍ", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد التمويل", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingPotCard(
    pot: SavingPot,
    primaryColor: Color,
    secondaryColor: Color,
    onAddFunds: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (pot.targetAmount > 0) (pot.currentAmount / pot.targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val progressPercent = (progress * 100).toInt()

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF2E2E3E), MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = "حصالة", tint = primaryColor)
                    }
                    Text(
                        text = pot.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف الوعاء", tint = Color.LightGray)
                }
            }

            // Progress statement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المدخر حالياً: ${pot.currentAmount} / ${pot.targetAmount} ${pot.currency}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "$progressPercent%",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                color = primaryColor,
                trackColor = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            // CTA
            Button(
                onClick = onAddFunds,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "توفير", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("غذِّ الوعاء بوفر مالي الآن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
