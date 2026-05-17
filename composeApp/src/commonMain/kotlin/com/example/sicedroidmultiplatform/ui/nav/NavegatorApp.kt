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
import com.example.sicedroidmultiplatform.createDatabase
import com.example.sicedroidmultiplatform.data.repository.SicenetRemoteDataSource
import com.example.sicedroidmultiplatform.data.repository.SqlDelightLocalDataSource
import com.example.sicedroidmultiplatform.data.repository.SicenetRepository
import com.example.sicedroidmultiplatform.ui.components.SicenetBottomBar
import com.example.sicedroidmultiplatform.ui.screens.*
import com.example.sicedroidmultiplatform.ui.viewModel.SicenetViewModel

@Composable
fun SicenetApp() {
    val navController = rememberNavController()

    val viewModel = remember {
        val database = createDatabase()
        
        // Ahora usamos las nuevas clases dentro de la carpeta repository
        val localDataSource = SqlDelightLocalDataSource(database)
        val remoteDataSource = SicenetRemoteDataSource()
        
        val repository = SicenetRepository(localDataSource, remoteDataSource)
        SicenetViewModel(repository)
    }

    val startDestination = remember {
        if (viewModel.checkExistingSession()) "profile" else "login"
    }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute != "login") {
                SicenetBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("profile") { saveState = true }
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
            startDestination = startDestination,
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
                CalificacionesPorUnidadScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("calificaciones_finales") {
                CalificacionesFinalesScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("kardex") {
                KardexScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("carga_academica") {
                CargaAcademicaScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
