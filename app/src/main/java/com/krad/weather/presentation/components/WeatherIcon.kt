package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ob-havo ikonkasi — maxsus chizilgan vektor art (oflayn, tez, tiniq).
 * @param animated true bo'lsa quyosh nurlari aylanadi, yomg'ir/qor yog'adi (asosiy ekranda).
 */
@Composable
fun WeatherIcon(
    iconCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    animated: Boolean = false
) {
    WeatherArt(
        iconCode = iconCode,
        modifier = modifier.size(size),
        animated = animated
    )
}
