package com.expedia.bookings.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.GoogleMap

public object GoogleMapsUtil {

    @JvmStatic public fun setMyLocationEnabled(context: Context, googleMap: GoogleMap, isMyLocationEnabled: Boolean) {
        if (isLocationPermissionGranted(context)) {
            googleMap?.isMyLocationEnabled = isMyLocationEnabled
        }
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }
}