package com.yourssu.ssugaeting.domain.viewer.business

import com.yourssu.ssugaeting.domain.profile.business.dto.PurchasedProfileResponse
import com.yourssu.ssugaeting.domain.profile.implement.GenderValidator
import com.yourssu.ssugaeting.domain.profile.implement.PurchasedProfileReader
import com.yourssu.ssugaeting.domain.verification.implement.VerificationWriter
import com.yourssu.ssugaeting.domain.viewer.business.command.AllViewersFoundCommand
import com.yourssu.ssugaeting.domain.viewer.business.command.TicketIssuedCommand
import com.yourssu.ssugaeting.domain.viewer.business.command.VerificationCommand
import com.yourssu.ssugaeting.domain.viewer.business.command.ViewerFoundCommand
import com.yourssu.ssugaeting.domain.viewer.business.dto.VerificationResponse
import com.yourssu.ssugaeting.domain.viewer.business.dto.ViewerDetailResponse
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
    private val genderValidator: GenderValidator,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val adminAccessChecker: AdminAccessChecker,
) {
    fun issueVerificationCode(command: VerificationCommand): VerificationResponse {
        genderValidator.validateViewer(uuid = command.toUuid(), gender = command.toGender())
        val code = verificationWriter.issueVerificationCode(uuid = command.toUuid(), command.toGender()!!)
        return VerificationResponse.from(code)
    }

    fun issueTicket(command: TicketIssuedCommand): ViewerResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val verification = verificationReader.findByCode(command.toVerificationCode())
        val viewer = viewerWriter.issueTicket(
            uuid = verification.uuid,
            ticket = command.ticket,
            gender = verification.gender)
        verificationWriter.remove(verification.uuid)
        return ViewerResponse.from(viewer)
    }

    fun getViewer(command: ViewerFoundCommand): ViewerDetailResponse {
        val viewer = viewerReader.get(command.toDomain())
        val purchasedProfiles = purchasedProfileReader.findByViewerId(viewer.id!!)
            .map { PurchasedProfileResponse.from(it) }
        return ViewerDetailResponse.from(viewer, purchasedProfiles)
    }

    fun findAllViewers(command: AllViewersFoundCommand): List<ViewerResponse> {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val viewers = viewerReader.findAll()
        return viewers.map { ViewerResponse.from(it) }
    }
}
