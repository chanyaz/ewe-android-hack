package com.expedia.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.expedia.bookings.utils.Constants

fun requestLocationPermission(activity: Activity) {
    val requestCode = if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
        Constants.PERMISSION_REQUEST_LOCATION_WITH_RATIONALE
    } else {
        Constants.PERMISSION_REQUEST_LOCATION
    }

    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
}

fun requestWriteToExternalStoragePermission(activity: Activity) {
    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.PERMISSION_WRITE_EXTERNAL_STORAGE_BUGSHAKER)
}

fun requestLocationPermission(fragment: Fragment) {
    // fragment won't get the callback if the activity is the one making the permission request
    // use ActivityCompat.requestPermissions if you need an activity to get the callback
    fragment.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.PERMISSION_REQUEST_LOCATION)
}

fun hasPermissionToWriteToExternalStorage(context: Context): Boolean {
    return isPermissionEnabled(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)
}

fun havePermissionToAccessLocation(context: Context): Boolean {
    return isPermissionEnabled(Manifest.permission.ACCESS_FINE_LOCATION, context)
}

private fun isPermissionEnabled(permission: String, context: Context): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    return permissionCheckResult == PackageManager.PERMISSION_GRANTED
}
