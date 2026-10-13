package com.example

import com.example.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHabitStreakCalculation_consecutiveDays() {
        val today = "2026-10-13"
        // 5 consecutive days completed: Oct 9, 10, 11, 12, 13
        val dates = setOf(
            "2026-10-09",
            "2026-10-10",
            "2026-10-11",
            "2026-10-12",
            "2026-10-13"
        )
        val (currentStreak, longestStreak) = DateUtils.calculateStreaks(dates, today)
        assertEquals(5, currentStreak)
        assertEquals(5, longestStreak)
    }

    @Test
    fun testHabitStreakCalculation_completedYesterdayNotToday() {
        val today = "2026-10-13"
        // Completed up to yesterday Oct 12: streak is 3 and still alive
        val dates = setOf(
            "2026-10-10",
            "2026-10-11",
            "2026-10-12"
        )
        val (currentStreak, longestStreak) = DateUtils.calculateStreaks(dates, today)
        assertEquals(3, currentStreak)
        assertEquals(3, longestStreak)
    }

    @Test
    fun testHabitStreakCalculation_brokenStreak() {
        val today = "2026-10-13"
        // Gap on Oct 11 and 12, completed only on Oct 10 and Oct 13
        val dates = setOf(
            "2026-10-08",
            "2026-10-09",
            "2026-10-10",
            "2026-10-13"
        )
        val (currentStreak, longestStreak) = DateUtils.calculateStreaks(dates, today)
        assertEquals(1, currentStreak) // only today
        assertEquals(3, longestStreak) // Oct 8, 9, 10
    }

    @Test
    fun testHabitStreakCalculation_emptyDates() {
        val today = "2026-10-13"
        val (currentStreak, longestStreak) = DateUtils.calculateStreaks(emptySet(), today)
        assertEquals(0, currentStreak)
        assertEquals(0, longestStreak)
    }

    @Test
    fun testFinanceCalculations() {
        val salaryCents = 150000_00L // 150,000.00
        val freelanceCents = 25000_50L // 25,000.50
        val groceryCents = 12000_00L // 12,000.00
        val rentCents = 40000_00L // 40,000.00

        val totalIncome = salaryCents + freelanceCents
        val totalExpense = groceryCents + rentCents
        val netBalance = totalIncome - totalExpense

        assertEquals(175000_50L, totalIncome)
        assertEquals(52000_00L, totalExpense)
        assertEquals(123000_50L, netBalance)

        val budgetCents = 60000_00L
        val remainingBudget = budgetCents - totalExpense
        assertEquals(8000_00L, remainingBudget)
    }

    @Test
    fun testMoneyFormatting() {
        val formatted = DateUtils.formatMoney(50000, "BDT", isBengali = false)
        assertTrue(formatted.contains("500.00"))

        val formattedBn = DateUtils.formatMoney(50000, "BDT", isBengali = true)
        assertTrue(formattedBn.contains("৳"))
    }

    @Test
    fun testDateUtils_nextAndPreviousDate() {
        val date = "2026-10-13"
        assertEquals("2026-10-12", DateUtils.previousDateString(date))
        assertEquals("2026-10-14", DateUtils.nextDateString(date))

        // Month boundary
        val monthStart = "2026-10-01"
        assertEquals("2026-09-30", DateUtils.previousDateString(monthStart))

        val monthEnd = "2026-09-30"
        assertEquals("2026-10-01", DateUtils.nextDateString(monthEnd))
    }

    @Test
    fun testGoalProgressClamping() {
        val negative = (-25).coerceIn(0, 100)
        val over100 = 150.coerceIn(0, 100)
        val valid = 75.coerceIn(0, 100)

        assertEquals(0, negative)
        assertEquals(100, over100)
        assertEquals(75, valid)
    }
}
