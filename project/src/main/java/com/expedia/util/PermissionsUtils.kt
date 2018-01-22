package com.expedia.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.expedia.bookings.utils.Constants
import com.mobiata.android.util.SettingUtils

object PermissionsUtils {

    private val PREF_IS_FIRST_ASKING_LOCATION_PERMISSION = "PREF_IS_FIRST_ASKING_LOCATION_PERMISSION"

    @JvmStatic fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.PERMISSION_REQUEST_LOCATION)
    }

    @JvmStatic fun havePermissionToAccessLocation(context: Context): Boolean {
        return isPermissionEnabled(Manifest.permission.ACCESS_FINE_LOCATION, context)
    }

    private fun isPermissionEnabled(permission: String, context: Context): Boolean {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic fun isFirstTimeAskingLocationPermission(context: Context): Boolean {
        val isFirstTime = SettingUtils.get(context, PREF_IS_FIRST_ASKING_LOCATION_PERMISSION, true)
        SettingUtils.save(context, PREF_IS_FIRST_ASKING_LOCATION_PERMISSION, false)
        return isFirstTime
    }
}
