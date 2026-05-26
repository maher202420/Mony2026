package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel
import com.example.ui.WamViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    HOME,
    SAVINGS,
    FINANCE,
    CHATBOT,
    ABOUT
}

class MainActivity : ComponentActivity() {

    private val viewModel: WamViewModel by viewModels {
        WamViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val adminConfig by viewModel.adminConfig.collectAsState()

                // Decode dynamic theme colors directly from Local State Config (Room-backed!)
                val primaryHex = adminConfig?.primaryColorHex ?: "#FFD700"
                val secondaryHex = adminConfig?.secondaryColorHex ?: "#00D4FF"
                val primaryColor = remember(primaryHex) { parseHexToColor(primaryHex, Color(0xFFFFD700)) }
                val secondaryColor = remember(secondaryHex) { parseHexToColor(secondaryHex, Color(0xFF00D4FF)) }

                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                var showAdminDashboardOverlay by remember { mutableStateOf(false) }
                var isOnboarded by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E17)
                ) {
                    if (!isOnboarded) {
                        OnboardingScreen(
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            onOnboardingComplete = { fullName, phone ->
                                viewModel.updateMainUserProfile(fullName, phone)
                                isOnboarded = true
                            }
                        )
                    } else if (showAdminDashboardOverlay) {
                        AdminScreen(
                            viewModel = viewModel,
                            onNavigateBack = { showAdminDashboardOverlay = false },
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                CustomM3NavigationBar(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { currentScreen = it },
                                    primaryColor = primaryColor,
                                    cryptoEnabled = adminConfig?.isCryptoEnabled == true
                                )
                            },
                            contentWindowInsets = WindowInsets.safeDrawing
                        ) { innerPadding ->
                            // Screen Views with crisp fade transitions
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentScreen) {
                                    Screen.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor
                                    )
                                    Screen.SAVINGS -> SavingsScreen(
                                        viewModel = viewModel,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor
                                    )
                                    Screen.FINANCE -> FinanceScreen(
                                        viewModel = viewModel,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor
                                    )
                                    Screen.CHATBOT -> ChatbotScreen(
                                        viewModel = viewModel,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor
                                    )
                                    Screen.ABOUT -> AboutScreen(
                                        viewModel = viewModel,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor,
                                        onNavigateToAdmin = { showAdminDashboardOverlay = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomM3NavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    primaryColor: Color,
    cryptoEnabled: Boolean
) {
    NavigationBar(
        containerColor = Color(0xFF131722),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onScreenSelected(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
            label = { Text("الرئيسية", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = primaryColor,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.SAVINGS,
            onClick = { onScreenSelected(Screen.SAVINGS) },
            icon = { Icon(Icons.Default.Savings, contentDescription = "الأوعية") },
            label = { Text("الأوعية", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = primaryColor,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.FINANCE,
            onClick = { onScreenSelected(Screen.FINANCE) },
            icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = "المالية") },
            label = { Text("الاستثمار", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = primaryColor,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.CHATBOT,
            onClick = { onScreenSelected(Screen.CHATBOT) },
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "المساعد الذكي") },
            label = { Text("المستشار الذكي WAM AI", fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = primaryColor,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.ABOUT,
            onClick = { onScreenSelected(Screen.ABOUT) },
            icon = { Icon(Icons.Default.Info, contentDescription = "عن WAM") },
            label = { Text("عن التطبيق", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = primaryColor,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )
    }
}

fun parseHexToColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}
