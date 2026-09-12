@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pe.sst.app.feature.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import pe.sst.app.AppContainer
import pe.sst.app.data.remote.UserDto
import pe.sst.app.ui.common.Async
import pe.sst.app.ui.common.AsyncContent
import pe.sst.app.ui.common.mensajeDeError
import pe.sst.app.ui.common.rememberAsync

/**
 * Detalle del hallazgo con su bitácora.
 *
 * Si el usuario es supervisor o comité, desde acá puede asignar responsable y cerrar el
 * hallazgo: exactamente lo mismo que hace en la web. Para el operario es solo lectura.
 */
@Composable
fun ReportDetailScreen(
    container: AppContainer,
    reportId: Int,
    canManage: Boolean,
    onBack: () -> Unit,
) {
    val (carga, recargar) = rememberAsync(reportId) {
        container.sgsstRepository.report(reportId)
    }
    var dialogo by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AsyncContent(state = carga.value, onRetry = recargar) { reporte ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text(
                "Reporte #${reporte.id}",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                if (reporte.kind == "ACTO") "Acto inseguro" else "Condición insegura",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Reportó ${reporte.reportedByName} · ${reporte.createdAt.take(16).replace('T', ' ')}" +
                    if (reporte.syncedOffline) " · llegó offline" else "",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Dato("Estado", reporte.status.replace('_', ' '))
                    Dato("Severidad", reporte.severity)
                    Dato("Categoría", reporte.categoryName ?: "—")
                    Dato("Área", reporte.areaName ?: "—")
                    Dato("Responsable", reporte.assignedToName ?: "Sin asignar")
                    Dato(
                        "Tiempo de resolución",
                        reporte.resolutionHours?.let { "%.1f h".format(it) } ?: "Sin cerrar",
                    )
                    if (reporte.description.isNotBlank()) {
                        Text(reporte.description, modifier = Modifier.padding(top = 8.dp))
                    }
                    if (reporte.closureNote.isNotBlank()) {
                        Text(
                            "Acción correctiva: ${reporte.closureNote}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            reporte.photo?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Evidencia del hallazgo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = 12.dp),
                )
            }

            Text(
                "Bitácora",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
            )
            reporte.actions.forEach { accion ->
                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(accion.authorName, style = MaterialTheme.typography.labelLarge)
                    Text(
                        accion.createdAt.take(16).replace('T', ' '),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(accion.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (reporte.actions.isEmpty()) {
                Text("Sin acciones registradas.", style = MaterialTheme.typography.bodySmall)
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            if (canManage && reporte.status != "CERRADO" && reporte.status != "DESCARTADO") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Button(
                    onClick = { dialogo = "asignar" },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Asignar responsable") }
                Button(
                    onClick = { dialogo = "cerrar" },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text("Cerrar hallazgo") }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) { Text("Volver") }
        }
    }

    if (dialogo == "cerrar") {
        var nota by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text("Cerrar hallazgo") },
            text = {
                Column {
                    Text(
                        "Describe la acción correctiva aplicada. Esto queda como evidencia ante una inspección.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = nota,
                        onValueChange = { nota = it },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                container.sgsstRepository.close(reportId, nota)
                                dialogo = null
                                recargar()
                            } catch (e: Exception) {
                                error = mensajeDeError(e)
                                dialogo = null
                            }
                        }
                    },
                    enabled = nota.isNotBlank(),
                ) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogo = null }) { Text("Cancelar") }
            },
        )
    }

    if (dialogo == "asignar") {
        val (usuarios, _) = rememberAsync(Unit) { container.sgsstRepository.users() }
        var elegido by remember { mutableStateOf<UserDto?>(null) }
        var nota by remember { mutableStateOf("") }
        var abierto by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text("Asignar responsable") },
            text = {
                Column {
                    val cargaUsuarios = usuarios.value
                    val lista: List<UserDto> =
                        if (cargaUsuarios is Async.Ok) cargaUsuarios.data else emptyList()
                    ExposedDropdownMenuBox(
                        expanded = abierto,
                        onExpandedChange = { abierto = it },
                    ) {
                        OutlinedTextField(
                            value = elegido?.let { "${it.firstName} ${it.lastName}".trim() } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Responsable") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = abierto,
                            onDismissRequest = { abierto = false },
                        ) {
                            lista.forEach { usuario ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${usuario.firstName} ${usuario.lastName}".trim()
                                                .ifBlank { usuario.username }
                                        )
                                    },
                                    onClick = {
                                        elegido = usuario
                                        abierto = false
                                    },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = nota,
                        onValueChange = { nota = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val usuario = elegido ?: return@TextButton
                        scope.launch {
                            try {
                                container.sgsstRepository.assign(reportId, usuario.id, nota)
                                dialogo = null
                                recargar()
                            } catch (e: Exception) {
                                error = mensajeDeError(e)
                                dialogo = null
                            }
                        }
                    },
                    enabled = elegido != null,
                ) { Text("Asignar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogo = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Text(
        "$etiqueta: $valor",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
