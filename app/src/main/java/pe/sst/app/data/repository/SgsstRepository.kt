package pe.sst.app.data.repository

import pe.sst.app.data.remote.AcknowledgeRequest
import pe.sst.app.data.remote.AgreementRequest
import pe.sst.app.data.remote.ApiService
import pe.sst.app.data.remote.AssignRequest
import pe.sst.app.data.remote.CloseRequest
import pe.sst.app.data.remote.CommitteeComplianceDto
import pe.sst.app.data.remote.CommitteeDto
import pe.sst.app.data.remote.CompleteInspectionRequest
import pe.sst.app.data.remote.ComplianceDto
import pe.sst.app.data.remote.EppDeliveryDto
import pe.sst.app.data.remote.EppDeliveryRequest
import pe.sst.app.data.remote.EppItemDto
import pe.sst.app.data.remote.InspectionDto
import pe.sst.app.data.remote.InspectionScheduleDto
import pe.sst.app.data.remote.IpercEntryDto
import pe.sst.app.data.remote.IpercEntryRequest
import pe.sst.app.data.remote.IpercMatrixDto
import pe.sst.app.data.remote.MeetingDto
import pe.sst.app.data.remote.MeetingRequest
import pe.sst.app.data.remote.MttrDto
import pe.sst.app.data.remote.ReportDetailDto
import pe.sst.app.data.remote.UserDto

/**
 * Todo lo del SGSST que no es "crear un reporte": seguimiento de hallazgos, IPERC, EPP,
 * inspecciones, comité y métricas.
 *
 * Estas pantallas sí requieren conexión: a diferencia del reporte, que se guarda local y
 * sincroniza después, acá el operario está consultando o decidiendo sobre datos que viven
 * en el servidor y que otra persona pudo haber cambiado hace un minuto.
 */
class SgsstRepository(private val api: ApiService) {

    // ----- Usuarios -----
    suspend fun users(): List<UserDto> = api.users().results

    // ----- Hallazgos -----
    suspend fun report(id: Int): ReportDetailDto = api.report(id)

    suspend fun assign(id: Int, userId: Int, note: String): ReportDetailDto =
        api.assignReport(id, AssignRequest(userId, note))

    suspend fun close(id: Int, note: String): ReportDetailDto =
        api.closeReport(id, CloseRequest(note))

    // ----- IPERC -----
    suspend fun ipercMatrices(): List<IpercMatrixDto> = api.ipercMatrices().results

    suspend fun ipercEntries(): List<IpercEntryDto> = api.ipercEntries().results

    suspend fun createIpercEntry(request: IpercEntryRequest): IpercEntryDto =
        api.createIpercEntry(request)

    // ----- EPP -----
    suspend fun eppItems(): List<EppItemDto> = api.eppItems().results

    suspend fun eppDeliveries(): List<EppDeliveryDto> = api.eppDeliveries().results

    suspend fun createEppDelivery(request: EppDeliveryRequest): EppDeliveryDto =
        api.createEppDelivery(request)

    suspend fun acknowledgeEpp(id: Int): EppDeliveryDto =
        api.acknowledgeEpp(id, AcknowledgeRequest(true))

    // ----- Inspecciones -----
    suspend fun inspectionSchedules(): List<InspectionScheduleDto> =
        api.inspectionSchedules().results

    suspend fun inspections(): List<InspectionDto> = api.inspections().results

    suspend fun completeInspection(
        id: Int,
        findings: String,
        results: Map<String, Boolean>,
    ): InspectionDto = api.completeInspection(id, CompleteInspectionRequest(findings, results))

    // ----- Comité -----
    suspend fun committee(): CommitteeDto? = api.committees().results.firstOrNull()

    suspend fun meetings(): List<MeetingDto> = api.meetings().results

    suspend fun createMeeting(request: MeetingRequest): MeetingDto = api.createMeeting(request)

    suspend fun createAgreement(meetingId: Int, description: String, dueDate: String?) =
        api.createAgreement(AgreementRequest(meetingId, description, dueDate))

    suspend fun committeeCompliance(): CommitteeComplianceDto = api.committeeCompliance()

    // ----- Métricas -----
    suspend fun mttr(days: Int = 90): MttrDto = api.mttr(days)

    suspend fun inspectionCompliance(days: Int = 90): ComplianceDto =
        api.inspectionCompliance(days)
}
