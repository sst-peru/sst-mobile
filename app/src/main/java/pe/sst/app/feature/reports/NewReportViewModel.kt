package pe.sst.app.feature.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.sst.app.AppContainer
import pe.sst.app.data.local.PendingReport
import pe.sst.app.data.remote.AreaDto
import pe.sst.app.data.remote.CategoryDto
import pe.sst.app.sync.ReportSyncWorker
import java.time.Instant
import java.util.UUID

data class NewReportState(
    val variant: String = "rapido",
    val kind: String = "CONDICION",
    val severity: String = "MEDIA",
    val description: String = "",
    val areaId: Int? = null,
    val categoryId: Int? = null,
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val areas: List<AreaDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val saving: Boolean = false,
    val saved: Boolean = false,
)

class NewReportViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(NewReportState())
    val state: StateFlow<NewReportState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(variant = container.authRepository.currentVariant()) }
            // Los catálogos pueden fallar sin señal: el formulario rápido funciona sin ellos.
            runCatching { container.reportRepository.areas() }
                .onSuccess { areas -> _state.update { it.copy(areas = areas) } }
            runCatching { container.reportRepository.categories() }
                .onSuccess { cats -> _state.update { it.copy(categories = cats) } }
        }
    }

    fun onKind(value: String) = _state.update { it.copy(kind = value) }
    fun onSeverity(value: String) = _state.update { it.copy(severity = value) }
    fun onDescription(value: String) = _state.update { it.copy(description = value) }
    fun onArea(value: Int?) = _state.update { it.copy(areaId = value) }
    fun onCategory(value: Int?) = _state.update { it.copy(categoryId = value) }
    fun onPhoto(path: String?) = _state.update { it.copy(photoPath = path) }
    fun onLocation(lat: Double?, lon: Double?) =
        _state.update { it.copy(latitude = lat, longitude = lon) }

    /**
     * Guarda el reporte en el celular y dispara la sincronización.
     *
     * Nunca esperamos a la red para decirle al operario que su reporte quedó registrado:
     * eso es justo lo que hace que deje de reportar.
     */
    fun save() {
        val current = _state.value
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            container.reportRepository.enqueue(
                PendingReport(
                    clientUuid = UUID.randomUUID().toString(),
                    kind = current.kind,
                    severity = current.severity,
                    description = current.description,
                    areaId = current.areaId,
                    categoryId = current.categoryId,
                    latitude = current.latitude,
                    longitude = current.longitude,
                    photoPath = current.photoPath,
                    occurredAtIso = Instant.now().toString(),
                    formVariant = current.variant,
                )
            )
            ReportSyncWorker.enqueue(getApplication())
            _state.update { it.copy(saving = false, saved = true) }
        }
    }

    companion object {
        fun factory(application: Application, container: AppContainer) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NewReportViewModel(application, container) as T
            }
    }
}
