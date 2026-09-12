package pe.sst.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Reporte guardado en el celular esperando subida.
 *
 * Todo reporte entra primero acá. Así la app nunca pierde un reporte por falta de señal
 * y el operario ve "guardado" de inmediato, sin esperar a la red.
 */
@Entity(tableName = "pending_reports")
data class PendingReport(
    @PrimaryKey val clientUuid: String,
    val kind: String,
    val severity: String,
    val description: String,
    val areaId: Int?,
    val categoryId: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    val occurredAtIso: String,
    val formVariant: String,
    val attempts: Int = 0,
    val lastError: String? = null,
    val uploaded: Boolean = false,
)

@Dao
interface PendingReportDao {

    @Insert
    suspend fun insert(report: PendingReport)

    @Query("SELECT * FROM pending_reports WHERE uploaded = 0 ORDER BY occurredAtIso")
    suspend fun pending(): List<PendingReport>

    @Query("SELECT * FROM pending_reports ORDER BY occurredAtIso DESC")
    fun observeAll(): Flow<List<PendingReport>>

    @Query("SELECT COUNT(*) FROM pending_reports WHERE uploaded = 0")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE pending_reports SET uploaded = 1 WHERE clientUuid = :uuid")
    suspend fun markUploaded(uuid: String)

    @Query("UPDATE pending_reports SET attempts = attempts + 1, lastError = :error WHERE clientUuid = :uuid")
    suspend fun markFailed(uuid: String, error: String?)
}

@Database(entities = [PendingReport::class], version = 1)
abstract class SstDatabase : RoomDatabase() {
    abstract fun pendingReports(): PendingReportDao
}
