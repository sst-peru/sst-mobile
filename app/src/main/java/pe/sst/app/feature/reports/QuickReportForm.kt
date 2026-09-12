@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pe.sst.app.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Variante "rapido" del experimento: tres pasos y listo.
 *
 * Paso 1: ¿acto o condición?  Paso 2: categoría (iconos grandes).  Paso 3: foto y enviar.
 * La descripción escrita es opcional a propósito: con guantes y casco, escribir es la
 * barrera que hace que el operario no reporte.
 */
@Composable
fun QuickReportForm(
    state: NewReportState,
    viewModel: NewReportViewModel,
    onCancel: () -> Unit,
) {
    var step by remember { mutableIntStateOf(1) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Paso $step de 3", style = MaterialTheme.typography.labelMedium)

        when (step) {
            1 -> {
                Text(
                    "¿Qué viste?",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                BigChoice("Una condición insegura", "Algo del ambiente: piso mojado, cable pelado, andamio roto") {
                    viewModel.onKind("CONDICION")
                    step = 2
                }
                BigChoice("Un acto inseguro", "Algo que alguien está haciendo mal: sin casco, sin arnés") {
                    viewModel.onKind("ACTO")
                    step = 2
                }
            }

            2 -> {
                Text(
                    "¿De qué tipo?",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                val options = state.categories.filter { it.kind == state.kind }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(options) { category ->
                        BigChoice(category.name, null) {
                            viewModel.onCategory(category.id)
                            step = 3
                        }
                    }
                }
                OutlinedButton(onClick = { step = 3 }, modifier = Modifier.fillMaxWidth()) {
                    Text("No estoy seguro, continuar")
                }
            }

            3 -> {
                Text(
                    "Una foto y listo",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                PhotoPicker(
                    photoPath = state.photoPath,
                    onPhoto = viewModel::onPhoto,
                    onLocation = viewModel::onLocation,
                )
                Text("¿Qué tan grave es?", modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BAJA", "MEDIA", "ALTA", "CRITICA").forEach { level ->
                        FilterChip(
                            selected = state.severity == level,
                            onClick = { viewModel.onSeverity(level) },
                            label = { Text(level) },
                        )
                    }
                }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(56.dp),
                ) {
                    Text(if (state.saving) "Guardando…" else "Enviar reporte")
                }
                Text(
                    "Si no hay señal, el reporte se guarda en el celular y se envía solo cuando vuelva la conexión.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        OutlinedButton(
            onClick = { if (step > 1) step-- else onCancel() },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text(if (step > 1) "Atrás" else "Cancelar")
        }
    }
}

@Composable
private fun BigChoice(title: String, subtitle: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
