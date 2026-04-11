package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.profile.implement.ProfileRanking

data class ProfileRankingResponse(
    val rank: Int,
    val totalProfiles: Int,
    val purchaseCount: Int,
    val gender: String,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
    val school: String
) {
    companion object {
        fun of(profileRanking: ProfileRanking, profile: Profile, totalProfiles: Int): ProfileRankingResponse {
            return ProfileRankingResponse(
                rank = profileRanking.rank,
                totalProfiles = totalProfiles,
                purchaseCount = profileRanking.purchaseCount,
                gender = profile.gender.name,
                department = profile.department,
                birthYear = profile.birthYear,
                animal = profile.animal,
                mbti = profile.mbti,
                nickname = profile.nickname,
                introSentences = profile.introSentences,
                school = profile.school
            )
        }
    }
}
