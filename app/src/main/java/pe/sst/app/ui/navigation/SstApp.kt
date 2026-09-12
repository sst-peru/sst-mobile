@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pe.sst.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pe.sst.app.AppContainer
import pe.sst.app.data.remote.UserDto
import pe.sst.app.feature.auth.LoginScreen
import pe.sst.app.feature.auth.RegisterScreen
import pe.sst.app.feature.committee.CommitteeScreen
import pe.sst.app.feature.dashboard.DashboardScreen
import pe.sst.app.feature.epp.EppScreen
import pe.sst.app.feature.inspections.InspectionsScreen
import pe.sst.app.feature.iperc.IpercScreen
import pe.sst.app.feature.reports.NewReportScreen
import pe.sst.app.feature.reports.ReportDetailScreen
import pe.sst.app.feature.reports.ReportListScreen
import pe.sst.app.feature.reports.ServerReportsScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "registro"
    const val DASHBOARD = "tablero"
    const val NEW = "reportes/nuevo"
    const val SERVER_LIST = "reportes"
    const val LOCAL_LIST = "reportes/locales"
    const val DETAIL = "reportes/{id}"
    const val IPERC = "iperc"
    const val EPP = "epp"
    const val INSPECTIONS = "inspecciones"
    const val COMMITTEE = "comite"
    const val MORE = "mas"

    fun detail(id: Int) = "reportes/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun SstApp(container: AppContainer) {
    var usuario by remember { mutableStateOf<UserDto?>(null) }
    var logueado by remember { mutableStateOf(false) }
    var cargandoSesion by remember { mutableStateOf(true) }

    LaunchedEffect(logueado) {
        cargandoSesion = true
        val hayToken = container.authRepository.isLoggedIn()
        usuario = if (hayToken) {
            // Si el perfil no carga (sin señal al abrir), seguimos sin rol: es el nivel con
            // menos permisos, nunca al revés.
            runCatching { container.authRepository.me() }.getOrNull()
        } else {
            null
        }
        logueado = hayToken
        cargandoSesion = false
    }

    if (cargandoSesion) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        return
    }

    // Dos flujos separados en vez de uno solo: así la pantalla de inicio se recalcula al
    // entrar, en vez de quedarse clavada en la que se eligió al abrir la app.
    if (!logueado) {
        AuthFlow(container = container, onLoggedIn = { logueado = true })
    } else {
        MainFlow(
            container = container,
            usuario = usuario,
            onLogout = {
                usuario = null
                logueado = false
            },
        )
    }
}

/** Login y registro, sin barra inferior. */
@Composable
private fun AuthFlow(container: AppContainer, onLoggedIn: () -> Unit) {
    var enRegistro by remember { mutableStateOf(false) }

    if (enRegistro) {
        RegisterScreen(
            container = container,
            onRegistered = onLoggedIn,
            onCancel = { enRegistro = false },
        )
    } else {
        LoginScreen(
            container = container,
            onLoggedIn = onLoggedIn,
            onRegister = { enRegistro = true },
        )
    }
}

@Composable
private fun MainFlow(
    container: AppContainer,
    usuario: UserDto?,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val canManage = usuario?.role in setOf("SUPERVISOR", "COMITE", "ADMIN")

    val tabs = if (canManage) {
        listOf(
            Tab(Routes.DASHBOARD, "Tablero", Icons.Default.Dashboard),
            Tab(Routes.SERVER_LIST, "Reportes", Icons.Default.List),
            Tab(Routes.INSPECTIONS, "Inspecciones", Icons.Default.Assignment),
            Tab(Routes.MORE, "Más", Icons.Default.MoreHoriz),
        )
    } else {
        listOf(
            Tab(Routes.NEW, "Reportar", Icons.Default.Add),
            Tab(Routes.SERVER_LIST, "Mis reportes", Icons.Default.List),
            Tab(Routes.EPP, "Mis EPP", Icons.Default.Assignment),
            Tab(Routes.MORE, "Más", Icons.Default.MoreHoriz),
        )
    }

    val backStack by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val seleccionado = backStack?.destination?.hierarchy?.any {
                        it.route == tab.route
                    } == true
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (canManage) Routes.DASHBOARD else Routes.NEW,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) { DashboardScreen(container) }
            composable(Routes.NEW) {
                NewReportScreen(
                    container = container,
                    onDone = { navController.navigate(Routes.SERVER_LIST) },
                    onCancel = { navController.navigate(Routes.SERVER_LIST) },
                )
            }
            composable(Routes.SERVER_LIST) {
                ServerReportsScreen(
                    container = container,
                    onOpenReport = { id -> navController.navigate(Routes.detail(id)) },
                )
            }
            composable(Routes.LOCAL_LIST) {
                ReportListScreen(
                    container = container,
                    onNewReport = { navController.navigate(Routes.NEW) },
                    onLogout = onLogout,
                )
            }
            composable(Routes.DETAIL) { entry ->
                val id = entry.arguments?.getString("id")?.toIntOrNull()
                if (id == null) {
                    Text("Reporte no encontrado", modifier = Modifier.padding(16.dp))
                } else {
                    ReportDetailScreen(
                        container = container,
                        reportId = id,
                        canManage = canManage,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(Routes.IPERC) { IpercScreen(container) }
            composable(Routes.EPP) { EppScreen(container, usuario?.id) }
            composable(Routes.INSPECTIONS) { InspectionsScreen(container) }
            composable(Routes.COMMITTEE) { CommitteeScreen(container) }
            composable(Routes.MORE) {
                MoreScreen(
                    container = container,
                    canManage = canManage,
                    usuario = usuario,
                    onNavigate = { ruta -> navController.navigate(ruta) },
                    onLogout = onLogout,
                )
            }
        }
    }
}

/** Pantalla "Más": todo lo que no entra en la barra inferior sigue siendo alcanzable. */
@Composable
private fun MoreScreen(
    container: AppContainer,
    canManage: Boolean,
    usuario: UserDto?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val opciones = buildList {
        add(Routes.IPERC to "Matriz IPERC")
        add(Routes.COMMITTEE to "Comité de SST")
        add(Routes.LOCAL_LIST to "Reportes guardados en el celular")
        if (canManage) {
            add(Routes.EPP to "Entregas de EPP")
            add(Routes.NEW to "Reportar un hallazgo")
        } else {
            add(Routes.INSPECTIONS to "Inspecciones")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        usuario?.let {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${it.firstName} ${it.lastName}".trim().ifBlank { it.username })
                    Text(it.role, style = MaterialTheme.typography.labelSmall)
                    it.companyName?.let { empresa ->
                        Text(empresa, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        opciones.forEach { (ruta, etiqueta) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                onClick = { onNavigate(ruta) },
            ) {
                Text(etiqueta, modifier = Modifier.padding(16.dp))
            }
        }

        TextButton(
            onClick = {
                scope.launch {
                    container.authRepository.logout()
                    onLogout()
                }
            },
            modifier = Modifier.padding(top = 12.dp),
        ) { Text("Cerrar sesión") }
    }
}
