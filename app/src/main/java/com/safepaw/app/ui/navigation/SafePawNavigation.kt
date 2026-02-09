package com.safepaw.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safepaw.app.ui.screens.*
import com.safepaw.app.ui.viewmodels.AnimalViewModel
import com.safepaw.app.ui.viewmodels.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
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
            // En una implementación real, aquí buscaríamos el animal en el ViewModel
            // por simplicidad pasamos un placeholder o disparamos la búsqueda
            // AnimalDetailScreen(...)
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
