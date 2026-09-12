package pe.sst.app.feature.inspections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import pe.sst.app.data.remote.InspectionDto
import pe.sst.app.ui.common.Async
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.mensajeDeError
import pe.sst.app.ui.common.rememberAsync

/**
 * Inspecciones programadas y su ejecución con checklist.
 *
 * Completarlas desde el celular es lo que hace que la tasa de cumplimiento sea real: si hay
 * que volver a la oficina a registrar la inspección, se registra tarde o no se registra.
 */
@Composable
fun InspectionsScreen(container: AppContainer) {
    val (carga, recargar) = rememberAsync(Unit) { container.sgsstRepository.inspections() }
    val (programas, _) = rememberAsync(Unit) { container.sgsstRepository.inspectionSchedules() }
    var ejecutando by remember { mutableStateOf<InspectionDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Inspecciones", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Programadas, realizadas y vencidas",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        AsyncContent(state = carga.value, onRetry = recargar) { inspecciones ->
            if (inspecciones.isEmpty()) {
                Text("No hay inspecciones programadas.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(inspecciones) { inspeccion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    inspeccion.title,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    when {
                                        inspeccion.status == "REALIZADA" -> "Realizada"
                                        inspeccion.isOverdue -> "Vencida"
                                        else -> "Pendiente"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (inspeccion.isOverdue) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                            Text(
                                "${inspeccion.areaName} · programada ${inspeccion.dueDate}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            if (inspeccion.findings.isNotBlank()) {
                                Text(
                                    inspeccion.findings,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            if (inspeccion.status != "REALIZADA") {
                                TextButton(onClick = { ejecutando = inspeccion }) {
                                    Text("Realizar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ejecutando?.let { inspeccion ->
        val estadoProgramas = programas.value
        val checklist = if (estadoProgramas is Async.Ok) {
            estadoProgramas.data.firstOrNull { it.id == inspeccion.schedule }?.checklist ?: emptyList()
        } else {
            emptyList()
        }
        DialogoEjecucion(
            titulo = inspeccion.title,
            checklist = checklist,
            container = container,
            inspectionId = inspeccion.id,
            onCancel = { ejecutando = null },
            onDone = {
                ejecutando = null
                recargar()
            },
            onError = {
                error = it
                ejecutando = null
            },
        )
    }
}

@Composable
private fun DialogoEjecucion(
    titulo: String,
    checklist: List<String>,
    container: AppContainer,
    inspectionId: Int,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    onError: (String) -> Unit,
) {
    var hallazgos by remember { mutableStateOf("") }
    val marcados = remember { mutableStateOf(checklist.associateWith { false }) }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(titulo) },
        text = {
            Column {
                if (checklist.isEmpty()) {
                    Text(
                        "Este programa no tiene checklist definido.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                checklist.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Checkbox(
                            checked = marcados.value[item] ?: false,
                            onCheckedChange = { marcado ->
                                marcados.value = marcados.value.toMutableMap().apply {
                                    this[item] = marcado
                                }
                            },
                        )
                        Text(item, style = MaterialTheme.typography.bodySmall)
                    }
                }
                OutlinedTextField(
                    value = hallazgos,
                    onValueChange = { hallazgos = it },
                    label = { Text("Hallazgos") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    enviando = true
                    scope.launch {
                        try {
                            container.sgsstRepository.completeInspection(
                                inspectionId, hallazgos, marcados.value
                            )
                            onDone()
                        } catch (e: Exception) {
                            onError(mensajeDeError(e))
                        }
                        enviando = false
                    }
                },
                enabled = !enviando,
            ) { Text("Marcar realizada") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } },
    )
}
