package com.yourssu.signal.domain.blacklist.implement.domain

class Blacklist(
    val id: Long? = null,
    val profileId: Long,
    val createdByAdmin: Boolean,
) {
    fun updateByAdmin(): Blacklist {
        return Blacklist(
            id = this.id,
            profileId = this.profileId,
            createdByAdmin = true,
        )
    }
}
