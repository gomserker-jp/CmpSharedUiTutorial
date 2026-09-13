package org.example.cmp_shared.util

import org.example.cmp_shared.location.Coordinates
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun distanceMeters(from: Coordinates, to: Coordinates): Double =
        distanceMeters(from.latitude, from.longitude, to.latitude, to.longitude)

    fun distanceMeters(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
    ): Double {
        val latDistanceRadians = (toLatitude - fromLatitude) * PI / 180.0
        val lonDistanceRadians = (toLongitude - fromLongitude) * PI / 180.0

        val fromLatRadians = fromLatitude * PI / 180.0
        val toLatRadians = toLatitude * PI / 180.0

        val haversine =
            sin(latDistanceRadians / 2) * sin(latDistanceRadians / 2) +
                cos(fromLatRadians) * cos(toLatRadians) *
                sin(lonDistanceRadians / 2) * sin(lonDistanceRadians / 2)

        val centralAngle = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
        return EARTH_RADIUS_METERS * centralAngle
    }
}
