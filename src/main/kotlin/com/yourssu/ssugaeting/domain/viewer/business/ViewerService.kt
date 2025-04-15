package com.yourssu.ssugaeting.domain.viewer.business

import com.yourssu.ssugaeting.domain.verification.implement.VerificationWriter
import com.yourssu.ssugaeting.domain.viewer.business.command.AllViewersFoundCommand
import com.yourssu.ssugaeting.domain.viewer.business.command.TicketIssuedCommand
import com.yourssu.ssugaeting.domain.viewer.business.command.VerificationCommand
import com.yourssu.ssugaeting.domain.viewer.business.command.ViewerFoundCommand
import com.yourssu.ssugaeting.domain.viewer.business.dto.VerificationResponse
import com.yourssu.ssugaeting.domain.viewer.business.dto.ViewerResponse
import com.yourssu.ssugaeting.domain.viewer.implement.AdminAccessChecker
import com.yourssu.ssugaeting.domain.viewer.implement.VerificationReader
import com.yourssu.ssugaeting.domain.viewer.implement.ViewerReader
import com.yourssu.ssugaeting.domain.viewer.implement.ViewerWriter
import org.springframework.stereotype.Service

@Service
class ViewerService(
    private val verificationWriter: VerificationWriter,
    private val verificationReader: VerificationReader,
    private val viewerWriter: ViewerWriter,
    private val viewerReader: ViewerReader,
    private val adminAccessChecker: AdminAccessChecker,
) {
    fun issueVerificationCode(command: VerificationCommand): VerificationResponse {
        val code = verificationWriter.issueVerificationCode(command.toDomain())
        return VerificationResponse.from(code)
    }

    fun issueTicket(command: TicketIssuedCommand): ViewerResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val uuid = verificationReader.findByCode(command.toVerificationCode())
        val viewer = viewerWriter.issueTicket(uuid = uuid, ticket = command.ticket)
        verificationWriter.remove(uuid)
        return ViewerResponse.from(viewer)
    }

    fun getViewer(command: ViewerFoundCommand): ViewerResponse {
        val viewer = viewerReader.get(command.toDomain())
        return ViewerResponse.from(viewer)
    }

    fun findAllViewers(command: AllViewersFoundCommand): List<ViewerResponse> {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val viewers = viewerReader.findAll()
        return viewers.map { ViewerResponse.from(it) }
    }
}
