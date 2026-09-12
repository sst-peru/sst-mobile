package pe.sst.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "sst_session")

/**
 * Guarda los tokens JWT. El refresh token dura 30 días a propósito: el operario puede
 * pasar varios días en obra o mina sin señal y no debería tener que volver a loguearse.
 */
class TokenStore(private val context: Context) {

    private val accessKey = stringPreferencesKey("access")
    private val refreshKey = stringPreferencesKey("refresh")
    private val variantKey = stringPreferencesKey("form_variant")

    suspend fun save(access: String, refresh: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[accessKey] = access
            if (refresh != null) prefs[refreshKey] = refresh
        }
    }

    suspend fun saveVariant(variant: String) {
        context.dataStore.edit { it[variantKey] = variant }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun access(): String? = context.dataStore.data.first()[accessKey]

    suspend fun refresh(): String? = context.dataStore.data.first()[refreshKey]

    suspend fun variant(): String? = context.dataStore.data.first()[variantKey]

    /** Lectura síncrona para el interceptor de OkHttp, que no es suspend. */
    fun accessBlocking(): String? = runBlocking { access() }

    fun refreshBlocking(): String? = runBlocking { refresh() }
}
