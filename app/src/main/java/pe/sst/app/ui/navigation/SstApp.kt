package pe.sst.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.sst.app.AppContainer
import pe.sst.app.feature.auth.LoginScreen
import pe.sst.app.feature.reports.NewReportScreen
import pe.sst.app.feature.reports.ReportListScreen

object Routes {
    const val LOGIN = "login"
    const val LIST = "reportes"
    const val NEW = "reportes/nuevo"
}

@Composable
fun SstApp(container: AppContainer) {
    val navController = rememberNavController()
    var start by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        start = if (container.authRepository.isLoggedIn()) Routes.LIST else Routes.LOGIN
    }

    val startDestination = start ?: return

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                container = container,
                onLoggedIn = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.LIST) {
            ReportListScreen(
                container = container,
                onNewReport = { navController.navigate(Routes.NEW) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LIST) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.NEW) {
            NewReportScreen(
                container = container,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
