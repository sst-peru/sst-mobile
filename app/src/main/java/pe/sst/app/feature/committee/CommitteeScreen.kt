package pe.sst.app.feature.committee

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
import pe.sst.app.data.remote.MeetingDto
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.rememberAsync

/**
 * Actas del comité de SST y sus acuerdos.
 *
 * Que el trabajador pueda leer las actas desde su celular no es un extra: la ley obliga a que
 * los acuerdos del comité se comuniquen, y hoy eso termina en un papel pegado en la pared.
 */
@Composable
fun CommitteeScreen(container: AppContainer) {
    val (carga, recargar) = rememberAsync(Unit) { container.sgsstRepository.meetings() }
    val (cumplimiento, _) = rememberAsync(Unit) {
        container.sgsstRepository.committeeCompliance()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Comité de SST", style = MaterialTheme.typography.headlineSmall)

        val estado = cumplimiento.value
        if (estado is pe.sst.app.ui.common.Async.Ok && estado.data.hasCommittee) {
            Text(
                "${estado.data.meetingsTotal ?: 0} actas · " +
                    "${estado.data.agreementsDone ?: 0} de ${estado.data.agreementsTotal ?: 0} acuerdos cumplidos",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        } else {
            Text(
                "Actas de reunión y acuerdos",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        AsyncContent(state = carga.value, onRetry = recargar) { actas ->
            if (actas.isEmpty()) {
                Text("Todavía no hay actas registradas.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(actas) { acta -> ActaCard(acta) }
            }
        }
    }
}

@Composable
private fun ActaCard(acta: MeetingDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Acta N° ${acta.number}",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (acta.quorumReached) "Con quórum" else "Sin quórum",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (acta.quorumReached) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(
                "${acta.date} · ${if (acta.isExtraordinary) "Extraordinaria" else "Ordinaria"} · " +
                    "${acta.attendeeCount} asistentes",
                style = MaterialTheme.typography.labelSmall,
            )
            if (acta.agenda.isNotBlank()) {
                Text(
                    "Agenda: ${acta.agenda}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            if (acta.agreements.isNotEmpty()) {
                Text(
                    "Acuerdos",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                acta.agreements.forEach { acuerdo ->
                    Text(
                        "• ${acuerdo.description}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "   ${acuerdo.responsibleName ?: "sin responsable"}" +
                            (acuerdo.dueDate?.let { " · plazo $it" } ?: "") +
                            " · ${acuerdo.status.replace('_', ' ')}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
