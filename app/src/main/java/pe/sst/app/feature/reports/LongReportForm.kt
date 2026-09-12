package pe.sst.app.feature.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Variante "largo" del experimento: el formulario tradicional, con todos los campos visibles
 * y obligatorios. Es el grupo de control: representa lo que usan hoy muchas empresas.
 */
@Composable
fun LongReportForm(
    state: NewReportState,
    viewModel: NewReportViewModel,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Nuevo reporte", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Complete todos los campos",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Dropdown(
            label = "Tipo de reporte",
            options = listOf("CONDICION" to "Condición insegura", "ACTO" to "Acto inseguro"),
            selected = state.kind,
            onSelect = viewModel::onKind,
        )

        Dropdown(
            label = "Área",
            options = state.areas.map { it.id.toString() to it.name },
            selected = state.areaId?.toString(),
            onSelect = { viewModel.onArea(it.toIntOrNull()) },
        )

        Dropdown(
            label = "Categoría del peligro",
            options = state.categories.filter { it.kind == state.kind }
                .map { it.id.toString() to it.name },
            selected = state.categoryId?.toString(),
            onSelect = { viewModel.onCategory(it.toIntOrNull()) },
        )

        Dropdown(
            label = "Severidad",
            options = listOf(
                "BAJA" to "Baja", "MEDIA" to "Media", "ALTA" to "Alta", "CRITICA" to "Crítica"
            ),
            selected = state.severity,
            onSelect = viewModel::onSeverity,
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescription,
            label = { Text("Descripción detallada del hallazgo") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        Text("Evidencia fotográfica", modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
        PhotoPicker(
            photoPath = state.photoPath,
            onPhoto = viewModel::onPhoto,
            onLocation = viewModel::onLocation,
        )

        Button(
            onClick = viewModel::save,
            enabled = !state.saving && state.areaId != null && state.description.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Text(if (state.saving) "Guardando…" else "Registrar reporte")
        }

        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Cancelar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(
    label: String,
    options: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}
