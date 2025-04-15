package com.yourssu.ssugaeting.domain.viewer.business.dto

import com.yourssu.ssugaeting.domain.viewer.implement.domain.Viewer

data class ViewerResponse(
    val id: Long? = null,
    val uuid: String,
    val ticket: Int,
    val usedTicket: Int,
    val updatedTime: String,
) {
    companion object {
        fun from(viewer: Viewer): ViewerResponse {
            return ViewerResponse(
                id = viewer.id,
                uuid = viewer.uuid.value,
                ticket = viewer.ticket,
                usedTicket = viewer.usedTicket,
                updatedTime = viewer.updatedTime.toString(),
            )
        }
    }
}
