package com.yourssu.signal.handler.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.yourssu.signal.handler.Error
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class ErrorResponse(
    val timestamp: String = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    val status: Int,
    val message: String,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val code: String? = null,
) {
    companion object {
        fun from(e: Error): ErrorResponse {
            return ErrorResponse(
                status = e.status.value(),
                message = e.message,
                code = e.code,
            )
        }
    }

}
