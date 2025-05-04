package com.yourssu.signal.infrastructure

import com.yourssu.signal.domain.verification.implement.domain.Verification
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object Notification {
    fun notifyTicketIssued(verification: Verification, ticket: Int) {
        logger.info { "&${verification.verificationCode.value} $ticket"}
    }
}
