package com.safepaw.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safepaw.app.ui.screens.*
import com.safepaw.app.ui.viewmodels.AnimalUiState
import com.safepaw.app.ui.viewmodels.AnimalViewModel
import com.safepaw.app.ui.viewmodels.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
    object AnimalAdd : Screen("animal_add")
    object AnimalDetail : Screen("animal_detail/{microchip}") {
        fun createRoute(microchip: String) = "animal_detail/$microchip"
    }
    object MedicalHistory : Screen("medical_history/{id}/{nombre}") {
        fun createRoute(id: String, nombre: String) = "medical_history/$id/$nombre"
    }
}

@Composable
fun SafePawNavigation(
    authViewModel: AuthViewModel,
    animalViewModel: AnimalViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        
        // 1. Pantalla de Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // 2. Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = animalViewModel,
                onAnimalClick = { animal ->
                    navController.navigate(Screen.AnimalDetail.createRoute(animal.microchip))
                },
                onScanClick = {
                    navController.navigate(Screen.Scanner.route)
                },
                onAddClick = {
                    navController.navigate(Screen.AnimalAdd.route)
                }
            )
        }

        // 2.5 Añadir Animal
        composable(Screen.AnimalAdd.route) {
            AnimalAddScreen(
                viewModel = animalViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Escáner
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onBarcodeDetected = { microchip ->
                    navController.navigate(Screen.AnimalDetail.createRoute(microchip)) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }

        // 4. Ficha Técnica (Buscando por microchip para soportar el escáner)
        composable(
            route = Screen.AnimalDetail.route,
            arguments = listOf(navArgument("microchip") { type = NavType.StringType })
        ) { backStackEntry ->
            val microchip = backStackEntry.arguments?.getString("microchip") ?: ""
            
            // Observamos el estado del ViewModel para reaccionar a cambios
            val uiState by animalViewModel.uiState.collectAsState()
            
            val animal = when (uiState) {
                is AnimalUiState.Success -> (uiState as AnimalUiState.Success).animales.find { it.microchip == microchip }
                else -> null
            }
            
            if (animal != null) {
                AnimalDetailScreen(
                    animal = animal,
                    viewModel = animalViewModel,
                    navController = navController,
                    onBack = { 
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            } else {
                // Si no se encuentra, intentamos buscarlo en la DB (por si venimos de un deep link o escaneo)
                LaunchedEffect(microchip) {
                    animalViewModel.searchByMicrochip(microchip)
                }
                
                Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // 5. Historial Médico
        composable(
            route = Screen.MedicalHistory.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            MedicalHistoryScreen(
                idAnimal = id,
                nombreAnimal = nombre,
                userRole = "Vet", // Esto vendría del AuthViewModel en una app real
                viewModel = animalViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
