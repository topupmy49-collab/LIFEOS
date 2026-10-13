package com.example.data.repository

import com.example.core.util.DateUtils
import com.example.data.local.LifeOSDatabase
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.MonthlyBudgetEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.HabitWithStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LifeOSRepository(private val database: LifeOSDatabase) {

    private val taskDao = database.taskDao()
    private val noteDao = database.noteDao()
    private val financeDao = database.financeDao()
    private val goalDao = database.goalDao()
    private val habitDao = database.habitDao()

    // --- Tasks ---
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    // --- Notes ---
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun toggleNotePinned(note: NoteEntity) {
        val updated = note.copy(
            isPinned = !note.isPinned,
            updatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }

    // --- Finance ---
    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        financeDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        financeDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        financeDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        financeDao.deleteTransactionById(id)

    fun getBudget(yearMonth: String): Flow<MonthlyBudgetEntity?> =
        financeDao.getBudget(yearMonth)

    suspend fun setBudget(yearMonth: String, amountCents: Long, currencyCode: String = "BDT") {
        val budget = MonthlyBudgetEntity(
            yearMonth = yearMonth,
            budgetAmountCents = amountCents,
            currencyCode = currencyCode
        )
        financeDao.setBudget(budget)
    }

    // --- Goals ---
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun insertGoal(goal: GoalEntity): Long = goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: GoalEntity) = goalDao.deleteGoal(goal)

    suspend fun deleteGoalById(id: Long) = goalDao.deleteGoalById(id)

    suspend fun updateGoalProgress(goal: GoalEntity, newProgress: Int) {
        val clamped = newProgress.coerceIn(0, 100)
        val updated = goal.copy(
            progressPercent = clamped,
            isCompleted = clamped == 100,
            updatedAt = System.currentTimeMillis()
        )
        goalDao.updateGoal(updated)
    }

    // --- Habits ---
    val habitsWithStreaks: Flow<List<HabitWithStreak>> = combine(
        habitDao.getAllHabits(),
        habitDao.getAllCompletions()
    ) { habits, completions ->
        val completionsByHabit = completions.groupBy { it.habitId }
        val todayStr = DateUtils.todayDateString()

        habits.map { habit ->
            val dates = completionsByHabit[habit.id]?.map { it.dateString }?.toSet() ?: emptySet()
            val (currentStreak, longestStreak) = DateUtils.calculateStreaks(dates, todayStr)
            val isCompletedToday = dates.contains(todayStr)

            HabitWithStreak(
                habit = habit,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                isCompletedToday = isCompletedToday,
                completionDates = dates
            )
        }
    }

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteCompletionsByHabitId(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String = DateUtils.todayDateString()) {
        val completions = habitDao.getCompletionListForHabit(habitId)
        val alreadyCompleted = completions.any { it.dateString == dateString }
        if (alreadyCompleted) {
            habitDao.deleteCompletion(habitId, dateString)
        } else {
            val record = HabitCompletionEntity(
                habitId = habitId,
                dateString = dateString,
                completedAt = System.currentTimeMillis()
            )
            habitDao.insertCompletion(record)
        }
    }

    suspend fun toggleHabitArchived(habit: HabitEntity) {
        val updated = habit.copy(isArchived = !habit.isArchived)
        habitDao.updateHabit(updated)
    }

    // --- Database Cleanup ---
    suspend fun clearAllData() {
        taskDao.clearAllTasks()
        noteDao.clearAllNotes()
        financeDao.clearAllTransactions()
        financeDao.clearAllBudgets()
        goalDao.clearAllGoals()
        habitDao.clearAllHabits()
        habitDao.clearAllCompletions()
    }
}
