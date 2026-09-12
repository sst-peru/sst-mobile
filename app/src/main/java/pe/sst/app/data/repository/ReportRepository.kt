package pe.sst.app.data.repository

import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.sst.app.data.local.PendingReport
import pe.sst.app.data.local.PendingReportDao
import pe.sst.app.data.remote.ApiService
import pe.sst.app.data.remote.AreaDto
import pe.sst.app.data.remote.CategoryDto
import pe.sst.app.data.remote.ReportDto
import java.io.File

class ReportRepository(
    private val api: ApiService,
    private val dao: PendingReportDao,
) {

    fun observeLocal(): Flow<List<PendingReport>> = dao.observeAll()

    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    /** Guarda el reporte en el celular. La subida la hace el worker de sincronización. */
    suspend fun enqueue(report: PendingReport) = dao.insert(report)

    suspend fun remoteReports(status: String? = null): List<ReportDto> =
        api.reports(status).results

    suspend fun areas(): List<AreaDto> = api.areas().results

    suspend fun categories(): List<CategoryDto> = api.categories().results

    /**
     * Sube los reportes pendientes. Devuelve true si la cola quedó vacía.
     *
     * Un fallo de red deja el reporte en la cola para el siguiente intento; un rechazo del
     * servidor (400) también queda registrado con su error, para no reintentar en bucle
     * algo que nunca va a pasar.
     */
    suspend fun syncPending(): Boolean {
        val pending = dao.pending()
        var allOk = true
        for (report in pending) {
            try {
                api.createReport(
                    clientUuid = report.clientUuid.toPart(),
                    kind = report.kind.toPart(),
                    severity = report.severity.toPart(),
                    description = report.description.toPart(),
                    occurredAt = report.occurredAtIso.toPart(),
                    formVariant = report.formVariant.toPart(),
                    syncedOffline = "true".toPart(),
                    area = report.areaId?.toString()?.toPart(),
                    category = report.categoryId?.toString()?.toPart(),
                    latitude = report.latitude?.toString()?.toPart(),
                    longitude = report.longitude?.toString()?.toPart(),
                    photo = report.photoPath?.let { path ->
                        val file = File(path)
                        if (!file.exists()) return@let null
                        MultipartBody.Part.createFormData(
                            "photo",
                            file.name,
                            file.asRequestBody("image/jpeg".toMediaTypeOrNull()),
                        )
                    },
                )
                dao.markUploaded(report.clientUuid)
            } catch (error: Exception) {
                dao.markFailed(report.clientUuid, error.message)
                allOk = false
            }
        }
        return allOk
    }

    private fun String.toPart(): RequestBody =
        toRequestBody("text/plain".toMediaTypeOrNull())
}
