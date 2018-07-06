package com.expedia.bookings.utils

import android.app.Application
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp.isAutomation
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.FacebookEvents
import com.tune.Tune
import com.tune.ma.application.TuneActivityLifecycleCallbacks

object TrackingUtils {

    @JvmStatic
    fun initializeTracking(app: Application) {
        if (!isAutomation()) {
            FacebookEvents.init(app)
            initializeTuneTracking(app)
        }
    }

    @JvmStatic
    private fun initializeTuneTracking(app: Application) {
        app.registerActivityLifecycleCallbacks(TuneActivityLifecycleCallbacks())
        val advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id)
        val conversionKey = app.getString(R.string.tune_sdk_app_conversion_key)

        val tune = Tune.init(app, advertiserID, conversionKey)
        val userStateManager = Ui.getApplication(app.applicationContext).appComponent().userStateManager()
        val shouldSetExistingUser = ProductFlavorFeatureConfiguration.getInstance().shouldSetExistingUserForTune()

        val provider = TuneTrackingProviderImpl(tune, app, userStateManager, shouldSetExistingUser)

        TuneUtils.init(provider)
    }

    @JvmStatic
    private fun getSingleInsuranceProductString(insurance: AbstractItinDetailsResponse.ResponseData.Insurance): String {
        var productString = ";Insurance:"
        val productId = insurance.insuranceTypeId
        val units = insurance.travellerCount
        val price = insurance.price?.total

        productString += "$productId;"
        productString += if (units == null) ";;" else "$units;"
        productString += price ?: ";"
        return productString
    }

    @JvmStatic
    fun getInsuranceProductsString(insurances: List<AbstractItinDetailsResponse.ResponseData.Insurance>): String {
        var productsString = ""
        for (i in 0.until(insurances.size)) {
            val prodString = getSingleInsuranceProductString(insurances[i])
            productsString += if (i != insurances.lastIndex) "$prodString," else prodString
        }
        return productsString
    }
}
