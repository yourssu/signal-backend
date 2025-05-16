package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.viewer.implement.domain.Viewer
import org.springframework.stereotype.Component

@Component
class ViewerReader(
    private val viewerRepository: ViewerRepository,
) {
    fun get(uuid: Uuid): Viewer {
        return viewerRepository.getByUuid(uuid)
    }

    fun existsByUuid(uuid: Uuid): Boolean {
        return viewerRepository.existsByUuid(uuid)
    }

    fun findAll(): List<Viewer> {
        return viewerRepository.findAll()
    }
}
