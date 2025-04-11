package com.yourssu.ssugaeting.domain.viewer.implement

import java.time.ZonedDateTime

class Viewer(
    val id: Long? = null,
    val uuid: String,
    val ticket: Int = 0,
    val usedTicket: Int = 0,
    val updatedDate: ZonedDateTime,
) {
}
