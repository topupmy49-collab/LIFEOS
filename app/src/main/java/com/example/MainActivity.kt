package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LifeOSApp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.LifeOSTheme
import com.example.ui.viewmodel.LifeOSViewModel
import com.example.ui.viewmodel.LifeOSViewModelFactory
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: LifeOSViewModel by viewModels {
        LifeOSViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val isDark = when (preferences.themeMode) {
                "light" -> false
                "system" -> isSystemInDarkTheme()
                else -> true
            }

            // Create localized context for dynamic in-app language switching
            val currentContext = LocalContext.current
            val localizedContext = rememberLocalizedContext(currentContext, preferences.language)

            CompositionLocalProvider(LocalContext provides localizedContext) {
                LifeOSTheme(darkTheme = isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = BackgroundDark
                    ) {
                        LifeOSApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

private fun rememberLocalizedContext(context: Context, languageCode: String): Context {
    val locale = if (languageCode == "bn") Locale("bn", "BD") else Locale.ENGLISH
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
}
