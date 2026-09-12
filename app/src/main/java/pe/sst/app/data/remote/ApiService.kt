package pe.sst.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login/")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @GET("auth/me/")
    suspend fun me(): UserDto

    @GET("auth/areas/")
    suspend fun areas(): Paginated<AreaDto>

    @GET("categories/")
    suspend fun categories(): Paginated<CategoryDto>

    @GET("reports/")
    suspend fun reports(@Query("status") status: String? = null): Paginated<ReportDto>

    /**
     * Sube un reporte con su foto.
     *
     * El client_uuid lo genera la app antes de enviar: si la subida falla y se reintenta,
     * el backend reconoce el uuid y devuelve el reporte existente en vez de duplicarlo.
     */
    @Multipart
    @POST("reports/")
    suspend fun createReport(
        @Part("client_uuid") clientUuid: RequestBody,
        @Part("kind") kind: RequestBody,
        @Part("severity") severity: RequestBody,
        @Part("description") description: RequestBody,
        @Part("occurred_at") occurredAt: RequestBody,
        @Part("form_variant") formVariant: RequestBody,
        @Part("synced_offline") syncedOffline: RequestBody,
        @Part("area") area: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part photo: MultipartBody.Part?,
    ): ReportDto

    @GET("experiments/my-variant/")
    suspend fun myVariant(@Query("key") key: String = "report_form"): AssignmentDto
}
