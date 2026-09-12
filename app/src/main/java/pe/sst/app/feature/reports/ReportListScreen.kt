package pe.sst.app.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.sst.app.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    container: AppContainer,
    onNewReport: () -> Unit,
    onLogout: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val localFlow = remember { container.reportRepository.observeLocal() }
    val pendingFlow = remember { container.reportRepository.observePendingCount() }
    val local by localFlow.collectAsState(initial = emptyList())
    val pendingCount by pendingFlow.collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reportes") },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            container.authRepository.logout()
                            onLogout()
                        }
                    }) { Text("Salir") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNewReport) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Reportar", modifier = Modifier.padding(start = 8.dp))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (pendingCount > 0) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Text(
                        "$pendingCount reporte(s) esperando señal para enviarse",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (local.isEmpty()) {
                Text(
                    "Todavía no has reportado nada. Si ves algo peligroso, tócalo en Reportar: " +
                        "son tres pasos.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(local) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    if (report.kind == "ACTO") "Acto inseguro"
                                    else "Condición insegura",
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    if (report.uploaded) "Enviado" else "Pendiente",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (report.uploaded) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                                )
                            }
                            Text(
                                report.description.ifBlank { "Sin descripción" },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                "Severidad ${report.severity} · ${report.occurredAtIso.take(16)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            report.lastError?.let {
                                Text(
                                    "Último error al enviar: $it",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
