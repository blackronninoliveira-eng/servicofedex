package com.servicofedex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.servicofedex.app.ui.EarningsScreen
import com.servicofedex.app.ui.HistoryScreen
import com.servicofedex.app.ui.HomeScreen
import com.servicofedex.app.ui.RegisterScreen
import com.servicofedex.app.ui.SettingsScreen
import com.servicofedex.app.ui.theme.Dark
import com.servicofedex.app.ui.theme.YellowDeep
import com.servicofedex.app.ui.theme.ServicoFedexTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            ServicoFedexTheme(darkTheme = uiState.settings.darkTheme) {
                ConfigureSystemBars(darkTheme = uiState.settings.darkTheme)
                MainScreen(state = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ConfigureSystemBars(darkTheme: Boolean) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window
    DisposableEffect(darkTheme, window) {
        if (window != null) {
            val barColor = (if (darkTheme) Dark else YellowDeep).toArgb()
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
        onDispose { }
    }
}

@Composable
private fun MainScreen(state: AppUiState, viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        Tab("Home", Icons.Default.Home),
        Tab("Registro", Icons.Default.Add),
        Tab("Histórico", Icons.Default.History),
        Tab("Ganhos", Icons.Default.AttachMoney),
        Tab("Config.", Icons.Default.Settings),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(state = state)
                1 -> RegisterScreen(state = state, viewModel = viewModel)
                2 -> HistoryScreen(state = state, viewModel = viewModel)
                3 -> EarningsScreen(state = state)
                4 -> SettingsScreen(state = state, viewModel = viewModel)
            }
        }
    }
}

private data class Tab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
