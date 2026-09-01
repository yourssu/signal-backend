package com.yourssu.signal.domain.meeting.implement

enum class MeetingSlot {
    SLOT_1,
    SLOT_2,
    SLOT_3,
    SLOT_4,
    SLOT_5,
    SLOT_6,
    SLOT_7,
    SLOT_8,
    SLOT_9,
    SLOT_10,
    ;

    companion object {
        fun of(value: String): MeetingSlot = entries.firstOrNull { it.name == value.uppercase() }
            ?: throw InvalidMeetingSlotException()
    }
}
