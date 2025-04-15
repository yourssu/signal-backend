package com.yourssu.ssugaeting.domain.profile.business

import com.yourssu.ssugaeting.domain.common.implement.Uuid

class TicketConsumedCommand(
    val profileId: Long,
    val uuid: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}
