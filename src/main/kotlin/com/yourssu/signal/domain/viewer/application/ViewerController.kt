package com.yourssu.signal.domain.viewer.application

import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.viewer.application.dto.*
import com.yourssu.signal.domain.viewer.business.ViewerService
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import com.yourssu.signal.infrastructure.TicketSseManager
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/viewers")
class ViewerController(
    private val viewerService: ViewerService,
    private val ticketSseManager: TicketSseManager,
) {
    @PostMapping("/verification")
    @RequireAuth
    fun issueVerification(@UserUuid uuid: String): ResponseEntity<Response<VerificationResponse>> {
        val response = viewerService.issueVerificationCode(uuid)
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping
    fun issueTicket(@Valid @RequestBody request: TicketIssuedRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        ticketSseManager.notifyTicketIssued(response)
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping("/sms")
    fun issueTicketByBankDepositSms(@Valid @RequestBody request: BankDepositSmsRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        ticketSseManager.notifyTicketIssued(response)
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/tickets/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @RequireAuth
    fun subscribeToTicketEvents(@Valid @ModelAttribute request: FoundSelfRequest): SseEmitter {
        return ticketSseManager.streamTicketEvents(request.toCommand())
    }

    @GetMapping("/uuid")
    @RequireAuth
    fun getViewer(@UserUuid uuid: String): ResponseEntity<Response<ViewerDetailResponse>> {
        val response = viewerService.getViewer(uuid)
        return ResponseEntity.ok(Response(result = response))
    }

    @Profile("!prod")
    @GetMapping
    fun findAllViewers(@Valid @ModelAttribute request: ViewersFoundRequest): ResponseEntity<Response<List<ViewerResponse>>> {
        val response = viewerService.findAllViewers(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping("/deposit")
    @RequireAuth
    fun notifyDeposit(@Valid @RequestBody request: NotificationDepositRequest): ResponseEntity<Response<ViewerResponse>?> {
        val response = viewerService.issueTicketByDepositName(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }
}
