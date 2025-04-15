package com.yourssu.ssugaeting.domain.viewer.storage

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.viewer.implement.Viewer
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(name = "viewer")
@EntityListeners(AuditingEntityListener::class)
class ViewerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val uuid: String,

    @Column(nullable = false)
    var ticket: Int = 0,

    @Column(nullable = false)
    var usedTicket: Int = 0,

    @Column(nullable = false)
    var updatedTime: ZonedDateTime,
) {
    companion object {
        fun from(viewer: Viewer): ViewerEntity {
            return ViewerEntity(
                uuid = viewer.uuid.value,
                ticket = viewer.ticket,
                usedTicket = 0,
                updatedTime = viewer.updatedTime
            )
        }
    }

    fun toDomain(): Viewer {
        return Viewer(
            id = id,
            uuid = Uuid(uuid),
            ticket = ticket,
            usedTicket = usedTicket,
            updatedTime = updatedTime,
        )
    }

    fun updateTicket(viewer: Viewer) {
        this.ticket = viewer.ticket
        this.updatedTime = ZonedDateTime.now()
    }

    fun updateUsedTicket(viewer: Viewer) {
        this.usedTicket = viewer.usedTicket
        this.updatedTime = ZonedDateTime.now()
    }
}
