package pe.sst.app.data.repository

import pe.sst.app.data.local.TokenStore
import pe.sst.app.data.remote.ApiService
import pe.sst.app.data.remote.LoginRequest
import pe.sst.app.data.remote.RegisterRequest
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

    /** Registro público: el API fuerza el rol OPERARIO, el cliente no lo manda. */
    suspend fun register(
        username: String,
        password: String,
        passwordConfirm: String,
        firstName: String,
        lastName: String,
        dni: String,
        phone: String,
        companyRuc: String,
    ): UserDto = api.register(
        RegisterRequest(
            username = username,
            password = password,
            passwordConfirm = passwordConfirm,
            firstName = firstName,
            lastName = lastName,
            dni = dni,
            phone = phone,
            companyRuc = companyRuc,
        )
    )

    suspend fun isLoggedIn(): Boolean = tokens.access() != null

    suspend fun currentVariant(): String = tokens.variant() ?: "rapido"

    suspend fun logout() = tokens.clear()

    suspend fun me(): UserDto = api.me()
}
