package com.yourssu.signal.domain.meeting.business

import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.business.command.MeetingMemberCommand
import com.yourssu.signal.domain.meeting.business.command.MeetingMatchCommand
import com.yourssu.signal.domain.meeting.business.command.MeetingRoomCreateCommand
import com.yourssu.signal.domain.meeting.business.dto.MeetingMyRoomResponse
import com.yourssu.signal.domain.meeting.implement.*
import com.yourssu.signal.domain.profile.implement.*
import com.yourssu.signal.domain.profile.implement.exception.BirthYearViolatedException
import com.yourssu.signal.domain.report.implement.ReportReader
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.exception.AdminPermissionDeniedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.ZoneId

class MeetingServiceTest : DescribeSpec({
    val roomRepository = mock<MeetingRoomRepository>()
    val memberRepository = mock<MeetingMemberRepository>()
    val matchRepository = mock<MeetingMatchRepository>()
    val profileReader = mock<ProfileReader>()
    val blacklistReader = mock<BlacklistReader>()
    val reportReader = mock<ReportReader>()
    val adminAccessChecker = mock<AdminAccessChecker>()
    val expirationManager = mock<MeetingExpirationManager>()
    val clock = AdjustableClock(Instant.parse("2026-08-31T03:00:00Z"), ZoneId.of("Asia/Seoul"))
    val service = MeetingService(roomRepository, memberRepository, matchRepository, profileReader, blacklistReader, reportReader, adminAccessChecker, expirationManager, clock)
    val uuid = Uuid("creator")
    val applicant = Uuid("applicant")

    fun profile(uuid: Uuid, id: Long = 1L) = Profile(
        id = id,
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
        reset(roomRepository, memberRepository, matchRepository, profileReader, blacklistReader, reportReader, adminAccessChecker, expirationManager)
        whenever(matchRepository.findRoomIdByApplicantUuidAndMatchedDate(any(), any())).thenReturn(null)
        whenever(profileReader.getNicknameByUuid(any())).thenReturn("방장")
        clock.set(Instant.parse("2026-08-31T03:00:00Z"))
        TransactionSynchronizationManager.initSynchronization()
    }

    afterEach {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
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

        it("블랙리스트에 등록됐으면 MEETING_BLOCKED 생성 불가 사유를 반환한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.findIdByUuid(uuid)).thenReturn(5L)
            whenever(blacklistReader.getAllBlacklistIds()).thenReturn(setOf(5L))
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())

            val result = service.getBoard(uuid.value)

            result.creationEligibility.canCreate shouldBe false
            result.creationEligibility.reason shouldBe MeetingService.MEETING_BLOCKED
            verify(profileReader, never()).getByUuid(uuid)
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

        it("생성한 방이 열려 있으면 ACTIVE_ROOM_EXISTS 생성 불가 사유를 반환한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsMatchedByCreatorUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsOpenByCreatorUuid(eq(uuid), any())).thenReturn(true)

            val result = service.getBoard(uuid.value)

            result.creationEligibility.canCreate shouldBe false
            result.creationEligibility.reason shouldBe MeetingService.ACTIVE_ROOM_EXISTS
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

        it("생성한 방이 열려 있으면 myRoom으로 OPEN 방장 정보를 반환한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(listOf(room()))
            whenever(roomRepository.findOpenByCreatorUuid(eq(uuid), any())).thenReturn(room())

            val result = service.getBoard(uuid.value)

            result.myRoom shouldBe MeetingMyRoomResponse(1L, MeetingRoomStatus.OPEN, MeetingTeamSide.CREATOR)
            verify(roomRepository, never()).findMatchedByCreatorUuidAndMatchedDate(any(), any())
            verify(matchRepository, never()).findRoomIdByApplicantUuidAndMatchedDate(any(), any())
        }

        it("오늘 만든 방이 매칭됐으면 myRoom으로 MATCHED 방장 정보를 반환한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            whenever(roomRepository.findOpenByCreatorUuid(eq(uuid), any())).thenReturn(null)
            whenever(roomRepository.findMatchedByCreatorUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(room(MeetingRoomStatus.MATCHED))

            val result = service.getBoard(uuid.value)

            result.myRoom shouldBe MeetingMyRoomResponse(1L, MeetingRoomStatus.MATCHED, MeetingTeamSide.CREATOR)
            verify(matchRepository, never()).findRoomIdByApplicantUuidAndMatchedDate(any(), any())
        }

        it("오늘 신청해 매칭됐으면 myRoom으로 MATCHED 신청자 정보를 반환한다") {
            whenever(profileReader.existsByUuid(applicant)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            whenever(roomRepository.findOpenByCreatorUuid(eq(applicant), any())).thenReturn(null)
            whenever(roomRepository.findMatchedByCreatorUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(null)
            whenever(matchRepository.findRoomIdByApplicantUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(7L)

            val result = service.getBoard(applicant.value)

            result.myRoom shouldBe MeetingMyRoomResponse(7L, MeetingRoomStatus.MATCHED, MeetingTeamSide.APPLICANT)
        }

        it("오늘 엮인 방이 없으면 myRoom은 null이다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())

            val result = service.getBoard(uuid.value)

            result.myRoom shouldBe null
        }

        it("어제 매칭된 방은 날짜가 바뀌면 myRoom에서 빠진다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.findAllOpen(any())).thenReturn(emptyList())
            whenever(roomRepository.findMatchedByCreatorUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(room(MeetingRoomStatus.MATCHED))
            clock.set(Instant.parse("2026-08-31T15:00:00Z"))

            val result = service.getBoard(uuid.value)

            result.myRoom shouldBe null
            verify(roomRepository).findMatchedByCreatorUuidAndMatchedDate(uuid, LocalDate.of(2026, 9, 1))
            verify(matchRepository).findRoomIdByApplicantUuidAndMatchedDate(uuid, LocalDate.of(2026, 9, 1))
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

        it("블랙리스트에 등록된 사용자는 방을 생성할 수 없다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid, id = 7L))
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31))).thenReturn(false)
            whenever(roomRepository.findOpenBySlot(eq(MeetingSlot.SLOT_1), any())).thenReturn(null)
            whenever(blacklistReader.existsByProfileId(7L)).thenReturn(true)

            shouldThrow<MeetingBlockedException> {
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

        it("승인된 신고의 연락처를 쓰는 프로필은 블랙리스트가 아니어도 방을 생성할 수 없다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid, id = 8L).copy(contact = "@reported"))
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31))).thenReturn(false)
            whenever(roomRepository.findOpenBySlot(eq(MeetingSlot.SLOT_1), any())).thenReturn(null)
            whenever(blacklistReader.existsByProfileId(8L)).thenReturn(false)
            whenever(reportReader.findApprovedContacts()).thenReturn(listOf("@Reported"))

            shouldThrow<MeetingBlockedException> {
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

        it("중복 연락처 정리로 블랙리스트에 오른 프로필과 같은 연락처여도 신고 이력이 없으면 방을 생성한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid, id = 11L))
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31))).thenReturn(false)
            whenever(roomRepository.findOpenBySlot(eq(MeetingSlot.SLOT_1), any())).thenReturn(null)
            whenever(blacklistReader.existsByProfileId(11L)).thenReturn(false)
            whenever(blacklistReader.getAllBlacklistIds()).thenReturn(setOf(10L))
            whenever(reportReader.findApprovedContacts()).thenReturn(emptyList())
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument<MeetingRoom>(0).copy(id = 1L) }
            whenever(memberRepository.saveAll(any())).thenAnswer { it.getArgument(0) }

            service.createRoom(
                MeetingRoomCreateCommand(
                    uuid.value,
                    MeetingSlot.SLOT_1,
                    "초대",
                    listOf(MeetingMemberCommand(Gender.MALE, 2000, "컴퓨터학부")),
                )
            )

            verify(roomRepository).save(any())
        }

        it("커밋 이후에 생성 알림을 1회 발행하도록 등록한다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid))
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31))).thenReturn(false)
            whenever(roomRepository.findOpenBySlot(eq(MeetingSlot.SLOT_1), any())).thenReturn(null)
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument<MeetingRoom>(0).copy(id = 1L) }
            whenever(memberRepository.saveAll(any())).thenAnswer { it.getArgument(0) }

            service.createRoom(
                MeetingRoomCreateCommand(
                    uuid.value,
                    MeetingSlot.SLOT_1,
                    "초대",
                    listOf(MeetingMemberCommand(Gender.MALE, 2001, "경영학부")),
                )
            )

            TransactionSynchronizationManager.getSynchronizations() shouldHaveSize 1
        }

        it("팀 구성 검증에 실패하면 생성 알림을 등록하지 않는다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid))
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31))).thenReturn(false)
            whenever(roomRepository.findOpenBySlot(eq(MeetingSlot.SLOT_1), any())).thenReturn(null)
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument<MeetingRoom>(0).copy(id = 1L) }

            shouldThrow<BirthYearViolatedException> {
                service.createRoom(
                    MeetingRoomCreateCommand(
                        uuid.value,
                        MeetingSlot.SLOT_1,
                        "초대",
                        listOf(MeetingMemberCommand(Gender.MALE, 1800, "경영학부")),
                    )
                )
            }

            verify(memberRepository, never()).saveAll(any())
            TransactionSynchronizationManager.getSynchronizations().shouldBeEmpty()
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

        it("방장으로 오늘 매칭에 성공한 사용자는 방을 생성할 수 없다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsMatchedByCreatorUuidAndMatchedDate(uuid, LocalDate.of(2026, 8, 31)))
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

        it("생성한 방이 열려 있으면 날짜가 바뀌어도 방을 더 만들 수 없다") {
            whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsOpenByCreatorUuid(uuid, LocalDateTime.of(2026, 8, 31, 12, 0)))
                .thenReturn(true)

            shouldThrow<ActiveRoomExistsException> {
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
            verify(roomRepository, never()).findOpenBySlot(any(), any())
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

        it("방 상세에 방장 프로필 닉네임을 담는다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(memberRepository.findAllByRoomId(1L)).thenReturn(emptyList())
            whenever(profileReader.getNicknameByUuid(uuid)).thenReturn("송하영")

            service.getRoom("viewer", 1L).room.creatorNickname shouldBe "송하영"
        }

        it("매칭된 방은 방장과 신청자에게 양 팀 구성을 계속 보여준다") {
            val members = listOf(
                MeetingMember(roomId = 1L, teamSide = MeetingTeamSide.CREATOR, memberOrder = 0, userUuid = uuid, gender = Gender.MALE, birthYear = 2000, department = "컴퓨터학부"),
                MeetingMember(roomId = 1L, teamSide = MeetingTeamSide.APPLICANT, memberOrder = 0, userUuid = applicant, gender = Gender.FEMALE, birthYear = 2001, department = "경영학부"),
                MeetingMember(roomId = 1L, teamSide = MeetingTeamSide.APPLICANT, memberOrder = 1, userUuid = null, gender = Gender.FEMALE, birthYear = 2002, department = "경영학부"),
            )
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room(MeetingRoomStatus.MATCHED))
            whenever(memberRepository.findAllByRoomId(1L)).thenReturn(members)

            service.getRoom(uuid.value, 1L).members shouldHaveSize 3
            service.getRoom(applicant.value, 1L).members.count { it.teamSide == MeetingTeamSide.APPLICANT } shouldBe 2
        }

        it("매칭된 방은 제3자에게 계속 409다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room(MeetingRoomStatus.MATCHED))
            whenever(memberRepository.findAllByRoomId(1L)).thenReturn(emptyList())

            shouldThrow<RoomAlreadyMatchedException> { service.getRoom("viewer", 1L) }
        }

        it("취소된 방은 방장에게도 409다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room(MeetingRoomStatus.CANCELLED))
            whenever(memberRepository.findAllByRoomId(1L)).thenReturn(emptyList())

            shouldThrow<RoomCancelledException> { service.getRoom(uuid.value, 1L) }
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

        it("블랙리스트에 등록된 사용자는 방에 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(profileReader.existsByUuid(applicant)).thenReturn(true)
            whenever(profileReader.getByUuid(applicant)).thenReturn(profile(applicant, id = 9L))
            whenever(blacklistReader.existsByProfileId(9L)).thenReturn(true)

            shouldThrow<MeetingBlockedException> { service.match(matchCommand()) }
            verify(matchRepository, never()).save(any())
        }

        it("프로필이 없어도 승인된 신고의 연락처로 신청하면 거절한다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(profileReader.existsByUuid(applicant)).thenReturn(false)
            whenever(reportReader.findApprovedContacts()).thenReturn(listOf("@applicant"))

            shouldThrow<MeetingBlockedException> { service.match(matchCommand()) }
            verify(matchRepository, never()).save(any())
        }

        it("승인된 신고 연락처와 대소문자만 다른 연락처로도 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(profileReader.existsByUuid(applicant)).thenReturn(false)
            whenever(reportReader.findApprovedContacts()).thenReturn(listOf("@Applicant"))

            shouldThrow<MeetingBlockedException> { service.match(matchCommand()) }
            verify(matchRepository, never()).save(any())
        }

        it("승인된 신고가 있어도 무관한 신청자는 매칭에 성공한다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(profileReader.existsByUuid(applicant)).thenReturn(false)
            whenever(reportReader.findApprovedContacts()).thenReturn(listOf("@someoneelse", "01099998888"))
            whenever(roomRepository.existsOpenByCreatorUuid(applicant, LocalDateTime.of(2026, 8, 31, 12, 0)))
                .thenReturn(false)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid))
            whenever(matchRepository.save(any())).thenAnswer { it.getArgument(0) }
            whenever(memberRepository.saveAll(any())).thenAnswer { it.getArgument(0) }
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument(0) }

            val result = service.match(matchCommand())

            result.status shouldBe MeetingRoomStatus.MATCHED
        }

        it("방을 만들었지만 매칭되지 않은 사용자는 다른 방에 신청할 수 있다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(roomRepository.existsByCreatorUuidAndCreationDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(true)
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsMatchedByCreatorUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsOpenByCreatorUuid(applicant, LocalDateTime.of(2026, 8, 31, 12, 0)))
                .thenReturn(false)
            whenever(profileReader.getByUuid(uuid)).thenReturn(profile(uuid))
            whenever(matchRepository.save(any())).thenAnswer { it.getArgument(0) }
            whenever(memberRepository.saveAll(any())).thenAnswer { it.getArgument(0) }
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument(0) }

            service.match(matchCommand())

            verify(matchRepository).save(any())
            verify(roomRepository).save(check { it.status shouldBe MeetingRoomStatus.MATCHED })
        }

        it("방장으로 오늘 매칭에 성공한 사용자는 다른 방에 신청할 수 없다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(matchRepository.existsByApplicantUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
                .thenReturn(false)
            whenever(roomRepository.existsMatchedByCreatorUuidAndMatchedDate(applicant, LocalDate.of(2026, 8, 31)))
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

    describe("관리자 방 취소") {
        it("열린 방을 CANCELLED로 바꾸고 슬롯을 반환한다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room())
            whenever(roomRepository.save(any())).thenAnswer { it.getArgument(0) }

            service.adminCancel(1L, "secret")

            verify(adminAccessChecker).validateAdminAccess("secret")
            verify(roomRepository).save(check {
                it.status shouldBe MeetingRoomStatus.CANCELLED
                it.activeSlot shouldBe null
            })
        }

        it("관리자 키가 틀리면 방을 조회하지 않고 거절한다") {
            doAnswer { throw AdminPermissionDeniedException() }
                .whenever(adminAccessChecker).validateAdminAccess("wrong")

            shouldThrow<AdminPermissionDeniedException> { service.adminCancel(1L, "wrong") }

            verify(roomRepository, never()).findByIdForUpdate(any())
            verify(roomRepository, never()).save(any())
        }

        it("이미 매칭된 방은 기존 오류로 거절한다") {
            whenever(roomRepository.findByIdForUpdate(1L)).thenReturn(room(MeetingRoomStatus.MATCHED))

            shouldThrow<RoomAlreadyMatchedException> { service.adminCancel(1L, "secret") }
            verify(roomRepository, never()).save(any())
        }

        it("없는 방은 MEETING_ROOM_NOT_FOUND로 거절한다") {
            whenever(roomRepository.findByIdForUpdate(99L)).thenReturn(null)

            shouldThrow<MeetingRoomNotFoundException> { service.adminCancel(99L, "secret") }
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
