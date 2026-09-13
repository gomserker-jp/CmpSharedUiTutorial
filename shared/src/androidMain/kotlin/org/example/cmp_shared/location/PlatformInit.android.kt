package org.example.cmp_shared.location

import android.content.Context

lateinit var androidAppContext: Context

fun initAndroidContext(context: Context) {
    androidAppContext = context.applicationContext
}

actual fun initPlatformContext() {
    // no-op
}
