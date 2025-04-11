package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import java.time.ZonedDateTime

class Viewer(
    val id: Long? = null,
    val uuid: Uuid,
    var ticket: Int = 0,
    var usedTicket: Int = 0,
    val updatedTime: ZonedDateTime,
) {
}
