package com.yourssu.signal.infrastructure

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/api/notification")
class PayNotification {
    @PostMapping
    fun payNotification(@RequestParam("message") message: String): ResponseEntity<String> {
        Notification.notifyDeposit(message)
        return ResponseEntity.ok(message)
    }
}
