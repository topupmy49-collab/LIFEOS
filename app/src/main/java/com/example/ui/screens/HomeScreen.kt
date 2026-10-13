package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.DateUtils
import com.example.ui.components.GoalDialog
import com.example.ui.components.HabitDialog
import com.example.ui.components.LifeOSCard
import com.example.ui.components.MetricCard
import com.example.ui.components.NoteDialog
import com.example.ui.components.PriorityBadge
import com.example.ui.components.TaskDialog
import com.example.ui.components.TransactionDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: LifeOSViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToHabits: () -> Unit
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isBengali = preferences.language == "bn"

    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val budget by viewModel.currentMonthBudget.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val habits by viewModel.habitsWithStreaks.collectAsStateWithLifecycle()

    // Dialog state
    var showTaskDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showTxDialog by remember { mutableStateOf(false) }
    var showHabitDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    // Time-based greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> stringResource(R.string.greeting_morning)
        hour in 12..16 -> stringResource(R.string.greeting_afternoon)
        hour in 17..20 -> stringResource(R.string.greeting_evening)
        else -> stringResource(R.string.greeting_night)
    }

    // Derived today's tasks
    val todayStartEpoch = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val todayEndEpoch = todayStartEpoch + 86400000L

    val todayTasks = tasks.filter {
        it.dueDateEpoch != null && it.dueDateEpoch in todayStartEpoch until todayEndEpoch
    }
    val todayCompletedCount = todayTasks.count { it.isCompleted }
    val totalTasksToday = todayTasks.size

    val upcomingTasks = tasks.filter { !it.isCompleted }.take(3)
    val pinnedNotes = notes.filter { it.isPinned }.take(3)
    val activeHabits = habits.filter { !it.habit.isArchived }
    val activeGoals = goals.filter { !it.isCompleted }.take(3)

    // Current month transactions summary
    val currentMonthKey = DateUtils.currentYearMonthString()
    val currentMonthTxs = transactions.filter {
        DateUtils.yearMonthStringFromEpoch(it.transactionDateEpoch) == currentMonthKey
    }
    val totalIncomeCents = currentMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amountCents }
    val totalExpenseCents = currentMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
    val netBalanceCents = totalIncomeCents - totalExpenseCents

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Greeting & Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "$greeting • LIFEOS",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDisplayDate(System.currentTimeMillis(), isBengali),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceSecondaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = stringResource(R.string.dashboard_quick_actions),
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionButton(
                        icon = Icons.Default.CheckCircle,
                        label = stringResource(R.string.action_add_task),
                        color = NeonLime,
                        onClick = { showTaskDialog = true },
                        testTag = "quick_add_task"
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Note,
                        label = stringResource(R.string.action_add_note),
                        color = CyanAccent,
                        onClick = { showNoteDialog = true },
                        testTag = "quick_add_note"
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = stringResource(R.string.action_add_finance),
                        color = SuccessGreen,
                        onClick = { showTxDialog = true },
                        testTag = "quick_add_finance"
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Repeat,
                        label = stringResource(R.string.action_add_habit),
                        color = Color(0xFFFFB300),
                        onClick = { showHabitDialog = true },
                        testTag = "quick_add_habit"
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Flag,
                        label = stringResource(R.string.action_add_goal),
                        color = Color(0xFFFF7043),
                        onClick = { showGoalDialog = true },
                        testTag = "quick_add_goal"
                    )
                }
            }
        }

        // Task Summary Metric
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    title = stringResource(R.string.dashboard_tasks_overview),
                    value = if (totalTasksToday == 0) "$todayCompletedCount / $totalTasksToday" else "$todayCompletedCount/$totalTasksToday done",
                    subtitle = if (totalTasksToday == 0) stringResource(R.string.no_tasks_today) else "${totalTasksToday - todayCompletedCount} remaining",
                    icon = Icons.Default.CheckCircle,
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f).clickable { onNavigateToTasks() }
                )
                MetricCard(
                    title = stringResource(R.string.dashboard_habits_today),
                    value = "${activeHabits.count { it.isCompletedToday }} / ${activeHabits.size}",
                    subtitle = "Checked in today",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f).clickable { onNavigateToHabits() }
                )
            }
        }

        // Financial Pulse Card
        item {
            LifeOSCard(
                onClick = onNavigateToFinance,
                modifier = Modifier.testTag("home_finance_card")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_finance_overview),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = DateUtils.formatDisplayMonth(currentMonthKey, isBengali),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.type_income), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(
                                text = DateUtils.formatMoney(totalIncomeCents, preferences.currencyCode, isBengali),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.type_expense), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(
                                text = DateUtils.formatMoney(totalExpenseCents, preferences.currencyCode, isBengali),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                        Column {
                            Text(stringResource(R.string.net_balance), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = DateUtils.formatMoney(netBalanceCents, preferences.currencyCode, isBengali),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netBalanceCents >= 0) TextPrimary else ErrorRed
                            )
                        }
                    }

                    if (budget != null && budget!!.budgetAmountCents > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val percent = (totalExpenseCents.toFloat() / budget!!.budgetAmountCents.toFloat()).coerceIn(0f, 1f)
                        val remainingCents = (budget!!.budgetAmountCents - totalExpenseCents).coerceAtLeast(0)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Budget: ${DateUtils.formatMoney(budget!!.budgetAmountCents, preferences.currencyCode, isBengali)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Remaining: ${DateUtils.formatMoney(remainingCents, preferences.currencyCode, isBengali)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (percent > 0.9f) ErrorRed else CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { percent },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (percent > 0.9f) ErrorRed else NeonLime,
                            trackColor = SurfaceSecondaryDark
                        )
                    }
                }
            }
        }

        // Today's Habit Checklist Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habits_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLime,
                    modifier = Modifier.clickable { onNavigateToHabits() }
                )
            }
        }

        if (activeHabits.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_habits_today),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else {
            items(activeHabits.take(4)) { item ->
                LifeOSCard(
                    onClick = { viewModel.toggleHabitToday(item.habit.id) },
                    backgroundColor = if (item.isCompletedToday) SurfaceSecondaryDark else SurfaceCardDark
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = item.isCompletedToday,
                            onCheckedChange = { viewModel.toggleHabitToday(item.habit.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonLime,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = BackgroundDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.habit.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (item.habit.description.isNotEmpty()) {
                                Text(
                                    text = item.habit.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (item.currentStreak > 0) Color(0xFFFFB300) else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.currentStreak}d",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (item.currentStreak > 0) Color(0xFFFFB300) else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Tasks Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.dashboard_upcoming_tasks),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLime,
                    modifier = Modifier.clickable { onNavigateToTasks() }
                )
            }
        }

        if (upcomingTasks.isEmpty()) {
            item {
                Text(
                    text = "No pending tasks. Great job!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else {
            items(upcomingTasks) { task ->
                LifeOSCard(
                    onClick = { viewModel.toggleTaskComplete(task) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTaskComplete(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonLime,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = BackgroundDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (task.dueDateEpoch != null) {
                                Text(
                                    text = DateUtils.formatDisplayDate(task.dueDateEpoch, isBengali),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        PriorityBadge(task.priority)
                    }
                }
            }
        }

        // Pinned Notes Section
        if (pinnedNotes.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.dashboard_pinned_notes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanAccent,
                        modifier = Modifier.clickable { onNavigateToNotes() }
                    )
                }
            }

            items(pinnedNotes) { note ->
                LifeOSCard(onClick = onNavigateToNotes) {
                    Column {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Active Goals Section
        if (activeGoals.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_goal_progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonLime,
                        modifier = Modifier.clickable { onNavigateToGoals() }
                    )
                }
            }

            items(activeGoals) { goal ->
                LifeOSCard(onClick = onNavigateToGoals) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${goal.progressPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goal.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = NeonLime,
                            trackColor = SurfaceSecondaryDark
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
        }
    }

    // Active Dialogs
    if (showTaskDialog) {
        TaskDialog(
            onDismiss = { showTaskDialog = false },
            onSave = { t, d, due, p -> viewModel.addTask(t, d, due, p) }
        )
    }
    if (showNoteDialog) {
        NoteDialog(
            onDismiss = { showNoteDialog = false },
            onSave = { t, c, p -> viewModel.addNote(t, c, p) }
        )
    }
    if (showTxDialog) {
        TransactionDialog(
            defaultCurrency = preferences.currencyCode,
            onDismiss = { showTxDialog = false },
            onSave = { type, cents, cat, note, date ->
                viewModel.addTransaction(type, cents, cat, note, date, preferences.currencyCode)
            }
        )
    }
    if (showHabitDialog) {
        HabitDialog(
            onDismiss = { showHabitDialog = false },
            onSave = { t, d -> viewModel.addHabit(t, d) }
        )
    }
    if (showGoalDialog) {
        GoalDialog(
            onDismiss = { showGoalDialog = false },
            onSave = { t, d, due, prog -> viewModel.addGoal(t, d, due, prog) }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}
