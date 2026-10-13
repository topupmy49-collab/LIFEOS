package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.util.DateUtils
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TaskDialog(
    initialTask: TaskEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, dueDateEpoch: Long?, priority: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "MEDIUM") }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTask == null) stringResource(R.string.task_add_new) else stringResource(R.string.task_edit),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                LifeOSTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = stringResource(R.string.task_field_title),
                    isError = titleError,
                    errorMessage = stringResource(R.string.required_field),
                    testTag = "task_dialog_title"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LifeOSTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.task_field_desc),
                    singleLine = false,
                    maxLines = 3,
                    testTag = "task_dialog_desc"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.task_field_priority),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                        LifeOSChip(
                            selected = priority == p,
                            text = when (p) {
                                "LOW" -> stringResource(R.string.priority_low)
                                "HIGH" -> stringResource(R.string.priority_high)
                                else -> stringResource(R.string.priority_medium)
                            },
                            onClick = { priority = p }
                        )
                    }
                }
            }
        },
        containerColor = SurfaceCardDark,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            LifeOSButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        // Due date defaults to today or keeps initial
                        val dueDate = initialTask?.dueDateEpoch ?: System.currentTimeMillis()
                        onSave(title, description, dueDate, priority)
                        onDismiss()
                    }
                },
                testTag = "task_dialog_save"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
fun NoteDialog(
    initialNote: NoteEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
    var contentError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialNote == null) stringResource(R.string.note_add_new) else stringResource(R.string.note_edit),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                LifeOSTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.note_field_title),
                    testTag = "note_dialog_title"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LifeOSTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        if (it.isNotBlank()) contentError = false
                    },
                    label = "Note Content",
                    placeholder = stringResource(R.string.note_field_body),
                    singleLine = false,
                    maxLines = 6,
                    isError = contentError,
                    errorMessage = stringResource(R.string.required_field),
                    testTag = "note_dialog_content"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.note_pin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonLime,
                            checkedTrackColor = NeonLime.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        containerColor = SurfaceCardDark,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            LifeOSButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    if (content.isBlank() && title.isBlank()) {
                        contentError = true
                    } else {
                        onSave(title, content, isPinned)
                        onDismiss()
                    }
                },
                testTag = "note_dialog_save"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
fun TransactionDialog(
    initialTx: TransactionEntity? = null,
    defaultCurrency: String = "BDT",
    onDismiss: () -> Unit,
    onSave: (type: String, amountCents: Long, category: String, note: String, dateEpoch: Long) -> Unit
) {
    var type by remember { mutableStateOf(initialTx?.type ?: "EXPENSE") }
    var amountText by remember {
        mutableStateOf(
            if (initialTx != null) String.format("%.2f", initialTx.amountCents / 100.0) else ""
        )
    }
    var category by remember {
        mutableStateOf(
            initialTx?.category ?: if (type == "INCOME") "Salary" else "Food & Groceries"
        )
    }
    var note by remember { mutableStateOf(initialTx?.note ?: "") }
    var amountError by remember { mutableStateOf(false) }

    val incomeCategories = listOf("Salary", "Freelance", "Investment", "Gift / Bonus", "Other")
    val expenseCategories = listOf(
        "Food & Groceries", "Transport", "Utilities & Bills", "Housing & Rent",
        "Shopping", "Health & Medical", "Entertainment", "Education", "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTx == null) {
                    if (type == "INCOME") stringResource(R.string.transaction_add_income)
                    else stringResource(R.string.transaction_add_expense)
                } else stringResource(R.string.transaction_edit),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LifeOSChip(
                        selected = type == "EXPENSE",
                        text = stringResource(R.string.type_expense),
                        onClick = {
                            type = "EXPENSE"
                            if (category !in expenseCategories) category = expenseCategories.first()
                        },
                        selectedColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                    LifeOSChip(
                        selected = type == "INCOME",
                        text = stringResource(R.string.type_income),
                        onClick = {
                            type = "INCOME"
                            if (category !in incomeCategories) category = incomeCategories.first()
                        },
                        selectedColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LifeOSTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = "${stringResource(R.string.field_amount)} ($defaultCurrency)",
                    placeholder = "0.00",
                    isError = amountError,
                    errorMessage = stringResource(R.string.error_invalid_amount),
                    testTag = "tx_dialog_amount"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.field_category),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val categories = if (type == "INCOME") incomeCategories else expenseCategories
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.chunked(3).forEach { rowList ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowList.forEach { cat ->
                                LifeOSChip(
                                    selected = category == cat,
                                    text = cat,
                                    onClick = { category = cat },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LifeOSTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = stringResource(R.string.field_note),
                    testTag = "tx_dialog_note"
                )
            }
        },
        containerColor = SurfaceCardDark,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            LifeOSButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        amountError = true
                    } else {
                        val cents = (parsed * 100).toLong()
                        val dateEpoch = initialTx?.transactionDateEpoch ?: System.currentTimeMillis()
                        onSave(type, cents, category, note, dateEpoch)
                        onDismiss()
                    }
                },
                testTag = "tx_dialog_save"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
fun BudgetDialog(
    initialAmountCents: Long = 0,
    currencyCode: String = "BDT",
    onDismiss: () -> Unit,
    onSave: (amountCents: Long) -> Unit
) {
    var amountText by remember {
        mutableStateOf(
            if (initialAmountCents > 0) String.format("%.2f", initialAmountCents / 100.0) else ""
        )
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.budget_set_dialog),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Specify your target spending limit for this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                LifeOSTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                    },
                    label = "Monthly Budget ($currencyCode)",
                    placeholder = "5000.00",
                    isError = isError,
                    errorMessage = stringResource(R.string.error_invalid_amount),
                    testTag = "budget_dialog_amount"
                )
            }
        },
        containerColor = SurfaceCardDark,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            LifeOSButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        isError = true
                    } else {
                        onSave((parsed * 100).toLong())
                        onDismiss()
                    }
                },
                testTag = "budget_dialog_save"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
fun GoalDialog(
    initialGoal: GoalEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, targetDateEpoch: Long?, progress: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var description by remember { mutableStateOf(initialGoal?.description ?: "") }
    var progress by remember { mutableFloatStateOf(initialGoal?.progressPercent?.toFloat() ?: 0f) }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialGoal == null) stringResource(R.string.goal_add_new) else stringResource(R.string.goal_edit),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                LifeOSTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = stringResource(R.string.goal_field_title),
                    isError = titleError,
                    errorMessage = stringResource(R.string.required_field),
                    testTag = "goal_dialog_title"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LifeOSTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.goal_field_desc),
                    testTag = "goal_dialog_desc"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.goal_field_progress),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${progress.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                }

                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..100f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonLime,
                        activeTrackColor = NeonLime,
                        inactiveTrackColor = SurfaceSecondaryDark
                    )
                )
            }
        },
        containerColor = SurfaceCardDark,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            LifeOSButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(title, description, null, progress.toInt())
                        onDismiss()
                    }
                },
                testTag = "goal_dialog_save"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
fun HabitDialog(
    initialHabit: HabitEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialHabit == null) stringResource(R.string.habit_add_new) else stringResource(R.string.habit_edit),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LifeOSTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = stringResource(R.string.habit_field_title),
                    isError = titleError,
                    errorMessage = stringResource(R.string.required_field),
                    testTag = "habit_dialog_title"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LifeOSTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.habit_field_desc),
                    testTag = "habit_dialog_desc"
                )
            }
        },
        containerColor = SurfaceCardDark,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            LifeOSButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(title, description)
                        onDismiss()
                    }
                },
                testTag = "habit_dialog_save"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TextSecondary)
            }
        }
    )
}
