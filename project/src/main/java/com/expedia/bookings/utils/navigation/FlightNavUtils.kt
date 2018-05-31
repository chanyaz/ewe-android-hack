package com.expedia.bookings.utils.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.flights.activity.FlightShoppingControllerActivity
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.ui.FlightActivity

class FlightNavUtils : NavUtils() {

    companion object {
        @JvmStatic fun goToFlights(context: Context) {
            goToFlights(context, null)
        }

        @JvmStatic fun goToFlights(context: Context, expediaFlags: Int) {
            goToFlights(context, null, expediaFlags, null)
        }

        @JvmStatic fun goToFlights(context: Context, animOptions: Bundle?) {
            goToFlights(context, animOptions, 0, null)
        }

        @JvmStatic fun goToFlights(context: Context, animOptions: Bundle?, expediaFlags: Int) {
            goToFlights(context, animOptions, expediaFlags, null)
        }

        @JvmStatic fun goToFlights(context: Context, params: FlightSearchParams) {
            goToFlights(context, null, 0, params)
        }

        @JvmStatic private fun goToFlights(context: Context, animOptions: Bundle?, expediaFlags: Int,
                                           flightSearchParams: FlightSearchParams?) {
            if (!PointOfSale.getPointOfSale().supports(LineOfBusiness.FLIGHTS)) {
                goToLaunchScreen(context, false, LineOfBusiness.FLIGHTS)
            } else {
                sendKillActivityBroadcast(context)
                val intent: Intent
                //intent = Intent(context, FlightActivity::class.java)
                intent = Intent(context, FlightShoppingControllerActivity::class.java)

                if (flightSearchParams != null) {
                    val gson = FlightsV2DataUtil.generateGson()
                    intent.putExtra(Codes.SEARCH_PARAMS, gson.toJson(flightSearchParams))
                }
                startActivity(context, intent, animOptions)
                finishIfFlagged(context, expediaFlags)
            }
        }
    }
}
