package com.yourssu.signal.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.meeting.implement.MeetingTeamSide
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.EgenTeto
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.MbtiCompatibilityTable
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

    @Test
    fun `api-docs는 신고와 미팅 요청의 실제 예시와 validation을 제공한다`() {
        val response = mockMvc.get("/api-docs").andExpect { status { isOk() } }.andReturn().response.contentAsString
        val schemas = objectMapper.readTree(response).path("components").path("schemas")
        val room = schemas.path("MeetingRoomCreateRequest").path("properties")
        val member = schemas.path("MeetingMemberRequest").path("properties")
        val match = schemas.path("MeetingMatchRequest").path("properties")
        val report = schemas.path("ReportCreatedRequest").path("properties")
        val approval = schemas.path("ReportApprovedRequest").path("properties")

        room.path("slot").path("enum").map { it.asText() } shouldBe MeetingSlot.selectableEntries.map { it.name }
        room.path("slot").path("example").asText() shouldBe "SLOT_1"
        room.path("invitation").path("minLength").asInt() shouldBe 1
        room.path("invitation").path("maxLength").asInt() shouldBe 500
        room.path("invitation").path("pattern").asText() shouldBe ".*\\S.*"
        room.path("companions").path("minItems").asInt() shouldBe 1
        room.path("companions").path("maxItems").asInt() shouldBe 3

        member.path("gender").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder(Gender.entries.map { it.name })
        member.path("gender").path("example").asText() shouldBe "MALE"
        member.path("birthYear").path("minimum").asLong() shouldBe ProfileValidationPolicy.MIN_BIRTH_YEAR
        member.path("birthYear").path("maximum").asInt() shouldBe ProfileValidationPolicy.maximumBirthYear()
        member.path("birthYear").path("example").asInt() shouldBe 2001
        member.path("department").path("minLength").asInt() shouldBe ProfileValidationPolicy.MIN_DEPARTMENT_LENGTH
        member.path("department").path("maxLength").asInt() shouldBe ProfileValidationPolicy.MAX_DEPARTMENT_LENGTH

        match.path("contact").path("pattern").asText() shouldBe ProfileValidationPolicy.CONTACT_PATTERN
        match.path("contact").path("example").asText() shouldBe "@signal_user"
        report.path("profileId").path("minimum").asInt() shouldBe 1
        report.path("profileId").path("example").asLong() shouldBe 1L
        approval.path("secretKey").path("minLength").asInt() shouldBe 1
        approval.path("secretKey").path("pattern").asText() shouldBe ".*\\S.*"

        val roomResponse = schemas.path("MeetingRoomResponse").path("properties")
        roomResponse.path("id").path("example").asLong() shouldBe 1L
        roomResponse.path("partySize").path("minimum").asInt() shouldBe 2
        roomResponse.path("partySize").path("maximum").asInt() shouldBe 4
        roomResponse.path("partySize").path("example").asInt() shouldBe 2
        roomResponse.path("creatorAnimal").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder(Animal.entries.map { it.name })
        roomResponse.path("creatorAnimal").path("example").asText() shouldBe "DOG"
        roomResponse.path("creatorNickname").path("type").asText() shouldBe "string"
        schemas.path("MeetingRoomSummaryResponse").path("properties").path("creatorAnimal").path("enum")
            .map { it.asText() }.shouldContainExactlyInAnyOrder(Animal.entries.map { it.name })
        schemas.path("MeetingLatestMatchResponse").path("properties").path("creatorAnimal").path("enum")
            .map { it.asText() }.shouldContainExactlyInAnyOrder(Animal.entries.map { it.name })
        val myRoom = schemas.path("MeetingMyRoomResponse").path("properties")
        myRoom.path("status").path("enum").map { it.asText() }.shouldContainExactlyInAnyOrder(listOf("OPEN", "MATCHED"))
        myRoom.path("teamSide").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder(MeetingTeamSide.entries.map { it.name })
        listOf("MeetingRoomResponse", "MeetingSlotResponse").forEach { schemaName ->
            schemas.path(schemaName).path("properties").path("slot").path("enum").map { it.asText() } shouldBe
                MeetingSlot.selectableEntries.map { it.name }
        }
        schemas.path("ErrorResponse").path("properties").path("status").path("example").asInt() shouldBe 400
        schemas.path("ResponseMeetingRoomResponse").path("properties").path("timestamp").path("format").asText() shouldBe
            "date-time"
    }

    @Test
    fun `api-docs는 외부 도메인 값과 보드 상태를 제공한다`() {
        val response = mockMvc.get("/api-docs").andExpect { status { isOk() } }.andReturn().response.contentAsString
        val document = objectMapper.readTree(response)
        val schemas = document.path("components").path("schemas")

        listOf("DeckRequest", "RandomProfileRequest").forEach { schemaName ->
            schemas.path(schemaName).path("properties").path("gender").path("enum").map { it.asText() }
                .shouldContainExactlyInAnyOrder(Gender.entries.map { it.name })
        }
        listOf("ProfileResponse", "MyProfileResponse", "ProfileContactResponse", "ProfileRankingResponse")
            .forEach { schemaName ->
                val properties = schemas.path(schemaName).path("properties")
                properties.path("gender").path("enum").map { it.asText() }
                    .shouldContainExactlyInAnyOrder(Gender.entries.map { it.name })
                properties.path("animal").path("enum").map { it.asText() }
                    .shouldContainExactlyInAnyOrder(Animal.entries.map { it.name })
                properties.path("mbti").path("enum").map { it.asText() }
                    .shouldContainExactlyInAnyOrder(MbtiCompatibilityTable.validTypes())
                properties.path("egenTeto").path("enum").map { it.asText() }
                    .shouldContainExactlyInAnyOrder(EgenTeto.entries.map { it.name })
            }
        schemas.path("BankDepositSmsRequest").path("properties").path("type").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder("kakao_sms", "kb_sms")
        schemas.path("MeetingCreationEligibilityResponse").path("properties").path("reason").path("enum")
            .map { it.asText() }
            .shouldContainExactlyInAnyOrder(
                "PROFILE_REQUIRED",
                "MEETING_BLOCKED",
                "DAILY_CREATION_LIMIT_EXCEEDED",
                "DAILY_MEETING_LIMIT_EXCEEDED",
                "ACTIVE_ROOM_EXISTS",
            )

        val genderParameter = document.path("paths").path("/api/profiles/genders/{gender}/count").path("get")
            .path("parameters").first { it.path("name").asText() == "gender" }
        genderParameter.path("schema").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder(Gender.entries.map { it.name })
        val smsTypeParameter = document.path("paths").path("/api/viewers/sms/{type}/{secretKey}").path("post")
            .path("parameters").first { it.path("name").asText() == "type" }
        smsTypeParameter.path("schema").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder("kakao_sms", "kb_sms")
    }

    @Test
    fun `api-docs는 신고와 미팅 API의 실제 성공과 실패 상태를 제공한다`() {
        val response = mockMvc.get("/api-docs").andExpect { status { isOk() } }.andReturn().response.contentAsString
        val paths = objectMapper.readTree(response).path("paths")

        mapOf(
            "/api/reports" to ("post" to setOf("201", "400", "401", "403", "404", "409")),
            "/api/reports/{reportId}/approve" to ("post" to setOf("200", "400", "403", "404", "409")),
            "/api/meetings/board" to ("get" to setOf("200", "401")),
            "/api/meetings/rooms" to ("post" to setOf("201", "400", "401", "403", "409")),
            "/api/meetings/rooms/{roomId}" to ("get" to setOf("200", "400", "401", "404", "409")),
            "/api/meetings/rooms/{roomId}/matches" to ("post" to setOf("201", "400", "401", "403", "404", "409")),
            "/api/meetings/rooms/{roomId}/cancel" to ("post" to setOf("204", "400", "401", "403", "404", "409")),
            "/api/meetings/rooms/{roomId}/result" to ("get" to setOf("200", "400", "401", "403", "404", "409")),
        ).forEach { (path, methodAndCodes) ->
            val (method, codes) = methodAndCodes
            paths.path(path).path(method).path("responses").fieldNames().asSequence().toSet() shouldBe codes
        }

        paths.path("/api/meetings/rooms").path("post").path("responses").path("400")
            .path("content").path("application/json").path("schema").path("${'$'}ref").asText() shouldBe
            "#/components/schemas/ErrorResponse"
        paths.path("/api/meetings/rooms").path("post").path("responses").path("401")
            .path("content").path("application/json").path("example").path("status").asInt() shouldBe 401
        paths.path("/api/meetings/rooms").path("post").path("responses").path("409")
            .path("content").path("application/json").path("example").path("code").asText() shouldBe
            "SLOT_ALREADY_OCCUPIED"
        paths.path("/api/reports").path("post").path("responses").path("409")
            .path("content").path("application/json").path("example").has("code") shouldBe false
        paths.path("/api/meetings/rooms/{roomId}").path("get").path("parameters")[0]
            .path("schema").path("minimum").asInt() shouldBe 1
        paths.path("/api/reports/{reportId}/approve").path("post").path("parameters")[0]
            .path("schema").path("minimum").asInt() shouldBe 1
    }
}
