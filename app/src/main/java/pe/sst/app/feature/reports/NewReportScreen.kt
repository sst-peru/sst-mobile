package pe.sst.app.feature.reports

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.sst.app.AppContainer

/**
 * Punto de entrada del formulario de reporte.
 *
 * Aquí vive el experimento A/B: la variante que trae el backend decide si el operario ve el
 * flujo corto (3-4 taps) o el formulario largo tradicional. El resto de la app es idéntico
 * para los dos grupos, así la única diferencia medida es el formulario.
 */
@Composable
fun NewReportScreen(
    container: AppContainer,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: NewReportViewModel = viewModel(
        factory = NewReportViewModel.factory(application, container)
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    when (state.variant) {
        "largo" -> LongReportForm(state = state, viewModel = viewModel, onCancel = onCancel)
        else -> QuickReportForm(state = state, viewModel = viewModel, onCancel = onCancel)
    }
}
