package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.WamViewModel
import com.example.ui.WamViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.AlMaherTheme

enum class Screen {
    HOME, SAVINGS, FINANCE, CHATBOT, ABOUT, ADMIN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: WamViewModel by viewModels {
                WamViewModelFactory(application)
            }

            val currentUser by viewModel.currentUser.collectAsState()
            var currentScreen by remember { mutableStateOf(Screen.HOME) }
            var showAdminDashboardOverlay by remember { mutableStateOf(false) }
            
            // Onboarding guard: user must be logged in to access the system
            var isOnboarded by remember { mutableStateOf(false) }

            val primaryColor = Color(0xFFFFD700) // Gold
            val secondaryColor = Color(0xFF00D4FF) // Electric Blue

            AlMaherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isOnboarded || currentUser == null) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onOnboardingComplete = {
                                isOnboarded = true
                            },
                            onNavigateToAdmin = {
                                showAdminDashboardOverlay = true
                            }
                        )
                    } else if (showAdminDashboardOverlay) {
                        AdminScreen(
                            viewModel = viewModel,
                            onNavigateBack = { showAdminDashboardOverlay = false }
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color(0xFF0A0E17),
                                    contentColor = Color.White
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.HOME,
                                        onClick = { currentScreen = Screen.HOME },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                        label = { Text("الرئيسية", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = primaryColor,
                                            indicatorColor = primaryColor,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen == Screen.SAVINGS,
                                        onClick = { currentScreen = Screen.SAVINGS },
                                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Savings") },
                                        label = { Text("ادخار ذكي", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = primaryColor,
                                            indicatorColor = primaryColor,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen == Screen.FINANCE,
                                        onClick = { currentScreen = Screen.FINANCE },
                                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Finance") },
                                        label = { Text("تمويل ميسر", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = primaryColor,
                                            indicatorColor = primaryColor,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen == Screen.CHATBOT,
                                        onClick = { currentScreen = Screen.CHATBOT },
                                        icon = { Icon(Icons.Default.Face, contentDescription = "المساعد الذكي") },
                                        label = { Text("المستشار الذكي WAM AI", fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = primaryColor,
                                            indicatorColor = primaryColor,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen == Screen.ABOUT,
                                        onClick = { currentScreen = Screen.ABOUT },
                                        icon = { Icon(Icons.Default.Info, contentDescription = "About WAM") },
                                        label = { Text("عن الـ WAM", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = primaryColor,
                                            indicatorColor = primaryColor,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            val user = currentUser
                            if (user != null) {
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    when (currentScreen) {
                                        Screen.HOME -> HomeScreen(
                                            viewModel = viewModel,
                                            currentUser = user,
                                            onNavigateToSavings = { currentScreen = Screen.SAVINGS }
                                        )
                                        Screen.SAVINGS -> SavingsScreen(
                                            viewModel = viewModel,
                                            currentUser = user
                                        )
                                        Screen.FINANCE -> FinanceScreen(
                                            viewModel = viewModel,
                                            currentUser = user
                                        )
                                        Screen.CHATBOT -> ChatbotScreen(
                                            viewModel = viewModel
                                        )
                                        Screen.ABOUT -> AboutScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { currentScreen = Screen.HOME },
                                            onTriggerAdminGate = {
                                                showAdminDashboardOverlay = true
                                            }
                                        )
                                        else -> currentScreen = Screen.HOME
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
