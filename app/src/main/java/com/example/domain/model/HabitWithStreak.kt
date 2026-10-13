package com.example.domain.model

import com.example.data.local.entity.HabitEntity

data class HabitWithStreak(
    val habit: HabitEntity,
    val currentStreak: Int,
    val longestStreak: Int,
    val isCompletedToday: Boolean,
    val completionDates: Set<String>
)
