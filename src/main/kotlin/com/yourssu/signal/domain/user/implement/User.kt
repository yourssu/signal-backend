package com.yourssu.signal.domain.user.implement

import com.yourssu.signal.domain.common.implement.Uuid

data class User(
    val id: Long? = null,
    val uuid: Uuid = Uuid.randomUUID(),
)
