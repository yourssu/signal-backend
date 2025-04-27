package com.yourssu.signal.domain.profile.application.dto

import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand

data class ProfileFoundRequest(
    val uuid: String
) {
    fun toCommand(profileId: Long): ProfileFoundCommand {
        return ProfileFoundCommand(
            profileId = profileId,
            uuid = uuid,
        )
    }
}
