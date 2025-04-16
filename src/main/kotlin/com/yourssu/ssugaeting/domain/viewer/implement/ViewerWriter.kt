package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Gender
import com.yourssu.ssugaeting.domain.viewer.implement.domain.Viewer
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ViewerWriter(
    private val viewerRepository: ViewerRepository,
) {
    @Transactional
    fun issueTicket(uuid: Uuid, ticket: Int, gender: Gender): Viewer {
        if (viewerRepository.existsByUuid(uuid)) {
            val viewer = viewerRepository.getByUuid(uuid)
            return addTicket(viewer, ticket)
        }
        return createViewer(uuid, ticket, gender)
    }

    private fun addTicket(viewer: Viewer, ticket: Int): Viewer {
        return viewerRepository.updateTicket(viewer.addTicket(ticket))
    }

    private fun createViewer(uuid: Uuid, ticket: Int, gender: Gender): Viewer {
        return viewerRepository.save(Viewer(uuid = uuid, ticket = ticket, gender = gender))
    }
}
