@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pe.sst.app.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.sst.app.AppContainer
import pe.sst.app.data.remote.ReportDto
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.rememberAsync

private val ESTADOS = listOf(
    "" to "Todos",
    "ABIERTO" to "Abiertos",
    "EN_PROCESO" to "En proceso",
    "CERRADO" to "Cerrados",
)

/**
 * Reportes tal como los ve el servidor.
 *
 * El API ya filtra por rol: el operario recibe solo los suyos y el supervisor los de toda la
 * empresa, así que esta pantalla es la misma para ambos y no necesita lógica de permisos.
 */
@Composable
fun ServerReportsScreen(
    container: AppContainer,
    onOpenReport: (Int) -> Unit,
) {
    var estado by remember { mutableStateOf("") }
    val (carga, recargar) = rememberAsync(estado) {
        container.reportRepository.remoteReports(estado.ifBlank { null })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            ESTADOS.forEach { (valor, etiqueta) ->
                FilterChip(
                    selected = estado == valor,
                    onClick = { estado = valor },
                    label = { Text(etiqueta) },
                )
            }
        }

        AsyncContent(state = carga.value, onRetry = recargar) { reportes ->
            if (reportes.isEmpty()) {
                Text(
                    "No hay reportes con ese filtro.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(reportes) { reporte ->
                    ReportCard(reporte) { onOpenReport(reporte.id) }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(reporte: ReportDto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (reporte.kind == "ACTO") "Acto inseguro" else "Condición insegura",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    reporte.status.replace('_', ' '),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (reporte.status == "CERRADO") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(
                reporte.categoryName ?: reporte.description.ifBlank { "Sin descripción" },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "#${reporte.id} · ${reporte.areaName ?: "sin área"} · severidad ${reporte.severity}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
