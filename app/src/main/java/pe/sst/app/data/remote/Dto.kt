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

// ---------------------------------------------------------------------------
// Registro
// ---------------------------------------------------------------------------

data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("password_confirm") val passwordConfirm: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val dni: String,
    val phone: String,
    @SerializedName("company_ruc") val companyRuc: String,
)

// ---------------------------------------------------------------------------
// Seguimiento de hallazgos
// ---------------------------------------------------------------------------

data class ReportActionDto(
    val id: Int,
    val note: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("created_at") val createdAt: String,
)

/** Detalle completo de un reporte tal como lo devuelve el API. */
data class ReportDetailDto(
    val id: Int,
    val kind: String,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("area_name") val areaName: String?,
    val description: String,
    val severity: String,
    val photo: String?,
    val latitude: String?,
    val longitude: String?,
    val status: String,
    @SerializedName("assigned_to") val assignedTo: Int?,
    @SerializedName("assigned_to_name") val assignedToName: String?,
    @SerializedName("closure_note") val closureNote: String,
    @SerializedName("reported_by_name") val reportedByName: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("resolution_hours") val resolutionHours: Double?,
    @SerializedName("form_variant") val formVariant: String,
    @SerializedName("synced_offline") val syncedOffline: Boolean,
    val actions: List<ReportActionDto> = emptyList(),
)

data class AssignRequest(
    @SerializedName("assigned_to") val assignedTo: Int,
    val note: String = "",
)

data class CloseRequest(@SerializedName("closure_note") val closureNote: String)

// ---------------------------------------------------------------------------
// IPERC
// ---------------------------------------------------------------------------

data class IpercEntryDto(
    val id: Int,
    val matrix: Int,
    val area: Int,
    @SerializedName("area_name") val areaName: String,
    @SerializedName("job_position") val jobPosition: String,
    val hazard: String,
    val risk: String,
    val probability: Int,
    val consequence: Int,
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("existing_controls") val existingControls: String,
    @SerializedName("proposed_controls") val proposedControls: String,
    @SerializedName("source_report") val sourceReport: Int?,
)

data class IpercMatrixDto(
    val id: Int,
    val version: Int,
    val status: String,
    @SerializedName("entry_count") val entryCount: Int,
)

data class IpercEntryRequest(
    val matrix: Int,
    val area: Int,
    @SerializedName("job_position") val jobPosition: String,
    val hazard: String,
    val risk: String,
    val probability: Int,
    val consequence: Int,
    @SerializedName("existing_controls") val existingControls: String = "",
    @SerializedName("proposed_controls") val proposedControls: String = "",
)

// ---------------------------------------------------------------------------
// EPP
// ---------------------------------------------------------------------------

data class EppItemDto(
    val id: Int,
    val name: String,
    @SerializedName("lifespan_days") val lifespanDays: Int,
    val stock: Int,
)

data class EppDeliveryDto(
    val id: Int,
    val item: Int,
    @SerializedName("item_name") val itemName: String,
    val worker: Int,
    @SerializedName("worker_name") val workerName: String,
    val quantity: Int,
    @SerializedName("delivered_at") val deliveredAt: String,
    @SerializedName("expires_at") val expiresAt: String?,
    val acknowledged: Boolean,
    @SerializedName("is_expired") val isExpired: Boolean,
)

data class AcknowledgeRequest(val acknowledged: Boolean = true)

data class EppDeliveryRequest(
    val item: Int,
    val worker: Int,
    val quantity: Int = 1,
    val notes: String = "",
)

// ---------------------------------------------------------------------------
// Inspecciones
// ---------------------------------------------------------------------------

data class InspectionScheduleDto(
    val id: Int,
    val title: String,
    val area: Int,
    @SerializedName("area_name") val areaName: String,
    val checklist: List<String> = emptyList(),
    val frequency: String,
)

data class InspectionDto(
    val id: Int,
    val schedule: Int,
    val title: String,
    @SerializedName("area_name") val areaName: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("performed_at") val performedAt: String?,
    val status: String,
    val findings: String,
    @SerializedName("is_overdue") val isOverdue: Boolean,
)

data class CompleteInspectionRequest(
    val findings: String,
    val results: Map<String, Boolean>,
)

// ---------------------------------------------------------------------------
// Comité de SST
// ---------------------------------------------------------------------------

data class CommitteeMemberDto(
    val id: Int,
    @SerializedName("user_name") val userName: String,
    val role: String,
    val represents: String,
    @SerializedName("is_active") val isActive: Boolean,
)

data class AgreementDto(
    val id: Int,
    val description: String,
    @SerializedName("responsible_name") val responsibleName: String?,
    @SerializedName("due_date") val dueDate: String?,
    val status: String,
)

data class MeetingDto(
    val id: Int,
    val number: Int,
    val date: String,
    val place: String,
    @SerializedName("is_extraordinary") val isExtraordinary: Boolean,
    val agenda: String,
    val minutes: String,
    @SerializedName("attendee_count") val attendeeCount: Int,
    @SerializedName("quorum_reached") val quorumReached: Boolean,
    val agreements: List<AgreementDto> = emptyList(),
)

data class CommitteeDto(
    val id: Int,
    @SerializedName("period_start") val periodStart: String,
    @SerializedName("period_end") val periodEnd: String,
    @SerializedName("is_supervisor_mode") val isSupervisorMode: Boolean,
    val members: List<CommitteeMemberDto> = emptyList(),
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("quorum_required") val quorumRequired: Int,
    @SerializedName("is_paritario") val isParitario: Boolean,
)

data class CommitteeComplianceDto(
    @SerializedName("has_committee") val hasCommittee: Boolean,
    @SerializedName("meetings_total") val meetingsTotal: Int? = null,
    @SerializedName("meetings_with_quorum") val meetingsWithQuorum: Int? = null,
    @SerializedName("agreements_total") val agreementsTotal: Int? = null,
    @SerializedName("agreements_done") val agreementsDone: Int? = null,
    @SerializedName("agreements_pending") val agreementsPending: Int? = null,
    @SerializedName("agreements_compliance_pct") val agreementsCompliancePct: Double? = null,
)

data class MeetingRequest(
    val date: String,
    val place: String = "",
    val agenda: String = "",
    val minutes: String = "",
    @SerializedName("is_extraordinary") val isExtraordinary: Boolean = false,
    val attendees: List<Int> = emptyList(),
)

data class AgreementRequest(
    val meeting: Int,
    val description: String,
    @SerializedName("due_date") val dueDate: String? = null,
)

// ---------------------------------------------------------------------------
// Métricas
// ---------------------------------------------------------------------------

data class SeverityMttr(
    @SerializedName("mttr_hours") val mttrHours: Double,
    val closed: Int,
)

data class MttrDto(
    @SerializedName("window_days") val windowDays: Int,
    @SerializedName("closed_reports") val closedReports: Int,
    @SerializedName("open_reports") val openReports: Int,
    @SerializedName("mttr_hours") val mttrHours: Double?,
    @SerializedName("by_severity") val bySeverity: Map<String, SeverityMttr> = emptyMap(),
)

data class ComplianceDto(
    @SerializedName("window_days") val windowDays: Int,
    val scheduled: Int,
    val performed: Int,
    val pending: Int,
    val overdue: Int,
    @SerializedName("compliance_rate_pct") val complianceRatePct: Double?,
)
