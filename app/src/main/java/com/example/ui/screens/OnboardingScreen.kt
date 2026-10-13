package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.LifeOSButton
import com.example.ui.components.LifeOSCard
import com.example.ui.components.LifeOSChip
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SurfaceSecondaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel

@Composable
fun OnboardingScreen(
    viewModel: LifeOSViewModel,
    onComplete: () -> Unit,
    currentLanguage: String
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 24.dp)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header / App Identity
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(NeonLime.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Language Selector
            Text(
                text = stringResource(R.string.onboarding_select_language),
                style = MaterialTheme.typography.labelMedium,
                color = CyanAccent,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LifeOSChip(
                    selected = currentLanguage == "en",
                    text = "English",
                    onClick = { viewModel.setLanguage("en") },
                    modifier = Modifier.weight(1f).testTag("lang_en_chip")
                )
                LifeOSChip(
                    selected = currentLanguage == "bn",
                    text = "বাংলা (Bengali)",
                    onClick = { viewModel.setLanguage("bn") },
                    modifier = Modifier.weight(1f).testTag("lang_bn_chip")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Feature Highlights Cards
            FeatureRow(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.onboarding_feature_tasks),
                subtitle = "Priority filters, due dates, and zero-clutter task execution."
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureRow(
                icon = Icons.Default.Note,
                title = stringResource(R.string.onboarding_feature_notes),
                subtitle = "Pin important ideas and search instant thoughts offline."
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureRow(
                icon = Icons.Default.AccountBalanceWallet,
                title = stringResource(R.string.onboarding_feature_finance),
                subtitle = "Record incomes, categorize expenses, and set monthly limits."
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureRow(
                icon = Icons.Default.Repeat,
                title = stringResource(R.string.onboarding_feature_habits),
                subtitle = "Build daily streaks with calendar-backed completion logic."
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureRow(
                icon = Icons.Default.Flag,
                title = "Milestone Goals",
                subtitle = "Track targeted ambitions with visual progress percentages."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Guarantee Notice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_no_account_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Button
            LifeOSButton(
                text = stringResource(R.string.onboarding_start_button),
                onClick = {
                    viewModel.completeOnboarding()
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "onboarding_start_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = {
                    viewModel.completeOnboarding()
                    onComplete()
                },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    LifeOSCard(
        backgroundColor = SurfaceSecondaryDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonLime.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonLime,
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
