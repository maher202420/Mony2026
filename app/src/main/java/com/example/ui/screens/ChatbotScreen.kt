package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(
    viewModel: WamViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color
) {
    val messages = viewModel.chatMessages
    val isLoading = viewModel.isChatLoading
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val presetPrompts = listOf(
        "كيف أحصل على قرض فوري؟",
        "ما هي رسوم التحويل في WAM؟",
        "معلومات عن الأستاذ ماهر العقبي",
        "هل سداد الفواتير آمن وبأمان؟"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with clear conversation button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "بوابة الذكاء الاصطناعي المدمج",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "المساعد الذكي WAM AI",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تفريع المحادثة",
                        tint = Color.White
                    )
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg = msg, primaryColor = primaryColor, secondaryColor = secondaryColor)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E2E))
                                .padding(12.dp)
                                .align(Alignment.Start)
                        ) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "WAM AI يفكر ويكتب استجابته المشفرة...",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Presets row when keyboard is closed
            if (userInput.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("أسئلة شائعة مقترحة:", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetPrompts.take(2).forEach { p ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E2E))
                                    .border(0.5.dp, Color(0xFF2E2E3E), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.sendChatMessage(p) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetPrompts.takeLast(2).forEach { p ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E2E))
                                    .border(0.5.dp, Color(0xFF2E2E3E), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.sendChatMessage(p) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Text Entry Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("اطرح استفساراً من WAM AI...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            viewModel.sendChatMessage(userInput.trim())
                            userInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ChatBubble(msg: Pair<String, Boolean>, primaryColor: Color, secondaryColor: Color) {
    val isUser = msg.second
    val bubbleColor = if (isUser) primaryColor else Color(0xFF1E1E2E)
    val textColor = if (isUser) Color.Black else Color.White
    val align = if (isUser) Alignment.End else Alignment.Start
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = align
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(
                    width = 0.5.dp,
                    color = if (isUser) Color.Transparent else Color(0xFF2E2E3E),
                    shape = shape
                )
        ) {
            Text(
                text = msg.first,
                color = textColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp),
                textAlign = if(isUser) TextAlign.End else TextAlign.Start
            )
        }
    }
}
