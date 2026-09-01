package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.meeting.implement.MeetingMember
import com.yourssu.signal.domain.meeting.implement.MeetingMemberRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class MeetingMemberRepositoryImpl(
    private val jpaRepository: MeetingMemberJpaRepository,
) : MeetingMemberRepository {
    override fun saveAll(members: List<MeetingMember>): List<MeetingMember> = jpaRepository
        .saveAllAndFlush(members.map(MeetingMemberEntity::from))
        .map { it.toDomain() }

    override fun findAllByRoomId(roomId: Long): List<MeetingMember> =
        jpaRepository.findAllByRoomIdOrderByMemberOrder(roomId).map { it.toDomain() }
}

interface MeetingMemberJpaRepository : JpaRepository<MeetingMemberEntity, Long> {
    fun findAllByRoomIdOrderByMemberOrder(roomId: Long): List<MeetingMemberEntity>
}
