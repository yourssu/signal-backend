package com.yourssu.ssugaeting.domain.common.business.dto

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Response<T>(
    val timestamp: String = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    val result: T,
)
