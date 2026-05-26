package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.WamViewModel

@Composable
fun FinanceScreen(
    viewModel: WamViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    val adminConfig by viewModel.adminConfig.collectAsState()
    val mainUser by viewModel.mainUser.collectAsState()

    var showLoanDialog by remember { mutableStateOf(false) }
    var selectedLoanAmount by remember { mutableStateOf(20000.0) }
    var selectedLoadCurrency by remember { mutableStateOf("YER") }
    var isProcessingLoan by remember { mutableStateOf(false) }

    val cryptoEnabled = adminConfig?.isCryptoEnabled == true

    // Fake historic crypto values to draw beautiful sparkline graphs
    val btcPoints = listOf(92.0f, 91.5f, 94.2f, 93.8f, 95.0f, 94.6f, 96.8f, 96.2f, 98.4f, 101.2f)
    val ethPoints = listOf(3550f, 3480f, 3520f, 3600f, 3590f, 3640f, 3620f, 3680f, 3750f, 3710f)

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
            // Screen Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "الخدمات المالية المتكاملة WAM",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "الاستثمار بالقروض والعملات الرقمية",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Micro-Loans (قروض صغيرة فورية) Section Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, primaryColor.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "قروض", tint = primaryColor)
                                }
                                Text(
                                    text = "التمويلات والقروض الصغيرة الفورية",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "موافقة فورية",
                                    color = Color(0xFF00E676),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "بناءً على تفاعلك وسلوك معاملاتك المالية السابقة في WAM، يمكنك الآن سحب قرض حسن فوري بضغطة زر وبدون فائدة تماماً لدعم مشاريعك التجارية الصغيرة.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )

                        Button(
                            onClick = { showLoanDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Handshake, contentDescription = "طلب تمويل", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اطلب تمويلاً حسناً فورياً WAM", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Cryptocurrency Segment Configuration check
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "منصة تداول العملات الرقمية WAM Crypto",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (cryptoEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("نشط وموثق", color = Color(0xFF00E676), fontSize = 9.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("معطل برمجيا", color = Color.Red, fontSize = 9.sp)
                        }
                    }
                }
            }

            if (!cryptoEnabled) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = "تداول مقفل", tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Text(
                                text = "خدمات العملات الرقمية معطلة حالياً",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "قام مدير النظام أو المالك للأستاذ ماهر أحمد الوتاري بتعطيل تداول البيتكوين والإيثريوم مؤقتاً في الأقاليم بانتظام التراخيص التشريعية. يمكنك مراجعتهم لتفعيله.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Crypto active rendering
                item {
                    CryptoRateCard(
                        symbol = "BTC",
                        name = "Bitcoin",
                        price = "98,154.20 USD",
                        change = "+4.52%",
                        changeColor = Color(0xFF00E676),
                        points = btcPoints,
                        primaryColor = primaryColor
                    )
                }

                item {
                    CryptoRateCard(
                        symbol = "ETH",
                        name = "Ethereum",
                        price = "3,712.50 USD",
                        change = "-1.24%",
                        changeColor = Color(0xFFFF3B30),
                        points = ethPoints,
                        primaryColor = secondaryColor
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // --- DIALOGS ---

    // Micro-Loan Request Dialog
    if (showLoanDialog) {
        Dialog(onDismissRequest = { showLoanDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "تأكيد طلب التمويل الأصغر الفوري",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يقوم محرك الذكاء الاصطناعي لـ WAM بتحليل البيانات والتحقق من الهاتف. الفائدة: 0% تماماً. السداد بمرونة عبر عمليات الإيداع القادمة.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    // Select loan amount
                    Text("اختر قيمة القرض الحسن المطلوب:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(20000.0, 50000.0, 100000.0).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(if (selectedLoanAmount == amt && selectedLoadCurrency == "YER") primaryColor else Color.DarkGray)
                                    .clickable {
                                        selectedLoanAmount = amt
                                        selectedLoadCurrency = "YER"
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${amt.toInt()} YER", color = if(selectedLoanAmount == amt && selectedLoadCurrency == "YER") Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50.0, 100.0, 200.0).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(if (selectedLoanAmount == amt && selectedLoadCurrency == "USD") secondaryColor else Color.DarkGray)
                                    .clickable {
                                        selectedLoanAmount = amt
                                        selectedLoadCurrency = "USD"
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$$amt USD", color = if(selectedLoanAmount == amt && selectedLoadCurrency == "USD") Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showLoanDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }

                        Button(
                            onClick = {
                                isProcessingLoan = true
                                viewModel.microLoan(selectedLoanAmount, selectedLoadCurrency) { success ->
                                    isProcessingLoan = false
                                    if (success) {
                                        Toast.makeText(context, "تهانينا! وافق الذكاء الاصطناعي لـ WAM على طلبك وتم إيداع المبلغ برصيدك فوراُ!", Toast.LENGTH_LONG).show()
                                        showLoanDialog = false
                                    } else {
                                        Toast.makeText(context, "فشل الإجراء: حسابك مجمد أو لا يلبي شروط الفحص", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("اسحب القرض فورا", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoRateCard(
    symbol: String,
    name: String,
    price: String,
    change: String,
    changeColor: Color,
    points: List<Float>,
    primaryColor: Color
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF2E2E3E), MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(symbol.uppercase().take(1), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(price, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text(change, color = changeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Beautiful Canvas Sparkline Graph
            Canvas(
                modifier = Modifier
                    .width(100.dp)
                    .height(45.dp)
                    .padding(4.dp)
            ) {
                val minVal = points.minOrNull() ?: 0f
                val maxVal = points.maxOrNull() ?: 1f
                val valRange = maxVal - minVal

                val stepX = size.width / (points.size - 1)
                val path = Path()

                points.forEachIndexed { index, value ->
                    val normY = if (valRange > 0) (value - minVal) / valRange else 0.5f
                    val y = size.height - (normY * size.height)
                    val x = index * stepX

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = changeColor,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
