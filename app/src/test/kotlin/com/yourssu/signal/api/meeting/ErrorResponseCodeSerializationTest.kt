package com.yourssu.signal.api.meeting

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.handler.ConflictException
import com.yourssu.signal.handler.dto.ErrorResponse
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class ErrorResponseCodeSerializationTest : DescribeSpec({
    val objectMapper = ObjectMapper()

    describe("오류 코드 직렬화") {
        it("코드가 없으면 JSON에서 code를 제외한다") {
            val json = objectMapper.writeValueAsString(ErrorResponse(status = 400, message = "잘못된 요청"))

            json shouldNotContain "code"
        }

        it("코드가 있으면 JSON에 포함한다") {
            val response = ErrorResponse.from(
                ConflictException(
                    message = "프로필 등록이 필요합니다.",
                    code = "PROFILE_REQUIRED",
                )
            )
            val json = objectMapper.writeValueAsString(response)

            json shouldContain "\"code\":\"PROFILE_REQUIRED\""
        }
    }
})
