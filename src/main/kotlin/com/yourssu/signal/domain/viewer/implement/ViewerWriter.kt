package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ViewerWriter(
    private val viewerRepository: ViewerRepository,
) {
    @Transactional
    fun issueTicket(uuid: Uuid, ticket: Int): Viewer {
        if (viewerRepository.existsByUuid(uuid)) {
            val viewer = viewerRepository.getByUuid(uuid)
            return addTicket(viewer, ticket)
        }
        return createViewer(uuid, ticket)
    }

    private fun addTicket(viewer: Viewer, ticket: Int): Viewer {
        return viewerRepository.updateTicket(viewer.addTicket(ticket))
    }

    private fun createViewer(uuid: Uuid, ticket: Int): Viewer {
        return viewerRepository.save(Viewer(uuid = uuid, ticket = ticket, updatedTime = null))
    }
}
