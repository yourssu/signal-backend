package com.yourssu.ssugaeting.domain.viewer.business.dto

class ViewerResponse(
    val id: Long? = null,
    val ticketNumber: Int,
    val uuid: String,
    val ticket: Int,
    val usedTicket: Int,
    val updatedDate: String,
) {
}
