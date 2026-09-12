package pe.sst.app.data.repository

import pe.sst.app.data.local.TokenStore
import pe.sst.app.data.remote.ApiService
import pe.sst.app.data.remote.LoginRequest
import pe.sst.app.data.remote.UserDto

class AuthRepository(
    private val api: ApiService,
    private val tokens: TokenStore,
) {

    suspend fun login(username: String, password: String): UserDto {
        val response = api.login(LoginRequest(username, password))
        tokens.save(response.access, response.refresh)
        // La variante del A/B test se guarda al entrar, así el formulario funciona offline.
        runCatching { api.myVariant() }.getOrNull()?.let { tokens.saveVariant(it.variant) }
        return response.user
    }

    suspend fun isLoggedIn(): Boolean = tokens.access() != null

    suspend fun currentVariant(): String = tokens.variant() ?: "rapido"

    suspend fun logout() = tokens.clear()

    suspend fun me(): UserDto = api.me()
}
