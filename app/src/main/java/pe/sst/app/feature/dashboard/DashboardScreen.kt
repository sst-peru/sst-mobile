package pe.sst.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.sst.app.AppContainer
import pe.sst.app.ui.common.Async
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.rememberAsync

/**
 * Tablero para supervisor y comité: las mismas métricas que la web.
 *
 * Sirve en campo: el supervisor que está en obra puede ver cuántos hallazgos tiene abiertos
 * sin volver a la oficina.
 */
@Composable
fun DashboardScreen(container: AppContainer) {
    val (mttr, recargar) = rememberAsync(Unit) { container.sgsstRepository.mttr(90) }
    val (cumplimiento, _) = rememberAsync(Unit) {
        container.sgsstRepository.inspectionCompliance(90)
    }
    val (comite, _) = rememberAsync(Unit) { container.sgsstRepository.committeeCompliance() }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Tablero de SST", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Últimos 90 días",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        AsyncContent(state = mttr.value, onRetry = recargar) { datos ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi(
                    etiqueta = "MTTR de hallazgos",
                    valor = datos.mttrHours?.let { horas ->
                        if (horas < 24) "%.1f h".format(horas) else "%.1f días".format(horas / 24)
                    } ?: "—",
                    detalle = "Promedio entre el reporte y su cierre",
                )
                Kpi(
                    etiqueta = "Hallazgos abiertos",
                    valor = datos.openReports.toString(),
                    detalle = "${datos.closedReports} cerrados en la ventana",
                    alerta = datos.openReports > 0,
                )

                if (datos.bySeverity.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("MTTR por severidad", fontWeight = FontWeight.SemiBold)
                            datos.bySeverity.forEach { (severidad, fila) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                    Text(severidad, modifier = Modifier.weight(1f))
                                    Text(
                                        "%.1f h · %d cerrados".format(fila.mttrHours, fila.closed),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val estadoCumplimiento = cumplimiento.value
        if (estadoCumplimiento is Async.Ok) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Kpi(
                    etiqueta = "Cumplimiento de inspecciones",
                    valor = estadoCumplimiento.data.complianceRatePct?.let { "%.0f%%".format(it) } ?: "—",
                    detalle = "${estadoCumplimiento.data.performed} de ${estadoCumplimiento.data.scheduled} programadas",
                )
                Kpi(
                    etiqueta = "Inspecciones vencidas",
                    valor = estadoCumplimiento.data.overdue.toString(),
                    detalle = "Pasaron su fecha sin realizarse",
                    alerta = estadoCumplimiento.data.overdue > 0,
                )
            }
        }

        val estadoComite = comite.value
        if (estadoComite is Async.Ok && estadoComite.data.hasCommittee) {
            Kpi(
                etiqueta = "Acuerdos del comité cumplidos",
                valor = estadoComite.data.agreementsCompliancePct?.let { "%.0f%%".format(it) } ?: "—",
                detalle = "${estadoComite.data.agreementsPending ?: 0} pendientes · " +
                    "${estadoComite.data.meetingsTotal ?: 0} actas",
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun Kpi(
    etiqueta: String,
    valor: String,
    detalle: String,
    alerta: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
            Text(
                valor,
                style = MaterialTheme.typography.headlineMedium,
                color = if (alerta) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(detalle, style = MaterialTheme.typography.labelSmall)
        }
    }
}
