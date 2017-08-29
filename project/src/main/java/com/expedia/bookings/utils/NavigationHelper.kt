package com.expedia.bookings.utils

import android.content.Context
import android.os.Bundle
import com.expedia.bookings.utils.navigation.CarNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.navigation.FlightNavUtils
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.PackageNavUtils

/**
 * The NavigationHelper is just a wrapper around NavUtils that is non-static, allowing
 * it to be easily mocked for testing purposes.
 */
open class NavigationHelper(private var context: Context) {

    open fun goToHotels(animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            HotelNavUtils.goToHotels(context, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            HotelNavUtils.goToHotels(context, animOptions)
        }
    }

    open fun goToFlights(animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            FlightNavUtils.goToFlights(context, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            FlightNavUtils.goToFlights(context, animOptions)
        }
    }

    open fun goToCars(animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            CarNavUtils.goToCars(context, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            CarNavUtils.goToCars(context, animOptions, 0)
        }
    }

    open fun goToActivities(animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            LXNavUtils.goToActivities(context, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            LXNavUtils.goToActivities(context, animOptions, 0)
        }
    }

    open fun goToTransport(animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            NavUtils.goToTransport(context, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            NavUtils.goToTransport(context, animOptions, 0)
        }
    }

    open fun goToPackages(data: Bundle?, animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            PackageNavUtils.goToPackages(context, data, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            PackageNavUtils.goToPackages(context, data, animOptions)
        }
    }

    open fun goToPackagesForResult(data: Bundle?, animOptions: Bundle?, requestCode: Int) {
        PackageNavUtils.goToPackagesForResult(context, data, animOptions, requestCode)
    }

    open fun goToRail(animOptions: Bundle?, finishCallingActivity: Boolean = false) {
        if (finishCallingActivity) {
            NavUtils.goToRail(context, animOptions, NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK)
        } else {
            NavUtils.goToRail(context, animOptions, 0)
        }
    }
}