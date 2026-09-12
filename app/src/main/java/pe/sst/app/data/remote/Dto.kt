package pe.sst.app.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val access: String,
    val refresh: String,
    val user: UserDto,
)

data class RefreshRequest(val refresh: String)

data class RefreshResponse(val access: String)

data class UserDto(
    val id: Int,
    val username: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val role: String,
    @SerializedName("company_name") val companyName: String?,
    val area: Int?,
    @SerializedName("area_name") val areaName: String?,
)

data class AreaDto(val id: Int, val name: String)

data class CategoryDto(val id: Int, val name: String, val kind: String)

data class ReportDto(
    val id: Int,
    @SerializedName("client_uuid") val clientUuid: String,
    val kind: String,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("area_name") val areaName: String?,
    val description: String,
    val severity: String,
    val photo: String?,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("closed_at") val closedAt: String?,
)

data class Paginated<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>,
)

data class AssignmentDto(
    @SerializedName("experiment_key") val experimentKey: String,
    val variant: String,
)
