package com.yourssu.signal.domain.profile.business.dto

import kotlin.math.round

class EgenTetoStatsResponse(
    val egenCount: Int,
    val tetoCount: Int,
    val total: Int,
    val egenRatio: Double,
    val tetoRatio: Double,
) {
    companion object {
        fun of(egenCount: Int, tetoCount: Int, total: Int): EgenTetoStatsResponse {
            val selectedTotal = egenCount + tetoCount
            return EgenTetoStatsResponse(
                egenCount = egenCount,
                tetoCount = tetoCount,
                total = total,
                egenRatio = ratio(egenCount, selectedTotal),
                tetoRatio = ratio(tetoCount, selectedTotal),
            )
        }

        private fun ratio(count: Int, selectedTotal: Int): Double =
            if (selectedTotal == 0) 0.0 else round(count.toDouble() / selectedTotal * 1000) / 10
    }
}
