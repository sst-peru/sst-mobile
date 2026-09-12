package pe.sst.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import pe.sst.app.SstApplication

/**
 * Sube los reportes guardados en el celular cuando vuelve la red.
 *
 * WorkManager se encarga de esperar la conexión y de reintentar con backoff, así que la app
 * no tiene que vigilar el estado de la red por su cuenta. Es lo que hace que el flujo offline
 * funcione en obra o mina.
 */
class ReportSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as SstApplication).container.reportRepository
        return try {
            if (repository.syncPending()) Result.success() else Result.retry()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "sync-reportes"

        /** Se llama al guardar un reporte y al abrir la app. */
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<ReportSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        }
    }
}
