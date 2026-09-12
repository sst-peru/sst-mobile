package pe.sst.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
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

    // ----- Registro -----

    @POST("auth/register/")
    suspend fun register(@Body body: RegisterRequest): UserDto

    @GET("auth/users/")
    suspend fun users(): Paginated<UserDto>

    // ----- Seguimiento de hallazgos -----

    @GET("reports/{id}/")
    suspend fun report(@Path("id") id: Int): ReportDetailDto

    @POST("reports/{id}/assign/")
    suspend fun assignReport(@Path("id") id: Int, @Body body: AssignRequest): ReportDetailDto

    @POST("reports/{id}/close/")
    suspend fun closeReport(@Path("id") id: Int, @Body body: CloseRequest): ReportDetailDto

    // ----- IPERC -----

    @GET("iperc/matrices/")
    suspend fun ipercMatrices(): Paginated<IpercMatrixDto>

    @GET("iperc/entries/")
    suspend fun ipercEntries(): Paginated<IpercEntryDto>

    @POST("iperc/entries/")
    suspend fun createIpercEntry(@Body body: IpercEntryRequest): IpercEntryDto

    // ----- EPP -----

    @GET("epp/items/")
    suspend fun eppItems(): Paginated<EppItemDto>

    @GET("epp/deliveries/")
    suspend fun eppDeliveries(): Paginated<EppDeliveryDto>

    @POST("epp/deliveries/")
    suspend fun createEppDelivery(@Body body: EppDeliveryRequest): EppDeliveryDto

    @PATCH("epp/deliveries/{id}/")
    suspend fun acknowledgeEpp(
        @Path("id") id: Int,
        @Body body: AcknowledgeRequest,
    ): EppDeliveryDto

    // ----- Inspecciones -----

    @GET("inspections/schedules/")
    suspend fun inspectionSchedules(): Paginated<InspectionScheduleDto>

    @GET("inspections/")
    suspend fun inspections(): Paginated<InspectionDto>

    @POST("inspections/{id}/complete/")
    suspend fun completeInspection(
        @Path("id") id: Int,
        @Body body: CompleteInspectionRequest,
    ): InspectionDto

    // ----- Comité de SST -----

    @GET("committee/")
    suspend fun committees(): Paginated<CommitteeDto>

    @GET("committee/meetings/")
    suspend fun meetings(): Paginated<MeetingDto>

    @POST("committee/meetings/")
    suspend fun createMeeting(@Body body: MeetingRequest): MeetingDto

    @POST("committee/agreements/")
    suspend fun createAgreement(@Body body: AgreementRequest): AgreementDto

    @GET("committee-compliance/")
    suspend fun committeeCompliance(): CommitteeComplianceDto

    // ----- Métricas -----

    @GET("metrics/mttr/")
    suspend fun mttr(@Query("days") days: Int = 90): MttrDto

    @GET("metrics/inspection-compliance/")
    suspend fun inspectionCompliance(@Query("days") days: Int = 90): ComplianceDto
}
