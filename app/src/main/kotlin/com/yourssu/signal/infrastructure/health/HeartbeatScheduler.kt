package com.yourssu.signal.infrastructure.health

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class HeartbeatScheduler {
    private val logger = LoggerFactory.getLogger("com.yourssu.signal.application.Heartbeat")

    @Scheduled(fixedRateString = "PT1M")
    fun heartbeat() {
        logger.info("SIGNAL_HEARTBEAT {}", Instant.now())
    }
}
