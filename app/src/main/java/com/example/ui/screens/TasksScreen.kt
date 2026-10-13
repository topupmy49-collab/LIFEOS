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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.DateUtils
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LifeOSCard
import com.example.ui.components.LifeOSChip
import com.example.ui.components.LifeOSTextField
import com.example.ui.components.PriorityBadge
import com.example.ui.components.TaskDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonLime
import com.example.ui.theme.OnNeonLime
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel

@Composable
fun TasksScreen(
    viewModel: LifeOSViewModel
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.taskSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.taskStatusFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.taskSortOrder.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isBengali = preferences.language == "bn"

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonLime,
                contentColor = OnNeonLime,
                modifier = Modifier.padding(bottom = 64.dp).testTag("fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.task_add_new))
            }
        },
        containerColor = BackgroundDark,
        modifier = Modifier.testTag("tasks_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = stringResource(R.string.tasks_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            LifeOSTextField(
                value = searchQuery,
                onValueChange = { viewModel.taskSearchQuery.value = it },
                label = stringResource(R.string.search_tasks),
                testTag = "tasks_search_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (All, Pending, Completed, Overdue)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    LifeOSChip(
                        selected = statusFilter == "ALL",
                        text = stringResource(R.string.filter_all),
                        onClick = { viewModel.taskStatusFilter.value = "ALL" },
                        modifier = Modifier.testTag("filter_all_chip")
                    )
                }
                item {
                    LifeOSChip(
                        selected = statusFilter == "PENDING",
                        text = stringResource(R.string.filter_pending),
                        onClick = { viewModel.taskStatusFilter.value = "PENDING" },
                        modifier = Modifier.testTag("filter_pending_chip")
                    )
                }
                item {
                    LifeOSChip(
                        selected = statusFilter == "COMPLETED",
                        text = stringResource(R.string.filter_completed),
                        onClick = { viewModel.taskStatusFilter.value = "COMPLETED" },
                        modifier = Modifier.testTag("filter_completed_chip")
                    )
                }
                item {
                    LifeOSChip(
                        selected = statusFilter == "OVERDUE",
                        text = stringResource(R.string.filter_overdue),
                        onClick = { viewModel.taskStatusFilter.value = "OVERDUE" },
                        modifier = Modifier.testTag("filter_overdue_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Sort:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                LifeOSChip(
                    selected = sortOrder == "DUE_DATE",
                    text = "Due Date",
                    onClick = { viewModel.taskSortOrder.value = "DUE_DATE" }
                )
                LifeOSChip(
                    selected = sortOrder == "PRIORITY",
                    text = "Priority",
                    onClick = { viewModel.taskSortOrder.value = "PRIORITY" }
                )
                LifeOSChip(
                    selected = sortOrder == "CREATED",
                    text = "Recent",
                    onClick = { viewModel.taskSortOrder.value = "CREATED" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task List or Empty State
            if (tasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = stringResource(R.string.empty_tasks_title),
                    subtitle = stringResource(R.string.empty_tasks_subtitle),
                    actionButtonText = stringResource(R.string.task_add_new),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            isBengali = isBengali,
                            onToggleComplete = { viewModel.toggleTaskComplete(task) },
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDelete = task }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        TaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, due, priority ->
                viewModel.addTask(title, desc, due, priority)
            }
        )
    }

    // Edit Dialog
    if (taskToEdit != null) {
        TaskDialog(
            initialTask = taskToEdit,
            onDismiss = { taskToEdit = null },
            onSave = { title, desc, due, priority ->
                viewModel.updateTask(
                    taskToEdit!!.copy(
                        title = title,
                        description = desc,
                        dueDateEpoch = due,
                        priority = priority
                    )
                )
                taskToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (taskToDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.btn_delete),
            message = stringResource(R.string.delete_task_confirm),
            isDestructive = true,
            confirmText = stringResource(R.string.btn_delete),
            onConfirm = {
                viewModel.deleteTask(taskToDelete!!)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}

@Composable
private fun TaskItemCard(
    task: TaskEntity,
    isBengali: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LifeOSCard(
        backgroundColor = if (task.isCompleted) SurfaceSecondaryDark.copy(alpha = 0.6f) else SurfaceCardDark,
        modifier = Modifier.testTag("task_item_${task.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonLime,
                    uncheckedColor = TextSecondary,
                    checkmarkColor = BackgroundDark
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
                if (task.dueDateEpoch != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val isOverdue = !task.isCompleted && task.dueDateEpoch < System.currentTimeMillis()
                    Text(
                        text = "Due: ${DateUtils.formatDisplayDate(task.dueDateEpoch, isBengali)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) com.example.ui.theme.ErrorRed else CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            PriorityBadge(task.priority)

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = com.example.ui.theme.ErrorRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
