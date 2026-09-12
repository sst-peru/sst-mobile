package pe.sst.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.sst.app.BuildConfig
import pe.sst.app.data.local.TokenStore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun create(tokens: TokenStore): ApiService {
        // Cliente sin interceptor de auth, solo para renovar el token (evita recursión).
        val plain = retrofit(OkHttpClient.Builder().build()).create(ApiService::class.java)

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokens) { plain })
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                    )
                }
            }
            // Timeouts cortos: en campo, mejor fallar rápido y dejar el reporte en la cola
            // local que dejar al operario esperando la rueda girando.
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return retrofit(client).create(ApiService::class.java)
    }

    private fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
