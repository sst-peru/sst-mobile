package pe.sst.app.feature.epp

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.sst.app.AppContainer
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.mensajeDeError
import pe.sst.app.ui.common.rememberAsync

/**
 * EPP entregados. El API ya filtra: el operario ve solo los suyos.
 *
 * La conformidad se firma desde acá, que es donde tiene sentido: el trabajador tiene el casco
 * en la mano y el celular en la otra.
 */
@Composable
fun EppScreen(container: AppContainer, userId: Int?) {
    val (carga, recargar) = rememberAsync(Unit) {
        container.sgsstRepository.eppDeliveries()
    }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Equipos de protección personal", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Entregas registradas y su vida útil",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        AsyncContent(state = carga.value, onRetry = recargar) { entregas ->
            if (entregas.isEmpty()) {
                Text("No tienes EPP registrados todavía.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entregas) { entrega ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    entrega.itemName,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                if (entrega.isExpired) {
                                    Text(
                                        "Vencido",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            Text(entrega.workerName, style = MaterialTheme.typography.labelSmall)
                            Text(
                                "Entregado ${entrega.deliveredAt.take(10)}" +
                                    (entrega.expiresAt?.let { " · vence $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            if (entrega.acknowledged) {
                                Text(
                                    "Conformidad firmada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            } else if (entrega.worker == userId) {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                container.sgsstRepository.acknowledgeEpp(entrega.id)
                                                recargar()
                                            } catch (e: Exception) {
                                                error = mensajeDeError(e)
                                            }
                                        }
                                    },
                                ) { Text("Dar conformidad") }
                            } else {
                                Text(
                                    "Conformidad pendiente",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
