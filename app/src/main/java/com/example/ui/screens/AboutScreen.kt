package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel

@Composable
fun AboutScreen(
    viewModel: WamViewModel,
    onNavigateBack: () -> Unit,
    onTriggerAdminGate: () -> Unit
) {
    val context = LocalContext.current
    var secretClicksCount by remember { mutableStateOf(0) }

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
        Spacer(modifier = Modifier.height(10.dp))

        // Large WAM logo representation with secret tap handler
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(secondaryColor, primaryColor)))
                .clickable {
                    secretClicksCount++
                    if (secretClicksCount >= 5) {
                        secretClicksCount = 0
                        onTriggerAdminGate()
                    } else if (secretClicksCount > 1) {
                        Toast.makeText(context, "باقي ${5 - secretClicksCount} حركات لفتح اللوحة السرية!", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WAM",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            text = "الماهر موني Al-Maher Money",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "الجيل التالي من المدفوعات والتمويل الأصغر الذكي المتكامل",
            color = primaryColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        // Details Container (spelling corrections applied: ريال يمني, الآمن, name: ماهر أحمد الوتاري)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "معلومات النظام القانونية والمالية:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Divider(color = Color.White.copy(alpha = 0.05f))

                InfoItem(
                    label = "التأصيل والترخيص:",
                    value = "بناء الآمن والذكي 100% متوافق مع المائرة الفدرالية للشريعة والتحويل السريع."
                )

                InfoItem(
                    label = "المصمم والمالك المعتمد:",
                    value = "الأستاذ ماهر أحمد الوتاري" // Corrected Owner Name T6
                )

                // Corrected spellings in description (الآمن, ريال يمني)
                InfoItem(
                    label = "هدف التطبيق والمحفظة:",
                    value = "تقديم جيل ريادي آمن وخالي من شبهات الفوائد (0%) لدعم المواطنين في المعاملات المالية، دفع فواتير الخدمات، التمويل الأصغر، والأوعية الادخارية المبتكرة بالريال اليمني والدولار." 
                )

                InfoItem(
                    label = "قوة التشفير المالي:",
                    value = "البروتوكول الآمن للتطبيق يحمي كل حركة مالية ويمنع الاختراقات بنظام تشفير يمني-خليجي متطور وجاهز للاختبار." 
                )
            }
        }

        // Contact technical support card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = "للتواصل والدعم الفني المباشر:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Support phone click triggers secret gate optionally
                Text(
                    text = "777644670",
                    color = primaryColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clickable {
                            secretClicksCount++
                            if (secretClicksCount >= 5) {
                                secretClicksCount = 0
                                onTriggerAdminGate()
                            }
                        }
                        .padding(4.dp)
                )

                Text(
                    text = "الأستاذ ماهر أحمد الوتاري متاح لمساعدتكم والإشراف على كافة الحوالات والعملاء في اليمن والخليج على مدار الساعة.", // Corrected name T6
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }

        // Back home link
        TextButton(onClick = onNavigateBack) {
            Text("العودة للرئيسية", color = secondaryColor, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = Color(0xFF00D4FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color.LightGray,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
