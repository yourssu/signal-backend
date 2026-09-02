package com.yourssu.signal.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportMeetingOpenApiContractTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `api-docs는 신고와 미팅 API의 태그와 설명을 제공한다`() {
        val response = mockMvc.get("/api-docs").andExpect { status { isOk() } }.andReturn().response.contentAsString
        val document = objectMapper.readTree(response)
        val tags = document.path("tags").associateBy { it.path("name").asText() }
        val paths = document.path("paths")

        tags.getValue("Report").path("description").asText() shouldBe "신고 접수 및 승인 API"
        tags.getValue("Meeting").path("description").asText() shouldBe "미팅 방 생성, 참여 및 결과 조회 API"

        mapOf(
            "/api/reports" to "신고 접수",
            "/api/reports/{reportId}/approve" to "신고 승인",
            "/api/meetings/board" to "미팅 보드 조회",
            "/api/meetings/rooms" to "미팅 방 생성",
            "/api/meetings/rooms/{roomId}" to "미팅 방 상세 조회",
            "/api/meetings/rooms/{roomId}/cancel" to "미팅 방 취소",
            "/api/meetings/rooms/{roomId}/matches" to "미팅 방 참여 및 매칭",
            "/api/meetings/rooms/{roomId}/result" to "미팅 결과 조회",
        ).forEach { (path, summary) ->
            val method = if (path == "/api/meetings/board" || path.endsWith("{roomId}") || path.endsWith("result")) {
                "get"
            } else {
                "post"
            }
            paths.path(path).path(method).path("summary").asText() shouldBe summary
        }
    }
}
