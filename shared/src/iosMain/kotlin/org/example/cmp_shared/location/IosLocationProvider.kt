package org.example.cmp_shared.location

import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject

private const val CLErrorLocationUnknown = 0L
private const val CLErrorDenied = 1L

@OptIn(ExperimentalForeignApi::class)
class IosLocationProvider : LocationProvider {

    override suspend fun getCurrentLocation(): LocationResult = withContext(Dispatchers.Main) {
        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted,
            -> return@withContext LocationResult.PermissionDenied
        }

        suspendCancellableCoroutine<LocationResult> { continuation ->
            val manager = CLLocationManager()
            var finished = false

            fun complete(result: LocationResult) {
                if (finished || !continuation.isActive) return
                finished = true
                manager.delegate = null
                continuation.resume(result)
            }

            fun requestLocationIfAuthorized() {
                when (CLLocationManager.authorizationStatus()) {
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways,
                    -> manager.requestLocation()
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted,
                    -> complete(LocationResult.PermissionDenied)
                    else -> Unit
                }
            }

            val delegate = LocationManagerDelegate(
                onLocation = { location ->
                    val coordinates = location?.toCoordinates()
                    if (coordinates != null) {
                        complete(LocationResult.Success(coordinates))
                    } else {
                        complete(LocationResult.Unavailable)
                    }
                },
                onError = { error ->
                    when (error.code) {
                        CLErrorLocationUnknown -> Unit
                        CLErrorDenied -> complete(LocationResult.PermissionDenied)
                        else -> complete(LocationResult.Unavailable)
                    }
                },
                onAuthorizationChanged = { requestLocationIfAuthorized() },
            )

            continuation.invokeOnCancellation {
                manager.delegate = null
            }

            manager.delegate = delegate
            manager.desiredAccuracy = kCLLocationAccuracyBest

            when (CLLocationManager.authorizationStatus()) {
                kCLAuthorizationStatusNotDetermined -> manager.requestWhenInUseAuthorization()
                else -> requestLocationIfAuthorized()
            }
        }
    }

    private fun CLLocation.toCoordinates(): Coordinates =
        coordinate.useContents {
            Coordinates(latitude = latitude, longitude = longitude)
        }
}

@OptIn(ExperimentalForeignApi::class)
private class LocationManagerDelegate(
    private val onLocation: (CLLocation?) -> Unit,
    private val onError: (NSError) -> Unit,
    private val onAuthorizationChanged: () -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        onLocation(location)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onError(didFailWithError)
    }

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
        onAuthorizationChanged()
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorizationChanged()
    }
}
