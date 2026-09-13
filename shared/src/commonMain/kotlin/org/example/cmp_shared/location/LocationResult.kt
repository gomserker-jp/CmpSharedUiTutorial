package org.example.cmp_shared.location

sealed interface LocationResult {
    data class Success(val coordinates: Coordinates) : LocationResult
    data object PermissionDenied : LocationResult
    data object Unavailable : LocationResult
}
