package com.yourssu.ssugaeting.domain.viewer.application

import com.yourssu.ssugaeting.domain.Response
import com.yourssu.ssugaeting.domain.viewer.application.dto.TicketCreatedRequest
import com.yourssu.ssugaeting.domain.viewer.application.dto.VerificationRequest
import com.yourssu.ssugaeting.domain.viewer.application.dto.ViewersFoundRequest
import com.yourssu.ssugaeting.domain.viewer.business.ViewerService
import com.yourssu.ssugaeting.domain.viewer.business.dto.VerificationResponse
import com.yourssu.ssugaeting.domain.viewer.business.dto.ViewerResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/viewers")
class ViewerController(
    private val viewerService: ViewerService,
) {
    @GetMapping("/verification")
    fun issueVerification(@Valid @ModelAttribute request: VerificationRequest) : ResponseEntity<Response<VerificationResponse>> {
        val response = viewerService.issueVerification(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping
    fun createTicket(@Valid @RequestBody request: TicketCreatedRequest) : ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping
    fun findAllViewers(@Valid @ModelAttribute request: ViewersFoundRequest) : ResponseEntity<Response<List<ViewerResponse>>> {
        val response = viewerService.findAllViewers()
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/me")
    fun getMyInfo(@Valid @ModelAttribute request: MyInfoRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.getMyInfo()
        return ResponseEntity.ok(Response(result = response))
    }
}
