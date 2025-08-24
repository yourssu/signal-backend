package com.yourssu.signal.domain.user

import com.yourssu.signal.domain.common.implement.Uuid

data class User(
    val uuid: Uuid = Uuid.randomUUID(),
)
