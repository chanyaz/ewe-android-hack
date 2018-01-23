package com.expedia.bookings.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.expedia.bookings.data.Location
import com.google.android.gms.maps.GoogleMap

object GoogleMapsUtil {

    @JvmStatic fun setMyLocationEnabled(context: Context, googleMap: GoogleMap, isMyLocationEnabled: Boolean) {
        if (isLocationPermissionGranted(context)) {
            googleMap.isMyLocationEnabled = isMyLocationEnabled
        }
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    @JvmStatic fun getDirectionsIntent(address: String): Intent? {
        if (TextUtils.isEmpty(address)) {
            return null
        }
        val uri = Uri.parse("http://maps.google.com/maps?daddr=" + address)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        return intent
    }

    @JvmStatic fun getGoogleMapsIntent(location: Location, label: String): Intent {
        val uri = Uri.parse("http://maps.google.com/maps?q=loc:" + location.latitude + "," + location.longitude + "(" + Uri.encode(label) + ")")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return intent
    }
}
