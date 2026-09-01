package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.config.security.DataCipher
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.MeetingMatch
import com.yourssu.signal.domain.meeting.implement.MeetingMatchRepository
import com.yourssu.signal.domain.meeting.implement.RoomAlreadyMatchedException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class MeetingMatchRepositoryImpl(
    private val jpaRepository: MeetingMatchJpaRepository,
    private val dataCipher: DataCipher,
) : MeetingMatchRepository {
    override fun save(match: MeetingMatch): MeetingMatch = try {
        jpaRepository.saveAndFlush(
            MeetingMatchEntity.from(
                match = match,
                creatorContact = dataCipher.encrypt(match.creatorContact),
                applicantContact = dataCipher.encrypt(match.applicantContact),
            )
        ).toDomain(match.creatorContact, match.applicantContact)
    } catch (exception: DataIntegrityViolationException) {
        if (MeetingConstraintClassifier.matches(exception, MeetingConstraintClassifier.MATCH_ROOM)) {
            throw RoomAlreadyMatchedException()
        }
        throw exception
    }

    override fun findByRoomId(roomId: Long): MeetingMatch? = jpaRepository.findByRoomId(roomId)?.toDomain()

    override fun findAllByApplicantUuid(applicantUuid: Uuid): List<MeetingMatch> =
        jpaRepository.findAllByApplicantUuid(applicantUuid.value).map { it.toDomain() }

    private fun MeetingMatchEntity.toDomain() = toDomain(
        creatorContact = dataCipher.decrypt(creatorContact),
        applicantContact = dataCipher.decrypt(applicantContact),
    )
}

interface MeetingMatchJpaRepository : JpaRepository<MeetingMatchEntity, Long> {
    fun findByRoomId(roomId: Long): MeetingMatchEntity?
    fun findAllByApplicantUuid(applicantUuid: String): List<MeetingMatchEntity>
    fun countByRoomId(roomId: Long): Long
}
