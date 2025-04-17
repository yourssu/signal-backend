package com.yourssu.signal.domain.viewer.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class ViewerFoundCommand(
    val uuid: String
) {
    fun toDomain(): Uuid {
        return Uuid(
            value = uuid,
        )
    }
}
