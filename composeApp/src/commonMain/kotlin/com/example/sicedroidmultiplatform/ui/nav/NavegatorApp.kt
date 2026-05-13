package com.example.sicedroidmultiplatform.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sicedroidmultiplatform.ui.components.SicenetBottomBar
import com.example.sicedroidmultiplatform.ui.screens.*
import com.example.sicedroidmultiplatform.ui.viewModel.SicenetViewModel

@Composable
fun SicenetApp() {
    val navController = rememberNavController()
    val viewModel = remember { SicenetViewModel() }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute != "login") {
                SicenetBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("profile") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(viewModel, onLoginSuccess = {
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("profile") {
                PerfilScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                )
            }
            composable("calificaciones_unidad") {
                CalificacionesPorUnidadScreen(
                    viewModel = viewModel, 
                    onBack = { navController.popBackStack() }
                )
            }
            composable("calificaciones_finales") {
                CalificacionesFinalesScreen(
                    viewModel = viewModel, 
                    onBack = { navController.popBackStack() }
                )
            }
            composable("kardex") {
                KardexScreen(
                    viewModel = viewModel, 
                    onBack = { navController.popBackStack() }
                )
            }
            composable("carga_academica") {
                CargaAcademicaScreen(
                    viewModel = viewModel, 
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
