package com.yourssu.signal.domain.viewer.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.viewer.implement.Viewer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.ZoneId

@Entity
@Table(name = "viewer")
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

    @Version
    var version: Long? = null,
): BaseEntity() {
    companion object {
        fun from(viewer: Viewer): ViewerEntity {
            return ViewerEntity(
                uuid = viewer.uuid.value,
                ticket = viewer.ticket,
                usedTicket = 0,
            )
        }
    }

    fun toDomain(): Viewer {
        return Viewer(
            id = id,
            uuid = Uuid(uuid),
            ticket = ticket,
            usedTicket = usedTicket,
            updatedTime = updatedTime?.atZone(ZoneId.systemDefault())
        )
    }

    fun updateTicket(viewer: Viewer) {
        this.ticket = viewer.ticket
    }

    fun updateUsedTicket(viewer: Viewer) {
        this.usedTicket = viewer.usedTicket
    }
}
