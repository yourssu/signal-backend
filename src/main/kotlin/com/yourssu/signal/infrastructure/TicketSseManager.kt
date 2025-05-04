package com.yourssu.signal.infrastructure

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.viewer.business.command.ViewerFoundCommand
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

private const val EMITTER_TIMEOUT = 30_0000L

@Component
class TicketSseManager(
    val emitters: MutableMap<Uuid, SseEmitter> = ConcurrentHashMap()
) {
    fun streamTicketEvents(command: ViewerFoundCommand): SseEmitter {
        val uuid = command.toDomain()
        val emitter = emitters[uuid]
            ?: SseEmitter(EMITTER_TIMEOUT).apply { emitters[uuid] = this }
        emitter.onCompletion { emitters.remove(uuid) }
        emitter.onTimeout { emitters.remove(uuid) }
        emitter.onError { emitters.remove(uuid) }
        return emitter
    }

    @Async
    fun notifyTicketIssued(response: ViewerResponse) {
        val uuid = Uuid(response.uuid)
        val emitter = emitters[uuid] ?: return
        try {
            logger.info { "Sent ticket-issued event to uuid: $uuid" }
            emitter.complete()
        } catch (e: IOException) {
            logger.error { "Failed to send ticket-issued event to uuid: $uuid" }
            emitter.completeWithError(e)
        }
    }
}
