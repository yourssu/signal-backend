package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.viewer.implement.domain.Viewer
import org.springframework.stereotype.Component

@Component
class ViewerReader(
    private val viewerRepository: ViewerRepository,
) {
    fun get(uuid: Uuid): Viewer {
        return viewerRepository.getByUuid(uuid)
    }

    fun findAll(): List<Viewer> {
        return viewerRepository.findAll()
    }
}
