package pe.sst.app

import android.app.Application
import androidx.room.Room
import pe.sst.app.data.local.SstDatabase
import pe.sst.app.data.local.TokenStore
import pe.sst.app.data.remote.ApiClient
import pe.sst.app.data.repository.AuthRepository
import pe.sst.app.data.repository.ReportRepository
import pe.sst.app.sync.ReportSyncWorker

/**
 * Inyección de dependencias a mano. Para el tamaño de este proyecto es más claro
 * que meter Hilt: un solo lugar donde se construye todo.
 */
class AppContainer(application: Application) {

    private val database = Room.databaseBuilder(
        application, SstDatabase::class.java, "sst.db"
    ).build()

    val tokenStore = TokenStore(application)
    private val api = ApiClient.create(tokenStore)

    val reportRepository = ReportRepository(api, database.pendingReports())
    val authRepository = AuthRepository(api, tokenStore)
}

class SstApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Si quedaron reportes sin subir de la sesión anterior, se intentan ahora.
        ReportSyncWorker.enqueue(this)
    }
}
