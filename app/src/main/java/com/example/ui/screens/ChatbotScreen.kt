package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    viewModel: WamViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatMessages = viewModel.chatMessages
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val listState = rememberLazyListState()

    var userPrompt by remember { mutableStateOf("") }
    
    // API KEY RESOLUTION: first look in BuildConfig, otherwise fall back to manual custom key
    var customApiKey by remember { 
        mutableStateOf(
            try { 
                val key = com.example.BuildConfig.GEMINI_API_KEY
                if (key == "UNSPECIFIED" || key.isEmpty()) "" else key
            } catch (e: Exception) { 
                "" 
            }
        ) 
    }

    var showApiKeyConfig by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFFFD700) // Gold
    val secondaryColor = Color(0xFF00D4FF) // Electric Blue
    val darkBgColor = Color(0xFF0A0E17)
    val cardColor = Color(0xFF131722)

    // Scroll chat to end when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBgColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chat Header View
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = primaryColor)
                    }
                    Column {
                        Text(
                            text = "المساعد المالي الذكي WAM AI",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "بوابة الذكاء الاصطناعي للمحفظة",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )
                    }
                }

                // Key setup button
                IconButton(
                    onClick = { showApiKeyConfig = !showApiKeyConfig },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Configure API Key",
                        tint = if (customApiKey.isNotBlank()) primaryColor else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Expanded Api Key Config Card
        AnimatedVisibility(visible = showApiKeyConfig) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "إعداد مفتاح Gemini API الخاص بك:",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتم تحميل مفتاحك تلقائياً من لوحة Secrets في AI Studio. وإذا كنت ترغب في تجاوز أو استخدام مفتاح مخصص آخر، يرجى كتابته هنا:",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )

                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        placeholder = { Text("أدخل AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )

                    TextButton(
                        onClick = {
                            showApiKeyConfig = false
                            Toast.makeText(context, "تم حفظ مفتاح الترخيص محلياً في الجلسة النشطة!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("تم، حفظ المفتاح", color = primaryColor, fontSize = 11.sp)
                    }
                }
            }
        }

        // Messages Bubble Board
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.015f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { messagePair ->
                val (text, isUser) = messagePair
                val bubbleColor = if (isUser) secondaryColor.copy(alpha = 0.15f) else cardColor
                val textColor = if (isUser) secondaryColor else Color.White
                val alignment = if (isUser) Alignment.End else Alignment.Start
                val shape = if (isUser) {
                    RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
                } else {
                    RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bubbleColor),
                        shape = shape,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.02f), shape)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isUser) "أنت" else "المستشار الذكي WAM AI",
                                color = if (isUser) secondaryColor else primaryColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // Typing indication spinner
            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = primaryColor,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "جاري التفكير المالي الآمن لـ WAM...",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Direct Text inputs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = { Text("اسأل WAM AI عن التمويل، الادخار، أو دفع الفواتير...") },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userPrompt.isNotBlank()) {
                        viewModel.askGemini(userPrompt, customApiKey)
                        userPrompt = ""
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text")
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                )
            )

            FloatingActionButton(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        viewModel.askGemini(userPrompt, customApiKey)
                        userPrompt = ""
                    }
                },
                containerColor = primaryColor,
                contentColor = Color.Black,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_fab_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}
