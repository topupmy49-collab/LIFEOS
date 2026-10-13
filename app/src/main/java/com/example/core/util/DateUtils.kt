package com.example.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val MONTH_FORMAT = SimpleDateFormat("yyyy-MM", Locale.US)

    fun todayDateString(): String {
        return DATE_FORMAT.format(Date())
    }

    fun currentYearMonthString(): String {
        return MONTH_FORMAT.format(Date())
    }

    fun yearMonthStringFromEpoch(epochMillis: Long): String {
        return MONTH_FORMAT.format(Date(epochMillis))
    }

    fun dateStringToEpoch(dateString: String): Long {
        return try {
            DATE_FORMAT.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun epochToDateString(epochMillis: Long): String {
        return DATE_FORMAT.format(Date(epochMillis))
    }

    fun formatDisplayDate(epochMillis: Long, isBengali: Boolean = false): String {
        val pattern = if (isBengali) "dd MMM, yyyy" else "MMM dd, yyyy"
        val locale = if (isBengali) Locale("bn", "BD") else Locale.US
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(epochMillis))
    }

    fun formatDisplayMonth(yearMonth: String, isBengali: Boolean = false): String {
        return try {
            val date = MONTH_FORMAT.parse(yearMonth) ?: Date()
            val pattern = if (isBengali) "MMMM yyyy" else "MMMM yyyy"
            val locale = if (isBengali) Locale("bn", "BD") else Locale.US
            SimpleDateFormat(pattern, locale).format(date)
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun previousDateString(dateString: String): String {
        return try {
            val date = DATE_FORMAT.parse(dateString) ?: return dateString
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, -1)
            DATE_FORMAT.format(cal.time)
        } catch (_: Exception) {
            dateString
        }
    }

    fun nextDateString(dateString: String): String {
        return try {
            val date = DATE_FORMAT.parse(dateString) ?: return dateString
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, 1)
            DATE_FORMAT.format(cal.time)
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Calculates current streak and longest streak accurately.
     * @param dates Set of dates in "yyyy-MM-dd" format where habit was completed
     * @param todayStr Today's date in "yyyy-MM-dd" format
     * @return Pair(currentStreak, longestStreak)
     */
    fun calculateStreaks(dates: Set<String>, todayStr: String = todayDateString()): Pair<Int, Int> {
        if (dates.isEmpty()) return Pair(0, 0)

        // 1. Calculate Current Streak
        var currentStreak = 0
        val isCompletedToday = dates.contains(todayStr)
        val yesterdayStr = previousDateString(todayStr)
        val isCompletedYesterday = dates.contains(yesterdayStr)

        if (isCompletedToday) {
            currentStreak = 1
            var checkDate = yesterdayStr
            while (dates.contains(checkDate)) {
                currentStreak++
                checkDate = previousDateString(checkDate)
            }
        } else if (isCompletedYesterday) {
            // Habit wasn't marked today yet, but streak from yesterday is still alive
            currentStreak = 1
            var checkDate = previousDateString(yesterdayStr)
            while (dates.contains(checkDate)) {
                currentStreak++
                checkDate = previousDateString(checkDate)
            }
        }

        // 2. Calculate Longest Streak
        val sortedDates = dates.toList().sorted()
        var maxStreak = 0
        var runningStreak = 0
        var expectedNext: String? = null

        for (d in sortedDates) {
            if (expectedNext == null) {
                runningStreak = 1
            } else if (d == expectedNext) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > maxStreak) {
                maxStreak = runningStreak
            }
            expectedNext = nextDateString(d)
        }

        return Pair(currentStreak, maxStreak)
    }

    fun formatMoney(amountCents: Long, currencyCode: String, isBengali: Boolean = false): String {
        val major = amountCents / 100.0
        val formattedNumber = String.format(Locale.US, "%,.2f", major)
        val symbolOrCode = when (currencyCode) {
            "BDT" -> if (isBengali) "৳" else "BDT "
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            else -> "$currencyCode "
        }

        return if (isBengali) {
            // Convert digits to Bengali numerals if needed
            val bnDigits = formattedNumber.map { ch ->
                when (ch) {
                    '0' -> '০'
                    '1' -> '১'
                    '2' -> '২'
                    '3' -> '৩'
                    '4' -> '৪'
                    '5' -> '৫'
                    '6' -> '৬'
                    '7' -> '৭'
                    '8' -> '৮'
                    '9' -> '৯'
                    else -> ch
                }
            }.joinToString("")
            "$symbolOrCode$bnDigits"
        } else {
            "$symbolOrCode$formattedNumber"
        }
    }
}
