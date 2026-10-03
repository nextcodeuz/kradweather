package com.krad.weather

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krad.weather.presentation.navigation.KradWeatherNavHost
import com.krad.weather.presentation.onboarding.OnboardingScreen
import com.krad.weather.presentation.settings.UserPreferences
import com.krad.weather.presentation.theme.KradWeatherTheme
import com.krad.weather.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: UserPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by prefs.theme.collectAsStateWithLifecycle(initialValue = "system")
            val onboardingDone by prefs.onboardingDone.collectAsStateWithLifecycle(initialValue = true)
            val darkTheme = when (theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            KradWeatherTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (!onboardingDone) {
                        OnboardingScreen()
                    } else {
                        KradWeatherNavHost()
                    }
                }
            }
        }
    }
}
