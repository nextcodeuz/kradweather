package com.krad.weather.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krad.weather.R
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.FavoriteWithWeather
import com.krad.weather.presentation.components.GlassCard
import com.krad.weather.presentation.components.WeatherBackground
import com.krad.weather.presentation.components.WeatherIcon
import com.krad.weather.util.BackgroundPalette
import com.krad.weather.util.WeatherFormatters

@Composable
fun FavoritesScreen(
    onNavigateHome: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    WeatherBackground(palette = BackgroundPalette.NIGHT) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            Text(
                stringResource(R.string.favorites),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (state.cities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_favorites),
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        state.cities,
                        key = { "${it.city.name}|${it.city.lat}|${it.city.lon}" }
                    ) { fav ->
                        FavoriteRow(
                            fav = fav,
                            units = state.units,
                            onClick = {
                                viewModel.select(fav.city)
                                onNavigateHome()
                            },
                            onRemove = { viewModel.remove(fav.city) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    fav: FavoriteWithWeather,
    units: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val city: CityLocation = fav.city
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (fav.icon != null) {
                WeatherIcon(iconCode = fav.icon, size = 44.dp)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = city.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(city.state, city.country).joinToString(", "),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (fav.temp != null) {
                Text(
                    text = WeatherFormatters.temp(
                        WeatherFormatters.convertTemp(fav.temp, fav.tempUnits, units),
                        WeatherFormatters.unitLabel(units)
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.fav_delete), tint = Color(0xFFFF8A80))
            }
        }
    }
}
