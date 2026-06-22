package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AddButtonScreen
import com.example.ui.CameraLearnScreen
import com.example.ui.DeviceDetailScreen
import com.example.ui.HomeScreen
import com.example.ui.IrViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF07080E)
                ) {
                    val navController = rememberNavController()
                    val viewModel: IrViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToDevice = { deviceId ->
                                    navController.navigate("device/$deviceId")
                                }
                            )
                        }

                        composable(
                            route = "device/{deviceId}",
                            arguments = listOf(
                                navArgument("deviceId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                            DeviceDetailScreen(
                                viewModel = viewModel,
                                deviceId = deviceId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToLearn = { buttonId ->
                                    navController.navigate("learn/$deviceId/$buttonId")
                                },
                                onNavigateToAddButton = { devId ->
                                    navController.navigate("add_button/$devId")
                                }
                            )
                        }

                        composable(
                            route = "learn/{deviceId}/{buttonId}",
                            arguments = listOf(
                                navArgument("deviceId") { type = NavType.StringType },
                                navArgument("buttonId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                            val buttonId = backStackEntry.arguments?.getString("buttonId") ?: ""
                            CameraLearnScreen(
                                viewModel = viewModel,
                                deviceId = deviceId,
                                buttonId = buttonId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "add_button/{deviceId}",
                            arguments = listOf(
                                navArgument("deviceId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                            AddButtonScreen(
                                viewModel = viewModel,
                                deviceId = deviceId,
                                onNavigateBack = { navController.popBackStack() },
                                onStartRecording = { buttonId ->
                                    navController.navigate("learn/$deviceId/$buttonId") {
                                        popUpTo("device/$deviceId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
