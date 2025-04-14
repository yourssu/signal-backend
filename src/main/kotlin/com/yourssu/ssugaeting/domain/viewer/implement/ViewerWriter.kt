package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class ViewerWriter(
    private val viewerRepository: ViewerRepository,
) {
    fun issueTicket(uuid: Uuid, ticket: Int): Viewer {
        return viewerRepository.save(findOrCreateViewer(uuid, ticket))
    }

    private fun findOrCreateViewer(uuid: Uuid, ticket: Int): Viewer {
        if (viewerRepository.existsByUuid(uuid)) {
            return viewerRepository.findByUuid(uuid)
        }
        return Viewer(uuid = uuid, ticket = ticket)
    }
}
