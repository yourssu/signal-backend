package com.yourssu.signal.utils

import kotlin.math.exp
import kotlin.math.sqrt

object GaussianDistributionUtils {
    fun calculateProbabilities(size: Int, stdDev: Double): DoubleArray {
        val indices = DoubleArray(size) { it.toDouble() }
        val pdfValues = DoubleArray(size) { i -> calculateGaussianPdf(indices[i], stdDev) }
        return DoubleArray(size) { pdfValues[it] / pdfValues.sum() }
    }

    private fun calculateGaussianPdf(z: Double, stdDev: Double): Double {
        return 2 * (1 / (stdDev * sqrt(2 * Math.PI))) *
                exp(-(z / stdDev) * (z / stdDev) / 2)
    }

    fun selectIndexByProbabilityDistribution(probabilities: DoubleArray): Int {
        val random = Math.random()
        var cumulativeProbability = 0.0
        for (i in probabilities.indices) {
            cumulativeProbability += probabilities[i]
            if (random < cumulativeProbability) {
                return i
            }
        }
        return probabilities.size - 1
    }
}
