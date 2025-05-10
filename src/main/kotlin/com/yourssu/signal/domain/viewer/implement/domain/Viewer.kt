package com.yourssu.signal.domain.viewer.implement.domain

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.viewer.implement.exception.ViolatedAddedTicketException
import com.yourssu.signal.domain.viewer.implement.exception.ViolatedExceedUsedTicketException
import java.time.ZonedDateTime

private const val INITIAL_USED_TICKET = 0
private const val MINIMUM_ADDED_TICKETS = 1

class Viewer(
    val id: Long? = null,
    val uuid: Uuid,
    val ticket: Int,
    val usedTicket: Int = INITIAL_USED_TICKET,
    val updatedTime: ZonedDateTime?,
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
}
