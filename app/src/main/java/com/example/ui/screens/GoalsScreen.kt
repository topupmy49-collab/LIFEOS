package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GoalDialog
import com.example.ui.components.LifeOSCard
import com.example.ui.components.LifeOSChip
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.OnNeonLime
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel

@Composable
fun GoalsScreen(
    viewModel: LifeOSViewModel,
    onBack: () -> Unit
) {
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "COMPLETED"

    var showAddDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<GoalEntity?>(null) }
    var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }

    val filtered = goals.filter {
        when (filter) {
            "ACTIVE" -> !it.isCompleted
            "COMPLETED" -> it.isCompleted
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonLime,
                contentColor = OnNeonLime,
                modifier = Modifier.padding(bottom = 64.dp).testTag("fab_add_goal")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.goal_add_new))
            }
        },
        containerColor = BackgroundDark,
        modifier = Modifier.testTag("goals_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.goals_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LifeOSChip(
                    selected = filter == "ALL",
                    text = stringResource(R.string.filter_all),
                    onClick = { filter = "ALL" }
                )
                LifeOSChip(
                    selected = filter == "ACTIVE",
                    text = stringResource(R.string.goal_status_active),
                    onClick = { filter = "ACTIVE" }
                )
                LifeOSChip(
                    selected = filter == "COMPLETED",
                    text = stringResource(R.string.goal_status_completed),
                    onClick = { filter = "COMPLETED" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Flag,
                    title = stringResource(R.string.empty_goals_title),
                    subtitle = stringResource(R.string.empty_goals_subtitle),
                    actionButtonText = stringResource(R.string.goal_add_new),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            onToggleComplete = {
                                viewModel.updateGoalProgress(goal, if (goal.isCompleted) 0 else 100)
                            },
                            onEdit = { goalToEdit = goal },
                            onDelete = { goalToDelete = goal }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        GoalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, targetDate, prog ->
                viewModel.addGoal(title, desc, targetDate, prog)
            }
        )
    }

    if (goalToEdit != null) {
        GoalDialog(
            initialGoal = goalToEdit,
            onDismiss = { goalToEdit = null },
            onSave = { title, desc, targetDate, prog ->
                viewModel.updateGoal(
                    goalToEdit!!.copy(
                        title = title,
                        description = desc,
                        targetDateEpoch = targetDate,
                        progressPercent = prog,
                        isCompleted = prog >= 100
                    )
                )
                goalToEdit = null
            }
        )
    }

    if (goalToDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.btn_delete),
            message = stringResource(R.string.delete_goal_confirm),
            isDestructive = true,
            confirmText = stringResource(R.string.btn_delete),
            onConfirm = {
                viewModel.deleteGoal(goalToDelete!!)
                goalToDelete = null
            },
            onDismiss = { goalToDelete = null }
        )
    }
}

@Composable
private fun GoalItemCard(
    goal: GoalEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LifeOSCard(
        backgroundColor = if (goal.isCompleted) SurfaceSecondaryDark else SurfaceCardDark,
        modifier = Modifier.testTag("goal_item_${goal.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleComplete) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Toggle Complete",
                            tint = if (goal.isCompleted) NeonLime else TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (goal.isCompleted) "Completed" else "Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goal.isCompleted) NeonLime else TextSecondary
                )
                Text(
                    text = "${goal.progressPercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) NeonLime else CyanAccent,
                trackColor = SurfaceSecondaryDark
            )
        }
    }
}
