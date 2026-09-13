package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.DailyCreationLimitExceededException
import com.yourssu.signal.domain.meeting.implement.MeetingRoom
import com.yourssu.signal.domain.meeting.implement.MeetingRoomRepository
import com.yourssu.signal.domain.meeting.implement.MeetingRoomStatus
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.meeting.implement.SlotAlreadyOccupiedException
import jakarta.persistence.LockModeType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class MeetingRoomRepositoryImpl(
    private val jpaRepository: MeetingRoomJpaRepository,
) : MeetingRoomRepository {
    override fun save(room: MeetingRoom): MeetingRoom = try {
        jpaRepository.saveAndFlush(MeetingRoomEntity.from(room)).toDomain()
    } catch (exception: DataIntegrityViolationException) {
        when {
            MeetingConstraintClassifier.matches(exception, MeetingConstraintClassifier.ACTIVE_SLOT) ->
                throw SlotAlreadyOccupiedException()
            MeetingConstraintClassifier.matches(exception, MeetingConstraintClassifier.CREATOR_DATE) ->
                throw DailyCreationLimitExceededException()
            else -> throw exception
        }
    }

    override fun findById(id: Long): MeetingRoom? = jpaRepository.findById(id)
        .orElse(null)
        ?.toDomain()

    override fun findByIdForUpdate(id: Long): MeetingRoom? = jpaRepository.findLockedById(id)?.toDomain()

    override fun findOpenBySlot(slot: MeetingSlot, now: LocalDateTime): MeetingRoom? =
        jpaRepository.findOpenBySlot(slot, MeetingRoomStatus.OPEN, now)?.toDomain()

    override fun findAllOpen(now: LocalDateTime): List<MeetingRoom> =
        jpaRepository.findAllOpen(MeetingRoomStatus.OPEN, now).map { it.toDomain() }

    override fun findLatestMatchedAfter(since: LocalDateTime): MeetingRoom? =
        jpaRepository.findFirstByStatusAndMatchedAtGreaterThanOrderByMatchedAtDescIdDesc(
            MeetingRoomStatus.MATCHED,
            since,
        )?.toDomain()

    override fun existsByCreatorUuidAndCreationLimitDate(creatorUuid: Uuid, creationDate: LocalDate): Boolean =
        jpaRepository.existsByCreatorUuidAndCreationLimitDate(creatorUuid.value, creationDate)

    override fun existsOpenByCreatorUuid(creatorUuid: Uuid, now: LocalDateTime): Boolean =
        jpaRepository.existsByCreatorUuidAndStatusAndExpiresAtAfter(creatorUuid.value, MeetingRoomStatus.OPEN, now)

    override fun findOpenByCreatorUuid(creatorUuid: Uuid, now: LocalDateTime): MeetingRoom? =
        jpaRepository.findFirstByCreatorUuidAndStatusAndExpiresAtAfterOrderByIdDesc(
            creatorUuid.value,
            MeetingRoomStatus.OPEN,
            now,
        )?.toDomain()

    override fun expireDueRoomByCreatorUuid(creatorUuid: Uuid, now: LocalDateTime): Int =
        jpaRepository.expireDueRoomByCreatorUuid(
            creatorUuid.value,
            MeetingRoomStatus.OPEN,
            MeetingRoomStatus.EXPIRED,
            now,
        )

    override fun existsMatchedByCreatorUuidAndMatchedDate(creatorUuid: Uuid, matchedDate: LocalDate): Boolean =
        jpaRepository.existsMatchedInPeriod(
            creatorUuid = creatorUuid.value,
            status = MeetingRoomStatus.MATCHED,
            startInclusive = matchedDate.atStartOfDay(),
            endExclusive = matchedDate.plusDays(1).atStartOfDay(),
        )

    override fun findMatchedByCreatorUuidAndMatchedDate(creatorUuid: Uuid, matchedDate: LocalDate): MeetingRoom? =
        jpaRepository.findMatchedInPeriod(
            creatorUuid = creatorUuid.value,
            status = MeetingRoomStatus.MATCHED,
            startInclusive = matchedDate.atStartOfDay(),
            endExclusive = matchedDate.plusDays(1).atStartOfDay(),
        ).firstOrNull()?.toDomain()

    override fun expireDueRooms(now: LocalDateTime): Int =
        jpaRepository.expireDueRooms(MeetingRoomStatus.OPEN, MeetingRoomStatus.EXPIRED, now)

    override fun expireDueRoomInSlot(slot: MeetingSlot, now: LocalDateTime): Int =
        jpaRepository.expireDueRoomInSlot(slot, MeetingRoomStatus.OPEN, MeetingRoomStatus.EXPIRED, now)
}

interface MeetingRoomJpaRepository : JpaRepository<MeetingRoomEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MeetingRoomEntity r where r.id = :id")
    fun findLockedById(id: Long): MeetingRoomEntity?

    @Query(
        """
        select r from MeetingRoomEntity r
        where r.activeSlot = :slot and r.status = :status and r.expiresAt > :now
        """
    )
    fun findOpenBySlot(slot: MeetingSlot, status: MeetingRoomStatus, now: LocalDateTime): MeetingRoomEntity?

    @Query(
        """
        select r from MeetingRoomEntity r
        where r.status = :status and r.expiresAt > :now
        """
    )
    fun findAllOpen(status: MeetingRoomStatus, now: LocalDateTime): List<MeetingRoomEntity>

    fun existsByCreatorUuidAndCreationLimitDate(creatorUuid: String, creationLimitDate: LocalDate): Boolean

    fun existsByCreatorUuidAndStatusAndExpiresAtAfter(
        creatorUuid: String,
        status: MeetingRoomStatus,
        expiresAt: LocalDateTime,
    ): Boolean

    fun findFirstByCreatorUuidAndStatusAndExpiresAtAfterOrderByIdDesc(
        creatorUuid: String,
        status: MeetingRoomStatus,
        expiresAt: LocalDateTime,
    ): MeetingRoomEntity?

    @Query(
        """
        select case when count(r) > 0 then true else false end from MeetingRoomEntity r
        where r.creatorUuid = :creatorUuid and r.status = :status
            and r.matchedAt >= :startInclusive and r.matchedAt < :endExclusive
        """
    )
    fun existsMatchedInPeriod(
        creatorUuid: String,
        status: MeetingRoomStatus,
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): Boolean

    @Query(
        """
        select r from MeetingRoomEntity r
        where r.creatorUuid = :creatorUuid and r.status = :status
            and r.matchedAt >= :startInclusive and r.matchedAt < :endExclusive
        order by r.id desc
        """
    )
    fun findMatchedInPeriod(
        creatorUuid: String,
        status: MeetingRoomStatus,
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): List<MeetingRoomEntity>

    fun findFirstByStatusAndMatchedAtGreaterThanOrderByMatchedAtDescIdDesc(
        status: MeetingRoomStatus,
        matchedAt: LocalDateTime,
    ): MeetingRoomEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update MeetingRoomEntity r
        set r.status = :expiredStatus,
            r.activeSlot = null,
            r.creationLimitDate = null,
            r.expiredAt = :now,
            r.updatedTime = :now
        where r.status = :openStatus and r.expiresAt <= :now
        """
    )
    fun expireDueRooms(
        openStatus: MeetingRoomStatus,
        expiredStatus: MeetingRoomStatus,
        now: LocalDateTime,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update MeetingRoomEntity r
        set r.status = :expiredStatus,
            r.activeSlot = null,
            r.creationLimitDate = null,
            r.expiredAt = :now,
            r.updatedTime = :now
        where r.creatorUuid = :creatorUuid and r.status = :openStatus and r.expiresAt <= :now
        """
    )
    fun expireDueRoomByCreatorUuid(
        creatorUuid: String,
        openStatus: MeetingRoomStatus,
        expiredStatus: MeetingRoomStatus,
        now: LocalDateTime,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update MeetingRoomEntity r
        set r.status = :expiredStatus,
            r.activeSlot = null,
            r.creationLimitDate = null,
            r.expiredAt = :now,
            r.updatedTime = :now
        where r.activeSlot = :slot and r.status = :openStatus and r.expiresAt <= :now
        """
    )
    fun expireDueRoomInSlot(
        slot: MeetingSlot,
        openStatus: MeetingRoomStatus,
        expiredStatus: MeetingRoomStatus,
        now: LocalDateTime,
    ): Int

    fun countBySlot(slot: MeetingSlot): Long
    fun countByActiveSlot(activeSlot: MeetingSlot): Long
    fun countByCreatorUuidAndCreationLimitDate(creatorUuid: String, creationLimitDate: LocalDate): Long
}
