package com.yourssu.signal.domain.profile.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PurchasedProfileRepositoryImplTest {

    @Test
    fun `ranking calculation logic should work correctly with different purchase counts`() {
        val purchaseCounts = listOf(3, 2, 1)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[3])
        assertEquals(2, countToRankMap[2])
        assertEquals(3, countToRankMap[1])
    }

    @Test
    fun `ranking calculation logic should handle ties correctly`() {
        val purchaseCounts = listOf(2, 2, 1)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[2])
        assertEquals(3, countToRankMap[1])
    }

    @Test
    fun `ranking calculation logic should handle multiple ties correctly`() {
        val purchaseCounts = listOf(3, 2, 2, 1, 1, 1)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[3])
        assertEquals(2, countToRankMap[2])
        assertEquals(4, countToRankMap[1])
    }

    @Test
    fun `ranking calculation logic should work with single item`() {
        val purchaseCounts = listOf(5)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[5])
    }

    @Test
    fun `ranking calculation logic should handle empty list`() {
        val purchaseCounts = emptyList<Int>()
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(0, countToRankMap.size)
    }
}