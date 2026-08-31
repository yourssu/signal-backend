package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.MeetingMember
import com.yourssu.signal.domain.meeting.implement.MeetingMemberRepository
import com.yourssu.signal.domain.meeting.implement.MeetingTeamSide
import com.yourssu.signal.domain.profile.implement.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MeetingMemberRepositoryImplTest {
    @Autowired lateinit var repository: MeetingMemberRepository

    @Test
    fun `대표 UUID와 동행인 정보를 순서대로 저장한다`() {
        val members = listOf(
            member(order = 0, uuid = Uuid("representative")),
            member(order = 1, uuid = null),
            member(order = 2, uuid = null),
        )

        repository.saveAll(members)

        assertEquals(members, repository.findAllByRoomId(2001).map { it.copy(id = null) })
    }

    private fun member(order: Int, uuid: Uuid?) = MeetingMember(
        roomId = 2001,
        teamSide = MeetingTeamSide.CREATOR,
        memberOrder = order,
        userUuid = uuid,
        gender = Gender.MALE,
        birthYear = 2003,
        department = "컴퓨터학부",
    )
}
