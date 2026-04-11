package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface ViewerRepository {
    fun save(viewer: Viewer): Viewer
    fun existsByUuid(uuid: Uuid): Boolean
    fun getByUuid(uuid: Uuid): Viewer
    fun updateTicket(viewer: Viewer): Viewer
    fun updateUsedTicket(viewer: Viewer): Viewer
    fun findAll(): List<Viewer>
}
