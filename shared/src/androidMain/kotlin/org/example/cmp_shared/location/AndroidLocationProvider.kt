package org.example.cmp_shared.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidLocationProvider : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationResult {
        val context = runCatching { androidAppContext }.getOrNull()
            ?: return LocationResult.Unavailable
        if (!hasFineLocationPermission(context)) {
            return LocationResult.PermissionDenied
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return LocationResult.Unavailable

        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastKnown != null) {
            return LocationResult.Success(lastKnown.toCoordinates())
        }

        return suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(LocationResult.Success(location.toCoordinates()))
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {
                }

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(LocationResult.Unavailable)
                    }
                }
            }

            continuation.invokeOnCancellation {
                locationManager.removeUpdates(listener)
            }

            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                else -> {
                    continuation.resume(LocationResult.Unavailable)
                    return@suspendCancellableCoroutine
                }
            }

            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                listener,
                Looper.getMainLooper(),
            )
        }
    }

    private fun hasFineLocationPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun Location.toCoordinates(): Coordinates =
        Coordinates(latitude = latitude, longitude = longitude)
}
