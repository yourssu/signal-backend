package com.yourssu.signal.domain.profile.business.command

data class ProfileUpdateCommand(
    val uuid: String,
    val nickname: String,
    val introSentences: List<String>,
)