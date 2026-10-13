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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.core.util.DateUtils
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.BudgetDialog
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LifeOSCard
import com.example.ui.components.LifeOSChip
import com.example.ui.components.LifeOSTextField
import com.example.ui.components.TransactionDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.OnNeonLime
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel

@Composable
fun FinanceScreen(
    viewModel: LifeOSViewModel
) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val budget by viewModel.currentMonthBudget.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedFinanceMonth.collectAsStateWithLifecycle()
    val typeFilter by viewModel.financeTypeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.financeSearchQuery.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isBengali = preferences.language == "bn"

    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amountCents }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SuccessGreen,
                contentColor = BackgroundDark,
                modifier = Modifier.padding(bottom = 64.dp).testTag("fab_add_finance")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        containerColor = BackgroundDark,
        modifier = Modifier.testTag("finance_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.finance_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                LifeOSChip(
                    selected = true,
                    text = DateUtils.formatDisplayMonth(selectedMonth, isBengali),
                    onClick = { /* Current month active */ },
                    selectedColor = SurfaceSecondaryDark,
                    selectedTextColor = CyanAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Card
            LifeOSCard(
                backgroundColor = SurfaceCardDark
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.monthly_income), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Text(
                                text = DateUtils.formatMoney(totalIncome, preferences.currencyCode, isBengali),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.monthly_expenses), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Text(
                                text = DateUtils.formatMoney(totalExpense, preferences.currencyCode, isBengali),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }

                        Column {
                            Text(stringResource(R.string.net_balance), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = DateUtils.formatMoney(netBalance, preferences.currencyCode, isBengali),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netBalance >= 0) TextPrimary else ErrorRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Budget Progress Bar & Set Button
                    val budgetCents = budget?.budgetAmountCents ?: 0L
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (budgetCents > 0) "Budget: ${DateUtils.formatMoney(budgetCents, preferences.currencyCode, isBengali)}" else "No Budget Set",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = if (budgetCents > 0) "Edit Limit" else "Set Limit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            modifier = Modifier
                                .clickable { showBudgetDialog = true }
                                .testTag("set_budget_button")
                        )
                    }

                    if (budgetCents > 0) {
                        val progress = (totalExpense.toFloat() / budgetCents.toFloat()).coerceIn(0f, 1f)
                        val remaining = (budgetCents - totalExpense).coerceAtLeast(0)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (progress > 0.9f) ErrorRed else NeonLime,
                            trackColor = SurfaceSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Used ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Remaining: ${DateUtils.formatMoney(remaining, preferences.currencyCode, isBengali)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (remaining == 0L) ErrorRed else CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LifeOSChip(
                    selected = typeFilter == "ALL",
                    text = stringResource(R.string.filter_all),
                    onClick = { viewModel.financeTypeFilter.value = "ALL" },
                    modifier = Modifier.weight(1f)
                )
                LifeOSChip(
                    selected = typeFilter == "EXPENSE",
                    text = stringResource(R.string.type_expense),
                    onClick = { viewModel.financeTypeFilter.value = "EXPENSE" },
                    selectedColor = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
                LifeOSChip(
                    selected = typeFilter == "INCOME",
                    text = stringResource(R.string.type_income),
                    onClick = { viewModel.financeTypeFilter.value = "INCOME" },
                    selectedColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            LifeOSTextField(
                value = searchQuery,
                onValueChange = { viewModel.financeSearchQuery.value = it },
                label = stringResource(R.string.search_finance),
                testTag = "finance_search_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = stringResource(R.string.empty_finance_title),
                    subtitle = stringResource(R.string.empty_finance_subtitle),
                    actionButtonText = stringResource(R.string.transaction_add_expense),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemCard(
                            tx = tx,
                            currencyCode = preferences.currencyCode,
                            isBengali = isBengali,
                            onEdit = { txToEdit = tx },
                            onDelete = { txToDelete = tx }
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
        TransactionDialog(
            defaultCurrency = preferences.currencyCode,
            onDismiss = { showAddDialog = false },
            onSave = { type, cents, cat, note, date ->
                viewModel.addTransaction(type, cents, cat, note, date, preferences.currencyCode)
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            initialAmountCents = budget?.budgetAmountCents ?: 0L,
            currencyCode = preferences.currencyCode,
            onDismiss = { showBudgetDialog = false },
            onSave = { amountCents ->
                viewModel.setMonthlyBudget(amountCents, preferences.currencyCode)
            }
        )
    }

    if (txToEdit != null) {
        TransactionDialog(
            initialTx = txToEdit,
            defaultCurrency = preferences.currencyCode,
            onDismiss = { txToEdit = null },
            onSave = { type, cents, cat, note, date ->
                viewModel.updateTransaction(
                    txToEdit!!.copy(
                        type = type,
                        amountCents = cents,
                        category = cat,
                        note = note,
                        transactionDateEpoch = date
                    )
                )
                txToEdit = null
            }
        )
    }

    if (txToDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.btn_delete),
            message = stringResource(R.string.delete_transaction_confirm),
            isDestructive = true,
            confirmText = stringResource(R.string.btn_delete),
            onConfirm = {
                viewModel.deleteTransaction(txToDelete!!)
                txToDelete = null
            },
            onDismiss = { txToDelete = null }
        )
    }
}

@Composable
private fun TransactionItemCard(
    tx: TransactionEntity,
    currencyCode: String,
    isBengali: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = tx.type == "INCOME"
    LifeOSCard(
        modifier = Modifier.testTag("tx_item_${tx.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) SuccessGreen.copy(alpha = 0.15f)
                        else ErrorRed.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isIncome) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (tx.note.isNotBlank()) {
                    Text(
                        text = tx.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = DateUtils.formatDisplayDate(tx.transactionDateEpoch, isBengali),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+" else "-") + DateUtils.formatMoney(tx.amountCents, currencyCode, isBengali),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) SuccessGreen else ErrorRed
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
