package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Animal
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime

class MeetingRoomTest : DescribeSpec({
    val now = LocalDateTime.of(2026, 8, 31, 12, 0)

    fun room(partySize: Int = 3) = MeetingRoom(
        id = 1L,
        slot = MeetingSlot.SLOT_1,
        activeSlot = MeetingSlot.SLOT_1,
        creatorUuid = Uuid("creator"),
        creatorAnimal = Animal.DOG,
        partySize = partySize,
        invitation = "같이 놀아요",
        status = MeetingRoomStatus.OPEN,
        creationDate = LocalDate.of(2026, 8, 31),
        expiresAt = now.plusHours(1),
    )

    describe("미팅 방") {
        it("2명부터 4명까지만 생성한다") {
            listOf(1, 5).forEach { shouldThrow<InvalidPartySizeException> { room(it) } }
            listOf(2, 3, 4).forEach { room(it).partySize shouldBe it }
        }

        it("초대 문구는 비어 있지 않고 500자를 넘지 않는다") {
            shouldThrow<InvalidMeetingInvitationException> { room().copy(invitation = " ") }
            shouldThrow<InvalidMeetingInvitationException> { room().copy(invitation = "가".repeat(501)) }
        }

        it("매칭되면 종료 상태가 되고 활성 슬롯을 반환한다") {
            val matched = room().match(now)

            matched.status shouldBe MeetingRoomStatus.MATCHED
            matched.activeSlot shouldBe null
            matched.matchedAt shouldBe now
        }

        it("취소되면 종료 상태가 되고 활성 슬롯을 반환한다") {
            val cancelled = room().cancel(now)

            cancelled.status shouldBe MeetingRoomStatus.CANCELLED
            cancelled.activeSlot shouldBe null
            cancelled.cancelledAt shouldBe now
        }

        it("만료되면 종료 상태가 되고 활성 슬롯을 반환한다") {
            val expired = room().expire(now.plusHours(1))

            expired.status shouldBe MeetingRoomStatus.EXPIRED
            expired.activeSlot shouldBe null
            expired.expiredAt shouldBe now.plusHours(1)
        }

        it("종료된 방은 다시 상태 전이하지 않는다") {
            shouldThrow<RoomAlreadyMatchedException> { room().match(now).cancel(now) }
        }
    }

    describe("미팅 슬롯") {
        it("외부에서 선택 가능한 슬롯 식별자는 7개이고 기존 값은 읽을 수 있다") {
            MeetingSlot.selectableEntries.size shouldBe 7
            MeetingSlot.selectableEntries.map { it.name } shouldBe (1..7).map { "SLOT_$it" }
            MeetingSlot.entries.map { it.name } shouldBe (1..10).map { "SLOT_$it" }
        }
    }
})
