package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.DateUtils
import com.example.data.local.LifeOSDatabase
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.MonthlyBudgetEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DataExportImportHelper
import com.example.data.repository.LifeOSRepository
import com.example.domain.model.HabitWithStreak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LifeOSViewModel(
    application: Application,
    private val repository: LifeOSRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val database: LifeOSDatabase
) : AndroidViewModel(application) {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    // --- Tasks State ---
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val taskSearchQuery = MutableStateFlow("")
    val taskStatusFilter = MutableStateFlow("ALL") // "ALL", "PENDING", "COMPLETED", "OVERDUE"
    val taskSortOrder = MutableStateFlow("DUE_DATE") // "DUE_DATE", "PRIORITY", "CREATED"

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        taskSearchQuery,
        taskStatusFilter,
        taskSortOrder
    ) { tasks, query, status, sort ->
        val now = System.currentTimeMillis()
        var list = tasks.filter {
            val matchesQuery = query.isEmpty() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            val matchesStatus = when (status) {
                "PENDING" -> !it.isCompleted
                "COMPLETED" -> it.isCompleted
                "OVERDUE" -> !it.isCompleted && it.dueDateEpoch != null && it.dueDateEpoch < now
                else -> true
            }
            matchesQuery && matchesStatus
        }

        list = when (sort) {
            "PRIORITY" -> list.sortedByDescending {
                when (it.priority.uppercase()) {
                    "HIGH" -> 3
                    "MEDIUM" -> 2
                    else -> 1
                }
            }
            "CREATED" -> list.sortedByDescending { it.createdAt }
            else -> list.sortedBy { it.dueDateEpoch ?: Long.MAX_VALUE }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Notes State ---
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val noteSearchQuery = MutableStateFlow("")

    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        allNotes,
        noteSearchQuery
    ) { notes, query ->
        if (query.isEmpty()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Finance State ---
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedFinanceMonth = MutableStateFlow(DateUtils.currentYearMonthString())
    val financeTypeFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val financeSearchQuery = MutableStateFlow("")

    val currentMonthBudget: StateFlow<MonthlyBudgetEntity?> = selectedFinanceMonth
        .flatMapLatest { yearMonth ->
            repository.getBudget(yearMonth)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        selectedFinanceMonth,
        financeTypeFilter,
        financeSearchQuery
    ) { transactions, month, typeFilter, query ->
        transactions.filter { tx ->
            val txMonth = DateUtils.yearMonthStringFromEpoch(tx.transactionDateEpoch)
            val matchesMonth = txMonth == month
            val matchesType = when (typeFilter) {
                "INCOME" -> tx.type == "INCOME"
                "EXPENSE" -> tx.type == "EXPENSE"
                else -> true
            }
            val matchesQuery = query.isEmpty() ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true)
            matchesMonth && matchesType && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Goals State ---
    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Habits State ---
    val habitsWithStreaks: StateFlow<List<HabitWithStreak>> = repository.habitsWithStreaks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- User Feedback / Toast message ---
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- Task Actions ---
    fun addTask(title: String, description: String, dueDateEpoch: Long?, priority: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    dueDateEpoch = dueDateEpoch,
                    priority = priority
                )
            )
            _userMessage.value = "Task created"
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
            _userMessage.value = "Task updated"
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _userMessage.value = "Task deleted"
        }
    }

    // --- Note Actions ---
    fun addNote(title: String, content: String, isPinned: Boolean = false) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = title.trim().ifEmpty { "Untitled" },
                    content = content.trim(),
                    isPinned = isPinned
                )
            )
            _userMessage.value = "Note saved"
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            _userMessage.value = "Note updated"
        }
    }

    fun toggleNotePinned(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNotePinned(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            _userMessage.value = "Note deleted"
        }
    }

    // --- Finance Actions ---
    fun addTransaction(
        type: String,
        amountCents: Long,
        category: String,
        note: String,
        dateEpoch: Long,
        currencyCode: String
    ) {
        if (amountCents <= 0) return
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    amountCents = amountCents,
                    currencyCode = currencyCode,
                    category = category,
                    note = note.trim(),
                    transactionDateEpoch = dateEpoch
                )
            )
            _userMessage.value = "Transaction added"
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx.copy(updatedAt = System.currentTimeMillis()))
            _userMessage.value = "Transaction updated"
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            _userMessage.value = "Transaction deleted"
        }
    }

    fun setMonthlyBudget(amountCents: Long, currencyCode: String) {
        val month = selectedFinanceMonth.value
        viewModelScope.launch {
            repository.setBudget(month, amountCents, currencyCode)
            _userMessage.value = "Budget updated"
        }
    }

    // --- Goal Actions ---
    fun addGoal(title: String, description: String, targetDateEpoch: Long?, progress: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    targetDateEpoch = targetDateEpoch,
                    progressPercent = progress.coerceIn(0, 100),
                    isCompleted = progress >= 100
                )
            )
            _userMessage.value = "Goal created"
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(updatedAt = System.currentTimeMillis()))
            _userMessage.value = "Goal updated"
        }
    }

    fun updateGoalProgress(goal: GoalEntity, progress: Int) {
        viewModelScope.launch {
            repository.updateGoalProgress(goal, progress)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _userMessage.value = "Goal deleted"
        }
    }

    // --- Habit Actions ---
    fun addHabit(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = title.trim(),
                    description = description.trim()
                )
            )
            _userMessage.value = "Habit created"
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            _userMessage.value = "Habit updated"
        }
    }

    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId)
        }
    }

    fun toggleHabitArchived(habit: HabitEntity) {
        viewModelScope.launch {
            repository.toggleHabitArchived(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            _userMessage.value = "Habit deleted"
        }
    }

    // --- Preferences Actions ---
    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setCurrency(currencyCode: String) {
        viewModelScope.launch {
            preferencesRepository.setCurrencyCode(currencyCode)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            _userMessage.value = "All data cleared"
            onComplete()
        }
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = DataExportImportHelper.exportToJson(database)
            onResult(json)
        }
    }

    fun importData(json: String, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val result = DataExportImportHelper.importFromJson(database, json)
            if (result.isSuccess) {
                _userMessage.value = "Restored ${result.getOrNull()} items successfully"
            }
            onResult(result)
        }
    }
}

class LifeOSViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LifeOSViewModel::class.java)) {
            val database = LifeOSDatabase.getInstance(application)
            val repository = LifeOSRepository(database)
            val preferences = UserPreferencesRepository(application)
            return LifeOSViewModel(application, repository, preferences, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
