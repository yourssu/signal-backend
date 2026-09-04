package com.yourssu.signal.api.meeting

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.MeetingRoom
import com.yourssu.signal.domain.meeting.implement.MeetingRoomRepository
import com.yourssu.signal.domain.meeting.implement.MeetingRoomStatus
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.meeting.storage.MeetingMatchJpaRepository
import com.yourssu.signal.domain.meeting.storage.MeetingMemberJpaRepository
import com.yourssu.signal.domain.meeting.storage.MeetingRoomJpaRepository
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.profile.implement.ProfileRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MeetingApiIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var profileRepository: ProfileRepository
    @Autowired lateinit var meetingRoomJpaRepository: MeetingRoomJpaRepository
    @Autowired lateinit var meetingRoomRepository: MeetingRoomRepository
    @Autowired lateinit var meetingMemberJpaRepository: MeetingMemberJpaRepository
    @Autowired lateinit var meetingMatchJpaRepository: MeetingMatchJpaRepository

    @BeforeEach
    fun cleanMeetingData() {
        meetingMatchJpaRepository.deleteAll()
        meetingMemberJpaRepository.deleteAll()
        meetingRoomJpaRepository.deleteAll()
    }

    @Test
    fun `실제 JWT로 프로필 없는 신청자가 방 생성은 거절되고 매칭과 양측 결과 조회는 성공한다`() {
        val creator = register()
        val applicant = register()
        profileRepository.save(profile(creator.uuid, "@jwt_creator"))

        mockMvc.post("/api/meetings/rooms") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_2")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("PROFILE_REQUIRED") }
        }

        val createdBody = mockMvc.post("/api/meetings/rooms") {
            bearer(creator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_1")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.result.status") { value("OPEN") }
            jsonPath("$.result.creatorAnimal") { value("DOG") }
            jsonPath("$.result.expiresAt") { value(org.hamcrest.Matchers.endsWith("+09:00")) }
        }.andReturn().response.contentAsString
        val roomId = objectMapper.readTree(createdBody).path("result").path("id").asLong()

        mockMvc.get("/api/meetings/board") {
            bearer(applicant.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.slots", hasSize<Any>(7))
            jsonPath("$.result.slots[0].room.creatorAnimal") { value("DOG") }
            jsonPath("$.result.latestMatch") { doesNotExist() }
            jsonPath("$.result.slots[0].order") { doesNotExist() }
            jsonPath("$.result.slots[0].xRatio") { doesNotExist() }
            jsonPath("$.result.slots[0].yRatio") { doesNotExist() }
        }

        mockMvc.get("/api/meetings/rooms/$roomId") {
            bearer(applicant.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.room.creatorAnimal") { value("DOG") }
        }

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@jwt_applicant")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.result.status") { value("MATCHED") }
            jsonPath("$.result.counterpartContact") { value("@jwt_creator") }
        }

        mockMvc.get("/api/meetings/board") {
            bearer(applicant.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.latestMatch.roomId") { value(roomId) }
            jsonPath("$.result.latestMatch.creatorNickname") { value("방장-${creator.uuid.take(6)}") }
            jsonPath("$.result.latestMatch.creatorAnimal") { value("DOG") }
            jsonPath("$.result.latestMatch.matchedAt") { exists() }
            jsonPath("$.result.latestMatch.visibleUntil") { exists() }
            jsonPath("$.result.latestMatch.counterpartContact") { doesNotExist() }
        }

        mockMvc.get("/api/meetings/rooms/$roomId/result") {
            bearer(creator.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.counterpartContact") { value("@jwt_applicant") }
        }
        mockMvc.get("/api/meetings/rooms/$roomId/result") {
            bearer(applicant.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.counterpartContact") { value("@jwt_creator") }
        }
    }

    @Test
    fun `미팅 API는 토큰 없이는 접근할 수 없다`() {
        mockMvc.get("/api/meetings/board").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `동일 슬롯 동시 생성의 패자는 SLOT_ALREADY_OCCUPIED를 응답받는다`() {
        val first = register()
        val second = register()
        profileRepository.save(profile(first.uuid, "@jwt_slot_race_first"))
        profileRepository.save(profile(second.uuid, "@jwt_slot_race_second"))
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        try {
            val responses = listOf(first, second).associateWith { user ->
                executor.submit<ApiResult> {
                    ready.countDown()
                    start.await(5, TimeUnit.SECONDS)
                    mockMvc.post("/api/meetings/rooms") {
                        bearer(user.accessToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = roomCreateBody("SLOT_7")
                    }.andReturn().response.let { ApiResult(it.status, it.contentAsString) }
                }
            }
            check(ready.await(5, TimeUnit.SECONDS))
            start.countDown()
            val results = responses.mapValues { it.value.get(10, TimeUnit.SECONDS) }

            check(results.values.count { it.status == 201 } == 1)
            val (loser, conflict) = results.entries.single { it.value.status != 201 }
            check(conflict.status == 409)
            check(objectMapper.readTree(conflict.body).path("code").asText() == "SLOT_ALREADY_OCCUPIED")

            mockMvc.post("/api/meetings/rooms") {
                bearer(loser.accessToken)
                contentType = MediaType.APPLICATION_JSON
                content = roomCreateBody("SLOT_6")
            }.andExpect { status { isCreated() } }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `매칭 요청 검증 실패는 400을 응답한다`() {
        val creator = register()
        val applicant = register()
        profileRepository.save(profile(creator.uuid, "@jwt_validation_creator"))
        val createdBody = mockMvc.post("/api/meetings/rooms") {
            bearer(creator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_6")
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        val roomId = objectMapper.readTree(createdBody).path("result").path("id").asLong()

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("invalid contact")
        }.andExpect { status { isBadRequest() } }

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "representative":{"gender":"FEMALE","birthYear":2000,"department":"글로벌미디어학부"},
                  "contact":"010-1234-5678",
                  "companions":[]
                }
            """.trimIndent()
        }.andExpect { status { isBadRequest() } }

        mockMvc.get("/api/meetings/board") {
            bearer(applicant.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.latestMatch") { doesNotExist() }
        }
    }

    @Test
    fun `동행자가 없는 방 생성 요청은 일일 생성 기회를 소비하지 않는다`() {
        val creator = register()
        profileRepository.save(profile(creator.uuid, "@jwt_rollback_creator"))

        mockMvc.post("/api/meetings/rooms") {
            bearer(creator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "slot":"SLOT_7",
                  "invitation":"동행자 없음",
                  "companions":[]
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.post("/api/meetings/rooms") {
            bearer(creator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_8")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("INVALID_MEETING_SLOT") }
        }

        mockMvc.post("/api/meetings/rooms") {
            bearer(creator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_7")
        }.andExpect { status { isCreated() } }
    }

    @Test
    fun `양수가 아닌 방 ID는 400을 응답한다`() {
        val user = register()

        mockMvc.get("/api/meetings/rooms/0") {
            bearer(user.accessToken)
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `만료된 방의 신청과 취소는 EXPIRED 상태와 슬롯 반환을 커밋한다`() {
        val matchCreator = register()
        val cancelCreator = register()
        val applicant = register()
        profileRepository.save(profile(matchCreator.uuid, "@expired_match_creator"))
        profileRepository.save(profile(cancelCreator.uuid, "@expired_cancel_creator"))
        val matchRoom = expiredRoom(matchCreator.uuid, MeetingSlot.SLOT_5)
        val cancelRoom = expiredRoom(cancelCreator.uuid, MeetingSlot.SLOT_6)

        mockMvc.post("/api/meetings/rooms/${matchRoom.id}/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@expired_applicant")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("ROOM_EXPIRED") }
        }
        mockMvc.post("/api/meetings/rooms/${cancelRoom.id}/cancel") {
            bearer(cancelCreator.accessToken)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("ROOM_EXPIRED") }
        }

        listOf(matchRoom.id!!, cancelRoom.id!!).forEach { roomId ->
            val stored = meetingRoomJpaRepository.findById(roomId).orElseThrow()
            check(stored.status == MeetingRoomStatus.EXPIRED)
            check(stored.activeSlot == null)
        }
        check(meetingMatchJpaRepository.countByRoomId(matchRoom.id!!) == 0L)
    }

    @Test
    fun `방 생성은 선택 슬롯의 만료 방만 정리한다`() {
        val expiredCreator = register()
        val otherExpiredCreator = register()
        val newCreator = register()
        profileRepository.save(profile(newCreator.uuid, "@new_slot_creator"))
        val selected = expiredRoom(expiredCreator.uuid, MeetingSlot.SLOT_6)
        val other = expiredRoom(otherExpiredCreator.uuid, MeetingSlot.SLOT_7)

        mockMvc.post("/api/meetings/rooms") {
            bearer(newCreator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_6")
        }.andExpect { status { isCreated() } }

        check(meetingRoomJpaRepository.findById(selected.id!!).orElseThrow().status == MeetingRoomStatus.EXPIRED)
        check(meetingRoomJpaRepository.findById(other.id!!).orElseThrow().status == MeetingRoomStatus.OPEN)
    }

    @Test
    fun `방 생성과 성공 매칭은 사용자당 하루 한 번만 허용한다`() {
        val creator = register()
        val otherCreator = register()
        val applicant = register()
        profileRepository.save(profile(creator.uuid, "@daily_creator"))
        profileRepository.save(profile(otherCreator.uuid, "@daily_other_creator"))
        profileRepository.save(profile(applicant.uuid, "@daily_applicant"))
        val roomId = createRoom(creator, "SLOT_1")

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@daily_applicant")
        }.andExpect { status { isCreated() } }

        mockMvc.post("/api/meetings/rooms") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody("SLOT_2")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("DAILY_MEETING_LIMIT_EXCEEDED") }
        }

        val otherRoomId = createRoom(otherCreator, "SLOT_3")
        mockMvc.post("/api/meetings/rooms/$otherRoomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@daily_applicant")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("DAILY_MEETING_LIMIT_EXCEEDED") }
        }

        mockMvc.post("/api/meetings/rooms/$otherRoomId/matches") {
            bearer(creator.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@daily_creator")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("DAILY_MEETING_LIMIT_EXCEEDED") }
        }

        mockMvc.get("/api/meetings/board") {
            bearer(applicant.accessToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.result.creationEligibility.canCreate") { value(false) }
            jsonPath("$.result.creationEligibility.reason") { value("DAILY_MEETING_LIMIT_EXCEEDED") }
        }
        check(meetingMatchJpaRepository.countByRoomId(otherRoomId) == 0L)
    }

    @Test
    fun `실패한 신청은 하루 참여 기회를 소비하지 않는다`() {
        val creator = register()
        val applicant = register()
        profileRepository.save(profile(creator.uuid, "@retry_creator"))
        val roomId = createRoom(creator, "SLOT_4")

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("invalid contact")
        }.andExpect { status { isBadRequest() } }

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(applicant.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@retry_applicant")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.result.status") { value("MATCHED") }
        }
    }

    @Test
    fun `열린 방을 만들어 둔 사용자는 다른 방에 신청할 수 없다`() {
        val creator = register()
        val holder = register()
        profileRepository.save(profile(creator.uuid, "@active_room_creator"))
        openRoom(holder.uuid, MeetingSlot.SLOT_5, LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1))
        val roomId = createRoom(creator, "SLOT_6")

        mockMvc.post("/api/meetings/rooms/$roomId/matches") {
            bearer(holder.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = matchBody("@active_room_holder")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("ACTIVE_ROOM_EXISTS") }
        }
        check(meetingMatchJpaRepository.countByRoomId(roomId) == 0L)
    }

    @Test
    fun `관리자 취소는 JWT 없이 어드민 키로만 동작하고 슬롯을 반환한다`() {
        val creator = register()
        profileRepository.save(profile(creator.uuid, "@admin-cancel"))
        val roomId = createRoom(creator, "SLOT_1")

        mockMvc.post("/api/meetings/rooms/$roomId/admin-cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"secretKey":"test-admin-access-key"}"""
        }.andExpect { status { isNoContent() } }

        check(meetingRoomJpaRepository.findById(roomId).get().status == MeetingRoomStatus.CANCELLED)

        val other = register()
        profileRepository.save(profile(other.uuid, "@slot-reuse"))
        createRoom(other, "SLOT_1")
    }

    @Test
    fun `관리자 취소는 키가 틀리거나 비면 방을 건드리지 않고 거절한다`() {
        val creator = register()
        profileRepository.save(profile(creator.uuid, "@admin-cancel-denied"))
        val roomId = createRoom(creator, "SLOT_2")

        mockMvc.post("/api/meetings/rooms/$roomId/admin-cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"secretKey":"wrong-key"}"""
        }.andExpect { status { isForbidden() } }

        mockMvc.post("/api/meetings/rooms/$roomId/admin-cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"secretKey":""}"""
        }.andExpect { status { isBadRequest() } }

        check(meetingRoomJpaRepository.findById(roomId).get().status == MeetingRoomStatus.OPEN)
    }

    @Test
    fun `관리자 취소는 없는 방과 이미 취소된 방을 기존 오류로 거절한다`() {
        mockMvc.post("/api/meetings/rooms/999999/admin-cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"secretKey":"test-admin-access-key"}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.code") { value("MEETING_ROOM_NOT_FOUND") }
        }

        val creator = register()
        profileRepository.save(profile(creator.uuid, "@admin-cancel-twice"))
        val roomId = createRoom(creator, "SLOT_4")
        mockMvc.post("/api/meetings/rooms/$roomId/admin-cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"secretKey":"test-admin-access-key"}"""
        }.andExpect { status { isNoContent() } }

        mockMvc.post("/api/meetings/rooms/$roomId/admin-cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"secretKey":"test-admin-access-key"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("ROOM_CANCELLED") }
        }
    }

    private fun createRoom(user: RegisteredUser, slot: String): Long {
        val body = mockMvc.post("/api/meetings/rooms") {
            bearer(user.accessToken)
            contentType = MediaType.APPLICATION_JSON
            content = roomCreateBody(slot)
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        return objectMapper.readTree(body).path("result").path("id").asLong()
    }

    private fun register(): RegisteredUser {
        val body = mockMvc.post("/api/auth/register")
            .andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        val result = objectMapper.readTree(body).path("result")
        val jwt = result.path("accessToken").asText()
        val parts = jwt.split('.')
        val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
        return RegisteredUser(objectMapper.readTree(payload).path("sub").asText(), jwt)
    }

    private fun profile(uuid: String, contact: String) = Profile(
        uuid = Uuid(uuid),
        gender = Gender.MALE,
        department = "컴퓨터학부",
        birthYear = 2000,
        animal = Animal.DOG,
        contact = contact,
        mbti = "ENFP",
        nickname = "방장-${uuid.take(6)}",
        introSentences = emptyList(),
        school = "숭실대학교",
    )

    private fun openRoom(uuid: String, slot: MeetingSlot, creationDate: LocalDate): MeetingRoom {
        val zone = ZoneId.of("Asia/Seoul")
        return meetingRoomRepository.save(
            MeetingRoom(
                slot = slot,
                activeSlot = slot,
                creatorUuid = Uuid(uuid),
                creatorAnimal = Animal.DOG,
                partySize = 2,
                invitation = "진행 중인 방",
                status = MeetingRoomStatus.OPEN,
                creationDate = creationDate,
                expiresAt = LocalDateTime.now(zone).plusHours(1),
            )
        )
    }

    private fun expiredRoom(uuid: String, slot: MeetingSlot): MeetingRoom {
        val zone = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(zone)
        return meetingRoomRepository.save(
            MeetingRoom(
                slot = slot,
                activeSlot = slot,
                creatorUuid = Uuid(uuid),
                creatorAnimal = Animal.DOG,
                partySize = 2,
                invitation = "만료 방",
                status = MeetingRoomStatus.OPEN,
                creationDate = LocalDate.now(zone),
                expiresAt = now.minusSeconds(1),
            )
        )
    }

    private fun roomCreateBody(slot: String) = """
        {
          "slot":"$slot",
          "invitation":"같이 놀아요",
          "companions":[{"gender":"MALE","birthYear":2001,"department":"경영학부"}]
        }
    """.trimIndent()

    private fun matchBody(contact: String) = """
        {
          "representative":{"gender":"FEMALE","birthYear":2000,"department":"글로벌미디어학부"},
          "contact":"$contact",
          "companions":[{"gender":"FEMALE","birthYear":2001,"department":"경영학부"}]
        }
    """.trimIndent()

    private fun org.springframework.test.web.servlet.MockHttpServletRequestDsl.bearer(token: String) {
        header("Authorization", "Bearer $token")
    }

    data class RegisteredUser(val uuid: String, val accessToken: String)
    data class ApiResult(val status: Int, val body: String)
}
