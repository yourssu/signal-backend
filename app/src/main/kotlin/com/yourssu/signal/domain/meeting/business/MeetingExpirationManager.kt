package com.yourssu.signal.domain.meeting.business

import com.yourssu.signal.domain.meeting.implement.MeetingRoomRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class MeetingExpirationManager(
    private val meetingRoomRepository: MeetingRoomRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun expireDueRooms(now: LocalDateTime) {
        meetingRoomRepository.expireDueRooms(now)
    }
}
