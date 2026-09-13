package org.example.cmp_shared.location

interface LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}
