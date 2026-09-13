package org.example.cmp_shared.ui

import org.example.cmp_shared.location.Coordinates
import org.example.cmp_shared.location.LocationResult

sealed interface UserLocationState {
    data object Loading : UserLocationState
    data class Success(val coordinates: Coordinates) : UserLocationState
    data object PermissionDenied : UserLocationState
    data object Unavailable : UserLocationState
}

internal fun LocationResult.toUserLocationState(): UserLocationState = when (this) {
    is LocationResult.Success -> UserLocationState.Success(coordinates)
    LocationResult.PermissionDenied -> UserLocationState.PermissionDenied
    LocationResult.Unavailable -> UserLocationState.Unavailable
}

internal val UserLocationState.coordinatesOrNull: Coordinates?
    get() = (this as? UserLocationState.Success)?.coordinates
