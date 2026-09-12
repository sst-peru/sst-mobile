package pe.sst.app.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import pe.sst.app.data.local.TokenStore

/** Agrega el Bearer token y, si el backend responde 401, renueva el token y reintenta una vez. */
class AuthInterceptor(
    private val tokens: TokenStore,
    private val refreshService: () -> ApiService,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val access = tokens.accessBlocking()
        val request = chain.request().newBuilder().apply {
            if (access != null) header("Authorization", "Bearer $access")
        }.build()

        val response = chain.proceed(request)
        if (response.code != 401) return response

        val refreshToken = tokens.refreshBlocking() ?: return response
        val newAccess = runCatching {
            runBlocking { refreshService().refresh(RefreshRequest(refreshToken)).access }
        }.getOrNull() ?: return response

        runBlocking { tokens.save(newAccess) }
        response.close()
        return chain.proceed(
            chain.request().newBuilder().header("Authorization", "Bearer $newAccess").build()
        )
    }
}
