package com.yourssu.ssugaeting.domain.viewer.business

import com.yourssu.ssugaeting.domain.common.implement.Uuid

class ViewerFoundCommand(
    val uuid: String
) {
    fun toDomain(): Uuid {
        return Uuid(
            value = uuid,
        )
    }
}
