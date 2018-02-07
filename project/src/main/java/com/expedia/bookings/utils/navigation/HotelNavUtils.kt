package com.expedia.bookings.utils.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.deeplink.HotelLandingPage
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.ui.HotelActivity

class HotelNavUtils : NavUtils() {
    companion object {
        @JvmStatic fun goToHotels(context: Context, expediaFlags: Int) {
            goToHotelsV2Params(context, null, null, expediaFlags)
        }

        @JvmStatic fun goToHotels(context: Context, params: HotelSearchParams) {
            goToHotels(context, params, null, 0)
        }

        @JvmStatic fun goToHotels(context: Context, animOptions: Bundle?) {
            goToHotelsV2Params(context, null, animOptions, 0)
        }

        @JvmStatic fun goToHotels(context: Context, animOptions: Bundle?, expediaFlags: Int) {
            goToHotelsV2Params(context, null, animOptions, expediaFlags)
        }

        @JvmStatic
        fun goToHotels(context: Context, oldParams: HotelSearchParams?,
                       animOptions: Bundle?, expediaFlags: Int) {
            var v2params: com.expedia.bookings.data.hotels.HotelSearchParams? = null
            if (oldParams != null) {
                v2params = HotelsV2DataUtil.getHotelV2SearchParams(context, oldParams)
            }
            goToHotelsV2Params(context, v2params, animOptions, expediaFlags)
        }

        @JvmStatic fun goToHotelsV2Params(context: Context, params: com.expedia.bookings.data.hotels.HotelSearchParams?,
                                          animOptions: Bundle?, expediaFlags: Int) {
            sendKillActivityBroadcast(context)

            val intent = Intent()

            if (expediaFlags and FLAG_DEEPLINK != 0) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(Codes.FROM_DEEPLINK, true)
            }

            if (expediaFlags and FLAG_OPEN_SEARCH != 0) {
                intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true)
            }

            if (expediaFlags and DEAL_SEARCH != 0) {
                intent.putExtra(Codes.DEALS, true)
            }

            if (expediaFlags and FLAG_PINNED_SEARCH_RESULTS != 0) {
                intent.putExtra(HotelExtras.LANDING_PAGE, HotelLandingPage.RESULTS.id)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(Codes.FROM_DEEPLINK, true)
            }

            val routingTarget = HotelActivity::class.java
            if (params != null) {
                val gson = HotelsV2DataUtil.generateGson()
                intent.putExtra(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS, gson.toJson(params))
                intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
            }

            // Launch activity based on routing selection
            intent.setClass(context, routingTarget)
            startActivity(context, intent, animOptions)
            finishIfFlagged(context, expediaFlags)
        }

        @JvmStatic fun goToHotels(context: Context, intent: Intent) {
            startActivity(context, intent, null)
        }
    }
}
