package com.expedia.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

public fun havePermissionToAccessLocation(context: Context): Boolean {
    return isPermissionEnabled(Manifest.permission.ACCESS_FINE_LOCATION, context)
}

private fun isPermissionEnabled(permission: String, context: Context): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    return permissionCheckResult == PackageManager.PERMISSION_GRANTED
}
