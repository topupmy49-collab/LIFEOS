package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.LifeOSButton
import com.example.ui.components.LifeOSCard
import com.example.ui.components.LifeOSChip
import com.example.ui.components.LifeOSTextField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel

@Composable
fun SettingsScreen(
    viewModel: LifeOSViewModel
) {
    val context = LocalContext.current
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Section: Preferences
        item {
            Text(
                text = stringResource(R.string.section_preferences),
                style = MaterialTheme.typography.labelLarge,
                color = NeonLime,
                fontWeight = FontWeight.Bold
            )
        }

        // Language
        item {
            LifeOSCard {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.setting_language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LifeOSChip(
                            selected = preferences.language == "en",
                            text = stringResource(R.string.setting_language_en),
                            onClick = { viewModel.setLanguage("en") },
                            modifier = Modifier.weight(1f).testTag("settings_lang_en")
                        )
                        LifeOSChip(
                            selected = preferences.language == "bn",
                            text = stringResource(R.string.setting_language_bn),
                            onClick = { viewModel.setLanguage("bn") },
                            modifier = Modifier.weight(1f).testTag("settings_lang_bn")
                        )
                    }
                }
            }
        }

        // Theme
        item {
            LifeOSCard {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = NeonLime, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.setting_theme),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LifeOSChip(
                            selected = preferences.themeMode == "dark",
                            text = stringResource(R.string.theme_dark),
                            onClick = { viewModel.setThemeMode("dark") },
                            modifier = Modifier.weight(1f)
                        )
                        LifeOSChip(
                            selected = preferences.themeMode == "light",
                            text = stringResource(R.string.theme_light),
                            onClick = { viewModel.setThemeMode("light") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Currency
        item {
            LifeOSCard {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.setting_currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BDT", "USD", "EUR", "GBP", "INR").forEach { cur ->
                            LifeOSChip(
                                selected = preferences.currencyCode == cur,
                                text = cur,
                                onClick = { viewModel.setCurrency(cur) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section: Data & Backup
        item {
            Text(
                text = stringResource(R.string.section_data),
                style = MaterialTheme.typography.labelLarge,
                color = CyanAccent,
                fontWeight = FontWeight.Bold
            )
        }

        // Export JSON
        item {
            SettingsActionCard(
                icon = Icons.Default.Download,
                title = stringResource(R.string.data_export_title),
                subtitle = stringResource(R.string.data_export_desc),
                iconColor = CyanAccent,
                onClick = {
                    viewModel.exportData { json ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "LIFEOS_Backup.json")
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        val chooser = Intent.createChooser(shareIntent, "Export LIFEOS Backup")
                        context.startActivity(chooser)
                    }
                },
                testTag = "settings_export_button"
            )
        }

        // Import JSON
        item {
            SettingsActionCard(
                icon = Icons.Default.Upload,
                title = stringResource(R.string.data_import_title),
                subtitle = stringResource(R.string.data_import_desc),
                iconColor = NeonLime,
                onClick = { showImportDialog = true },
                testTag = "settings_import_button"
            )
        }

        // Clear All Data
        item {
            SettingsActionCard(
                icon = Icons.Default.CleaningServices,
                title = stringResource(R.string.data_clear_title),
                subtitle = stringResource(R.string.data_clear_desc),
                iconColor = ErrorRed,
                onClick = { showClearDataDialog = true },
                testTag = "settings_clear_data_button"
            )
        }

        // Section: Security & About
        item {
            Text(
                text = stringResource(R.string.section_privacy),
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Default.Security,
                title = stringResource(R.string.privacy_title),
                subtitle = stringResource(R.string.privacy_desc),
                iconColor = NeonLime,
                onClick = { showPrivacyDialog = true }
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Default.Star,
                title = stringResource(R.string.premium_title),
                subtitle = stringResource(R.string.premium_desc),
                iconColor = Color(0xFFFFB300),
                onClick = { showPremiumDialog = true }
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Default.Info,
                title = "About LIFEOS",
                subtitle = "v${stringResource(R.string.app_version)} • ${stringResource(R.string.about_creator)}",
                iconColor = TextSecondary,
                onClick = { showAboutDialog = true }
            )
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        ConfirmDialog(
            title = stringResource(R.string.data_clear_confirm_title),
            message = stringResource(R.string.data_clear_confirm_message),
            isDestructive = true,
            confirmText = stringResource(R.string.btn_delete),
            onConfirm = {
                viewModel.clearAllData {
                    Toast.makeText(context, context.getString(R.string.data_cleared_success), Toast.LENGTH_SHORT).show()
                }
                showClearDataDialog = false
            },
            onDismiss = { showClearDataDialog = false }
        )
    }

    // Import Backup JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.data_import_title),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste the JSON backup content below to restore your records safely into local database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LifeOSTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = "Backup JSON",
                        placeholder = "{\"version\":1, \"appName\":\"LIFEOS\", ...}",
                        singleLine = false,
                        maxLines = 8,
                        testTag = "import_json_input"
                    )
                }
            },
            containerColor = SurfaceCardDark,
            confirmButton = {
                LifeOSButton(
                    text = stringResource(R.string.btn_confirm),
                    onClick = {
                        viewModel.importData(importJsonText) { result ->
                            if (result.isSuccess) {
                                Toast.makeText(context, "Restored ${result.getOrNull()} items!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                                importJsonText = ""
                            } else {
                                Toast.makeText(context, "Import failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    testTag = "import_confirm_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = TextSecondary)
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text("Security & Privacy", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    text = "LIFEOS is built with zero surveillance. All data (tasks, notes, personal finances, habits, goals) is stored strictly in your device's sandboxed Room SQLite database.\n\nNo accounts, no analytics, no external tracking servers. When you uninstall the app or clear data, records are wiped completely unless exported.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            containerColor = SurfaceCardDark,
            confirmButton = {
                LifeOSButton(text = stringResource(R.string.btn_close), onClick = { showPrivacyDialog = false })
            }
        )
    }

    // Premium Roadmap Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = {
                Text("LIFEOS Premium Roadmap", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    text = "The MVP includes 100% free core features. In future releases, an optional LIFEOS Premium subscription will offer:\n\n• End-to-end encrypted cloud sync\n• Automated Google Drive backup\n• Advanced financial cashflow graphs\n• Custom neon theme variations\n\nNo fake purchases or mocked unlocks in this build.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            containerColor = SurfaceCardDark,
            confirmButton = {
                LifeOSButton(text = stringResource(R.string.btn_close), onClick = { showPremiumDialog = false })
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("LIFEOS", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version: ${stringResource(R.string.app_version)}\nBuild: Native Android Kotlin & Jetpack Compose\nDatabase: Local Room SQLite\nPlatform: Modern Android 14/15/16 Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            containerColor = SurfaceCardDark,
            confirmButton = {
                LifeOSButton(text = stringResource(R.string.btn_close), onClick = { showAboutDialog = false })
            }
        )
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    LifeOSCard(
        onClick = onClick,
        modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
