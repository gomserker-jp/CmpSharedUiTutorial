package org.example.cmp_shared.location

actual fun createLocationProvider(): LocationProvider = AndroidLocationProvider()
