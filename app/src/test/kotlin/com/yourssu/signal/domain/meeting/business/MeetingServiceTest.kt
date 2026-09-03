package com.yourssu.signal.domain.meeting.business

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.business.command.MeetingMemberCommand
import com.yourssu.signal.domain.meeting.business.command.MeetingMatchCommand
import com.yourssu.signal.domain.meeting.business.command.MeetingRoomCreateCommand
import com.yourssu.signal.domain.meeting.implement.*
import com.yourssu.signal.domain.profile.implement.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MeetingServiceTest : DescribeSpec({
    val roomRepository = mock<MeetingRoomRepository>()
    val memberRepository = mock<MeetingMemberRepository>()
    val matchRepository = mock<MeetingMatchRepository>()
    val profileReader = mock<ProfileReader>()
    val expirationManager = mock<MeetingExpirationManager>()
    val clock = AdjustableClock(Instant.parse("2026-08-31T03:00:00Z"), ZoneId.of("Asia/Seoul"))
    val service = MeetingService(roomRepository, memberRepository, matchRepository, profileReader, expirationManager, clock)
    val uuid = Uuid("creator")
    val applicant = Uuid("applicant")

    fun profile(uuid: Uuid) = Profile(
        uuid = uuid,
        gender = Gender.MALE,
        department = "컴퓨터학부",
        birthYear = 2000,
        animal = Animal.DOG,
        contact = "@creator",
        mbti = "ENFP",
        nickname = "방장",
        introSentences = emptyList(),
        school = "숭실대학교",
    )

    fun matchCommand() = MeetingMatchCommand(
        uuid = applicant.value,
        roomId = 1L,
        representative = MeetingMemberCommand(Gender.FEMALE, 2000, "컴퓨터학부"),
        contact = "@applicant",
        companions = listOf(MeetingMemberCommand(Gender.FEMALE, 2001, "경영학부")),
    )

    fun room(status: MeetingRoomStatus = MeetingRoomStatus.OPEN) = MeetingRoom(
        id = 1L,
        slot = MeetingSlot.SLOT_1,
        activeSlot = MeetingSlot.SLOT_1.takeIf { status == MeetingRoomStatus.OPEN },
        creatorUuid = uuid,
        creatorAnimal = Animal.DOG,
        partySize = 2,
        invitation = "초대",
        status = status,
        creationDate = LocalDate.of(2026, 8, 31),
        expiresAt = LocalDateTime.now(clock).plusHours(1),
        matchedAt = LocalDateTime.now(clock).takeIf { status == MeetingRoomStatus.MATCHED },
    )

    beforeEach {
        reset(roomRepository, memberRepository, matchRepository, profileReader, expirationManager)
        clock.set(Instant.parse("2026-08-31T03:00:00Z"))
    }

    describe("보드 조회") {
        it("프로필이 없으면 7개 슬롯과 PROFILE_REQUIRED 생성 불가 사유를 반환한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(false)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            doAnswer {
                clock.advanceSeconds(2)
                null
            }.whenever(expirationManager).expireDueRooms(any())

            val result = service.getBoard(uuid.value)

            result.slots shouldHaveSize 7
            result.creationEligibility.canCreate shouldBe false
            result.creationEligibility.reason shouldBe MeetingService.PROFILE_REQUIRED
            verify(expirationManager).expireDueRooms(LocalDateTime.of(2026, 8, 31, 12, 0))
            verify(roomRepository).findAllOpen(LocalDateTime.of(2026, 8, 31, 12, 0, 2))
        }

        it("오늘 매칭에 성공했으면 DAILY_MEETING_LIMIT_EXCEEDED 생성 불가 사유를 반환한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(true)

            val result = service.getBoard(uuid.value)

            result.creationEligibility.canCreate shouldBe false
            result.creationEligibility.reason shouldBe MeetingService.DAILY_MEETING_LIMIT_EXCEEDED
        }

        it("최근 30초 안에 매칭된 최신 방의 생성자 닉네임과 노출 기한을 반환한다") {
            val matchedAt = LocalDateTime.of(2026, 8, 31, 11, 59, 45)
            val matchedRoom = room(MeetingRoomStatus.MATCHED).copy(matchedAt = matchedAt)
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            whenever(roomRepository.findLatestMatchedAfter(LocalDateTime.of(2026, 8, 31, 11, 59, 30)))
                .thenReturn(matchedRoom)
            whenever(profileReader.getNicknameByUuid(uuid)).thenReturn("방장")

            val result = service.getBoard(uuid.value)

            result.latestMatch!!.roomId shouldBe 1L
            result.latestMatch!!.creatorNickname shouldBe "방장"
            result.latestMatch!!.matchedAt.toLocalDateTime() shouldBe matchedAt
            result.latestMatch!!.visibleUntil.toLocalDateTime() shouldBe matchedAt.plusSeconds(30)
            verify(profileReader, never()).getByUuid(uuid)
        }
    }

    describe("방 생성") {
        it("생성자와 동행 친구 수로 방 인원수를 계산하고 요청의 동행만 저장한다") {
            val profile = profile(uuid)
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile)
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31))).thenReturn(false)
            whenever(roomRepository.findOpenBySlot(eq(MeetingSlot.SLOT_1), any())).thenReturn(null)
            whenever(roomRepository.save(any())).thenAnswer { invocation ->
                invocation.getArgument<MeetingRoom>(0).copy(id = 1L)
            }
            whenever(memberRepository.saveAll(any())).thenAnswer { it.getArgument(0) }

            service.createRoom(
                MeetingRoomCreateCommand(
                    uuid = uuid.value,
                    slot = MeetingSlot.SLOT_1,
                    invitation = "같이 놀아요",
                    companions = listOf(
                        MeetingMemberCommand(Gender.MALE, 2001, "경영학부"),
                        MeetingMemberCommand(Gender.FEMALE, 2002, "경제학과"),
                    ),
                )
            )

            verify(roomRepository).save(check { it.partySize shouldBe 3 })
            val members = argumentCaptor<List<MeetingMember>>()
            verify(memberRepository).saveAll(members.capture())
            members.firstValue.map { it.userUuid } shouldBe listOf(uuid, null, null)
            verify(roomRepository).expireDueRoomInSlot(eq(MeetingSlot.SLOT_1), any())
            verify(expirationManager, never()).expireDueRooms(any())
        }

        it("오늘 매칭에 성공한 사용자는 방을 생성할 수 없다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(true)

            shouldThrow<DailyMeetingLimitExceededException> {
                service.createRoom(
                    MeetingRoomCreateCommand(
                        uuid.value,
                        MeetingSlot.SLOT_1,
                        "초대",
                        listOf(MeetingMemberCommand(Gender.MALE, 2000, "컴퓨터학부")),
                    )
                )
            }
            verify(roomRepository, never()).save(any())
        }

        it("프로필이 없으면 저장하지 않는다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(false)

            shouldThrow<ProfileRequiredException> {
                service.createRoom(
                    MeetingRoomCreateCommand(
                        uuid.value,
                        MeetingSlot.SLOT_1,
                        "초대",
                        listOf(MeetingMemberCommand(Gender.MALE, 2000, "컴퓨터학부")),
                    )
                )
            }
            verify(roomRepository, never()).save(any())
        }
    }

    describe("매칭 결과") {
        it("생성자와 신청자에게 각각 상대 연락처만 공개한다") {
            val room = room(status = MeetingRoomStatus.MATCHED)
            val match = MeetingMatch(1L, 1L, Uuid("applicant"), "@creator", "@applicant", LocalDateTime.now(clock))
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room)
            whenever(matchRepository.findByRoomId(1L)).thenReturn(match)

            service.getResult("creator", 1L).counterpartContact shouldBe "@applicant"
            service.getResult("applicant", 1L).counterpartContact shouldBe "@creator"
            shouldThrow<MeetingResultForbiddenException> { service.getResult("third-party", 1L) }
            verify(expirationManager, never()).expireDueRooms(any())
        }

        it("열린 방은 잠근 뒤 최신 시각으로 만료시킨다") {
            val expiredRoom = room().copy(expiresAt = LocalDateTime.of(2026, 8, 31, 11, 59, 59))
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(expiredRoom)
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument(0) }

            shouldThrow<RoomExpiredException> { service.getResult("creator", 1L) }

            verify(roomRepository).findByIdForUpdate(1L)
            verify(roomRepository).save(check { it.status shouldBe MeetingRoomStatus.EXPIRED })
            verify(matchRepository, never()).findByRoomId(any())
        }
    }

    describe("방 상세") {
        it("방과 멤버를 같은 잠금 경계에서 조회한다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(memberRepository.findAllByRoomId(1L)).thenReturn(emptyList())

            service.getRoom("viewer", 1L)

            verify(roomRepository).findByIdForUpdate(1L)
            verify(roomRepository, never()).findById(1L)
        }
    }

    describe("방 신청") {
        it("방 생성자는 자신의 방에 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())

            shouldThrow<SelfMatchNotAllowedException> {
                service.match(
                    MeetingMatchCommand(
                        uuid = uuid.value,
                        roomId = 1L,
                        representative = MeetingMemberCommand(Gender.MALE, 2000, "컴퓨터학부"),
                        contact = "@creator",
                        companions = listOf(MeetingMemberCommand(Gender.MALE, 2001, "경영학부")),
                    )
                )
            }
            verify(matchRepository, never()).save(any())
        }

        it("오늘 방을 생성한 사용자는 다른 방에 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(true)

            shouldThrow<DailyMeetingLimitExceededException> { service.match(matchCommand()) }

            verify(matchRepository, never()).save(any())
            verify(memberRepository, never()).saveAll(any())
        }

        it("오늘 매칭에 성공한 사용자는 다른 방에 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(true)

            shouldThrow<DailyMeetingLimitExceededException> { service.match(matchCommand()) }

            verify(matchRepository, never()).save(any())
            verify(roomRepository, never()).existsOpenByCreatorUuid(any(), any())
        }

        it("생성한 방이 열려 있는 동안에는 다른 방에 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsOpenByCreatorUuid(applicant, LocalDateTime.of(2026, 8, 31, 12, 0)))
                .thenReturn(true)

            shouldThrow<ActiveRoomExistsException> { service.match(matchCommand()) }

            verify(matchRepository, never()).save(any())
        }

        it("잠금 대기 중 만료되면 EXPIRED로 전환하고 신청을 거절한다") {
            val expiringRoom = room().copy(expiresAt = LocalDateTime.of(2026, 8, 31, 13, 0))
            clock.set(Instant.parse("2026-08-31T03:59:59Z"))
            whenever(roomRepository.findByIdForUpdate(1L)).thenAnswer {
                clock.advanceSeconds(2)
                expiringRoom
            }
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument(0) }

            shouldThrow<RoomExpiredException> {
                service.match(
                    MeetingMatchCommand(
                        uuid = "applicant",
                        roomId = 1L,
                        representative = MeetingMemberCommand(Gender.FEMALE, 2000, "컴퓨터학부"),
                        contact = "@applicant",
                        companions = listOf(MeetingMemberCommand(Gender.FEMALE, 2001, "경영학부")),
                    )
                )
            }

            verify(roomRepository).save(check { it.status shouldBe MeetingRoomStatus.EXPIRED })
            verify(matchRepository, never()).save(any())
        }
    }

    describe("방 취소") {
        it("잠금 대기 중 만료되면 EXPIRED로 전환하고 취소를 거절한다") {
            val expiringRoom = room().copy(expiresAt = LocalDateTime.of(2026, 8, 31, 13, 0))
            clock.set(Instant.parse("2026-08-31T03:59:59Z"))
            whenever(roomRepository.findByIdForUpdate(1L)).thenAnswer {
                clock.advanceSeconds(2)
                expiringRoom
            }
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument(0) }

            shouldThrow<RoomExpiredException> { service.cancel(uuid.value, 1L) }

            verify(roomRepository).save(check { it.status shouldBe MeetingRoomStatus.EXPIRED })
        }
    }
})

private class AdjustableClock(
    private var current: Instant,
    private val currentZone: ZoneId,
) : Clock() {
    override fun getZone(): ZoneId = currentZone

    override fun withZone(zone: ZoneId): Clock = AdjustableClock(current, zone)

    override fun instant(): Instant = current

    fun set(instant: Instant) {
        current = instant
    }

    fun advanceSeconds(seconds: Long) {
        current = current.plusSeconds(seconds)
    }
}
