package com.example.data.repository

import com.example.data.local.LifeOSDatabase
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.MonthlyBudgetEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object DataExportImportHelper {

    suspend fun exportToJson(database: LifeOSDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "LIFEOS")
        root.put("exportedAt", System.currentTimeMillis())

        // Tasks
        val tasks = database.taskDao().getAllTasks().first()
        val tasksArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("description", t.description)
            if (t.dueDateEpoch != null) obj.put("dueDateEpoch", t.dueDateEpoch)
            obj.put("priority", t.priority)
            obj.put("isCompleted", t.isCompleted)
            obj.put("createdAt", t.createdAt)
            obj.put("updatedAt", t.updatedAt)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Notes
        val notes = database.noteDao().getAllNotes().first()
        val notesArray = JSONArray()
        for (n in notes) {
            val obj = JSONObject()
            obj.put("title", n.title)
            obj.put("content", n.content)
            obj.put("isPinned", n.isPinned)
            obj.put("createdAt", n.createdAt)
            obj.put("updatedAt", n.updatedAt)
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        // Transactions
        val transactions = database.financeDao().getAllTransactions().first()
        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject()
            obj.put("type", tx.type)
            obj.put("amountCents", tx.amountCents)
            obj.put("currencyCode", tx.currencyCode)
            obj.put("category", tx.category)
            obj.put("note", tx.note)
            obj.put("transactionDateEpoch", tx.transactionDateEpoch)
            obj.put("createdAt", tx.createdAt)
            obj.put("updatedAt", tx.updatedAt)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Budgets
        val budgets = database.financeDao().getAllBudgets().first()
        val budgetsArray = JSONArray()
        for (b in budgets) {
            val obj = JSONObject()
            obj.put("yearMonth", b.yearMonth)
            obj.put("budgetAmountCents", b.budgetAmountCents)
            obj.put("currencyCode", b.currencyCode)
            budgetsArray.put(obj)
        }
        root.put("budgets", budgetsArray)

        // Goals
        val goals = database.goalDao().getAllGoals().first()
        val goalsArray = JSONArray()
        for (g in goals) {
            val obj = JSONObject()
            obj.put("title", g.title)
            obj.put("description", g.description)
            if (g.targetDateEpoch != null) obj.put("targetDateEpoch", g.targetDateEpoch)
            obj.put("progressPercent", g.progressPercent)
            obj.put("isCompleted", g.isCompleted)
            obj.put("createdAt", g.createdAt)
            obj.put("updatedAt", g.updatedAt)
            goalsArray.put(obj)
        }
        root.put("goals", goalsArray)

        // Habits
        val habits = database.habitDao().getAllHabits().first()
        val habitsArray = JSONArray()
        for (h in habits) {
            val obj = JSONObject()
            obj.put("legacyId", h.id)
            obj.put("title", h.title)
            obj.put("description", h.description)
            obj.put("isArchived", h.isArchived)
            obj.put("createdAt", h.createdAt)
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        // Habit completions
        val completions = database.habitDao().getAllCompletions().first()
        val completionsArray = JSONArray()
        for (c in completions) {
            val obj = JSONObject()
            obj.put("habitLegacyId", c.habitId)
            obj.put("dateString", c.dateString)
            obj.put("completedAt", c.completedAt)
            completionsArray.put(obj)
        }
        root.put("habitCompletions", completionsArray)

        root.toString(2)
    }

    suspend fun importFromJson(database: LifeOSDatabase, jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("appName") || root.optString("appName") != "LIFEOS") {
                return@withContext Result.failure(IllegalArgumentException("Unsupported backup format. Expected LIFEOS backup."))
            }

            var importedCount = 0

            // Import Tasks
            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val task = TaskEntity(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        dueDateEpoch = if (obj.has("dueDateEpoch")) obj.getLong("dueDateEpoch") else null,
                        priority = obj.optString("priority", "MEDIUM"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    database.taskDao().insertTask(task)
                    importedCount++
                }
            }

            // Import Notes
            if (root.has("notes")) {
                val array = root.getJSONArray("notes")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val note = NoteEntity(
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        isPinned = obj.optBoolean("isPinned", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    database.noteDao().insertNote(note)
                    importedCount++
                }
            }

            // Import Transactions
            if (root.has("transactions")) {
                val array = root.getJSONArray("transactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val tx = TransactionEntity(
                        type = obj.getString("type"),
                        amountCents = obj.getLong("amountCents"),
                        currencyCode = obj.optString("currencyCode", "BDT"),
                        category = obj.getString("category"),
                        note = obj.optString("note", ""),
                        transactionDateEpoch = obj.getLong("transactionDateEpoch"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    database.financeDao().insertTransaction(tx)
                    importedCount++
                }
            }

            // Import Budgets
            if (root.has("budgets")) {
                val array = root.getJSONArray("budgets")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val b = MonthlyBudgetEntity(
                        yearMonth = obj.getString("yearMonth"),
                        budgetAmountCents = obj.getLong("budgetAmountCents"),
                        currencyCode = obj.optString("currencyCode", "BDT")
                    )
                    database.financeDao().setBudget(b)
                    importedCount++
                }
            }

            // Import Goals
            if (root.has("goals")) {
                val array = root.getJSONArray("goals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val goal = GoalEntity(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        targetDateEpoch = if (obj.has("targetDateEpoch")) obj.getLong("targetDateEpoch") else null,
                        progressPercent = obj.optInt("progressPercent", 0),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    database.goalDao().insertGoal(goal)
                    importedCount++
                }
            }

            // Import Habits with ID re-mapping
            val idMapping = mutableMapOf<Long, Long>()
            if (root.has("habits")) {
                val array = root.getJSONArray("habits")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val legacyId = obj.optLong("legacyId", 0L)
                    val habit = HabitEntity(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    val newId = database.habitDao().insertHabit(habit)
                    if (legacyId != 0L) {
                        idMapping[legacyId] = newId
                    }
                    importedCount++
                }
            }

            // Import Habit completions
            if (root.has("habitCompletions")) {
                val array = root.getJSONArray("habitCompletions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val legacyId = obj.getLong("habitLegacyId")
                    val mappedId = idMapping[legacyId] ?: legacyId
                    val completion = HabitCompletionEntity(
                        habitId = mappedId,
                        dateString = obj.getString("dateString"),
                        completedAt = obj.optLong("completedAt", System.currentTimeMillis())
                    )
                    database.habitDao().insertCompletion(completion)
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
