package com.yourssu.signal.domain.profile.business.dto

class ConnectionsCountResponse(
    val maleCount: Int,
    val femaleCount: Int,
    val totalCount: Int,
) {
    companion object {
        fun of(maleCount: Int, femaleCount: Int): ConnectionsCountResponse {
            return ConnectionsCountResponse(
                maleCount = maleCount,
                femaleCount = femaleCount,
                totalCount = maleCount + femaleCount,
            )
        }
    }
}
