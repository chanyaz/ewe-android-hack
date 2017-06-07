package com.expedia.bookings.utils

import android.content.Context
import android.os.Bundle

/**
 * The NavigationHelper is just a wrapper around NavUtils that is non-static, allowing
 * it to be easily mocked for testing purposes.
 */
open class NavigationHelper {

    private var context: Context;

    constructor(context: Context) {
        this.context = context
    }

    open fun goToHotels(animOptions: Bundle?) {
        NavUtils.goToHotels(context, animOptions)
    }

    open fun goToFlights(animOptions: Bundle?) {
        NavUtils.goToFlights(context, animOptions)
    }

    open fun goToCars(animOptions: Bundle?) {
        NavUtils.goToCars(context, animOptions)
    }

    open fun goToActivities(animOptions: Bundle?) {
        NavUtils.goToActivities(context, animOptions)
    }

    open fun goToTransport(animOptions: Bundle?) {
        NavUtils.goToTransport(context, animOptions)
    }

    open fun goToPackages(data: Bundle?, animOptions: Bundle?) {
        NavUtils.goToPackages(context, data, animOptions)
    }

    open fun goToPackagesForResult(data: Bundle?, animOptions: Bundle?, requestCode: Int) {
        NavUtils.goToPackagesForResult(context, data, animOptions, requestCode)
    }

    open fun goToRail(animOptions: Bundle?) {
        NavUtils.goToRail(context, animOptions)
    }
}