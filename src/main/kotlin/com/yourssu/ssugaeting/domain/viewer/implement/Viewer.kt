package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.viewer.implement.exception.ViolatedAddedTicketException
import java.time.ZonedDateTime

private const val MINIMUM_ADDED_TICKETS = 1

class Viewer(
    val id: Long? = null,
    val uuid: Uuid,
    val ticket: Int,
    val usedTicket: Int = 0,
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
            ticket = this.ticket + ticket,
            usedTicket = usedTicket,
            updatedTime = updatedTime,
        )
    }
}
