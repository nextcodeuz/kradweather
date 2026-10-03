package com.krad.weather.presentation.common

import com.krad.weather.domain.model.CityLocation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitySelectionBus @Inject constructor() {
    private val _selected = MutableSharedFlow<CityLocation>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val selected: SharedFlow<CityLocation> = _selected.asSharedFlow()

    fun select(city: CityLocation) {
        _selected.tryEmit(city)
    }

    private val _location = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val locationRequested: SharedFlow<Unit> = _location.asSharedFlow()

    fun requestLocation() {
        _location.tryEmit(Unit)
    }
}
