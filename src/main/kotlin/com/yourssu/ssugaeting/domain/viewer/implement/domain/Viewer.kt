package com.yourssu.ssugaeting.domain.viewer.implement.domain

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Gender
import com.yourssu.ssugaeting.domain.viewer.implement.exception.GenderMismatchException
import com.yourssu.ssugaeting.domain.viewer.implement.exception.ViolatedAddedTicketException
import com.yourssu.ssugaeting.domain.viewer.implement.exception.ViolatedExceedUsedTicketException
import java.time.ZonedDateTime

private const val INITIAL_USED_TICKET = 0
private const val MINIMUM_ADDED_TICKETS = 1

class Viewer(
    val id: Long? = null,
    val uuid: Uuid,
    val gender: Gender,
    val ticket: Int,
    val usedTicket: Int = INITIAL_USED_TICKET,
    val updatedTime: ZonedDateTime = ZonedDateTime.now(),
) {
    companion object {
        private fun validateMinimumAddedTickets(ticket: Int) {
            if (ticket < MINIMUM_ADDED_TICKETS) {
                throw ViolatedAddedTicketException()
            }
        }
    }

    fun addTicket(ticket: Int): Viewer {
        validateMinimumAddedTickets(ticket)
        return Viewer(
            id = id,
            uuid = uuid,
            gender = gender,
            ticket = this.ticket + ticket,
            usedTicket = usedTicket,
            updatedTime = updatedTime,
        )
    }

    fun consumeTicket(ticket: Int): Viewer {
        validateOverUsedTicket(ticket)
        return Viewer(
            id = id,
            uuid = uuid,
            gender = gender,
            ticket = this.ticket,
            usedTicket = this.usedTicket + ticket,
            updatedTime = updatedTime,
        )
    }

    private fun validateOverUsedTicket(ticket: Int) {
        if (this.usedTicket + ticket > this.ticket) {
            throw ViolatedExceedUsedTicketException()
        }
    }

    fun validateSameGender(gender: Gender) {
        if (this.gender != gender) {
            throw GenderMismatchException()
        }
    }
}
