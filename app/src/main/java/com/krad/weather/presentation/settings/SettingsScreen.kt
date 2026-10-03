package com.krad.weather.presentation.settings

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.krad.weather.BuildConfig
import com.krad.weather.R
import com.krad.weather.presentation.components.GlassCard
import com.krad.weather.presentation.components.WeatherBackground
import com.krad.weather.util.BackgroundPalette

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notifPermission = if (Build.VERSION.SDK_INT >= 33) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    WeatherBackground(palette = BackgroundPalette.NIGHT) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(top = 12.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                stringResource(R.string.settings),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.theme), color = Color.White, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "system" to stringResource(R.string.theme_auto),
                            "light" to stringResource(R.string.theme_light),
                            "dark" to stringResource(R.string.theme_dark)
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = state.theme == key,
                                onClick = { viewModel.setTheme(key) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.units_title), color = Color.White, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("metric" to "°C", "imperial" to "°F", "standard" to "K").forEach { (key, label) ->
                            FilterChip(
                                selected = state.units == key,
                                onClick = { viewModel.setUnits(key) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.lang_title), color = Color.White, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("uz" to "O'zbekcha", "ru" to "Русский", "en" to "English").forEach { (key, label) ->
                            FilterChip(
                                selected = state.language == key,
                                onClick = { viewModel.setLanguage(key) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.dynamic_bg), color = Color.White, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(R.string.dyn_bg_desc),
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(checked = state.dynamicBackground, onCheckedChange = viewModel::setDynamicBackground)
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.daily_notification), color = Color.White, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(R.string.daily_notification_desc),
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.notifications,
                        onCheckedChange = {
                            if (it && notifPermission != null && !notifPermission.status.isGranted) {
                                notifPermission.launchPermissionRequest()
                            }
                            viewModel.setNotifications(it)
                        }
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.developer), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(stringResource(R.string.developer_name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    androidx.compose.material3.TextButton(onClick = { uriHandler.openUri("https://t.me/machine_dev") }) {
                        Text(stringResource(R.string.developer_link), color = Color(0xFF90CAF9), fontSize = 13.sp)
                    }
                }
            }

            Text(
                text = stringResource(R.string.footer_fmt, BuildConfig.VERSION_NAME),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
