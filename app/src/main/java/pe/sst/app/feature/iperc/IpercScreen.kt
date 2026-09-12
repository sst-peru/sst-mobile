package pe.sst.app.feature.iperc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.sst.app.AppContainer
import pe.sst.app.data.remote.IpercEntryDto
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.rememberAsync

/**
 * Matriz IPERC en el celular.
 *
 * Para el operario esto es consulta: "¿qué controles aplican a mi puesto?". Es el uso real en
 * campo, y hoy eso vive en un archivador de la oficina que nadie abre.
 */
@Composable
fun IpercScreen(container: AppContainer) {
    val (carga, recargar) = rememberAsync(Unit) {
        container.sgsstRepository.ipercEntries()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matriz IPERC", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Peligros identificados y cómo se controlan",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        AsyncContent(state = carga.value, onRetry = recargar) { entradas ->
            if (entradas.isEmpty()) {
                Text("La matriz todavía no tiene peligros registrados.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entradas) { entrada -> EntradaCard(entrada) }
            }
        }
    }
}

@Composable
private fun EntradaCard(entrada: IpercEntryDto) {
    val critico = entrada.riskLevel in setOf("IMPORTANTE", "INTOLERABLE")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(entrada.jobPosition, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    "${entrada.riskLevel} (${entrada.riskScore})",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (critico) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
            Text(entrada.areaName, style = MaterialTheme.typography.labelSmall)
            Text(
                "Peligro: ${entrada.hazard}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text("Riesgo: ${entrada.risk}", style = MaterialTheme.typography.bodySmall)
            if (entrada.existingControls.isNotBlank()) {
                Text(
                    "Controles: ${entrada.existingControls}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            entrada.sourceReport?.let {
                Text(
                    "Originado en el reporte #$it",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
