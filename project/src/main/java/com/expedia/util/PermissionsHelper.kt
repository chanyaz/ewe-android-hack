package com.expedia.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.expedia.bookings.utils.Constants

fun requestLocationPermission(activity: Activity) {
    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.PERMISSION_REQUEST_LOCATION)
}

fun havePermissionToAccessLocation(context: Context): Boolean {
    return isPermissionEnabled(Manifest.permission.ACCESS_FINE_LOCATION, context)
}

private fun isPermissionEnabled(permission: String, context: Context): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    return permissionCheckResult == PackageManager.PERMISSION_GRANTED
}
