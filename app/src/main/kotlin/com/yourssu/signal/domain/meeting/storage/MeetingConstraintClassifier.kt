package com.yourssu.signal.domain.meeting.storage

import org.hibernate.exception.ConstraintViolationException

internal object MeetingConstraintClassifier {
    const val ACTIVE_SLOT = "uk_meeting_room_active_slot"
    const val CREATOR_DATE = "uk_meeting_room_creator_date"
    const val MATCH_ROOM = "uk_meeting_match_room_id"

    fun constraintName(exception: Throwable): String? = generateSequence(exception) { it.cause }
        .filterIsInstance<ConstraintViolationException>()
        .firstOrNull()
        ?.constraintName
        ?.lowercase()

    fun matches(exception: Throwable, expected: String): Boolean =
        constraintName(exception)?.contains(expected) == true
}
