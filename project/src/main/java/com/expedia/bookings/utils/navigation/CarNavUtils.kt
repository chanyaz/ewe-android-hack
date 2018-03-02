package com.expedia.bookings.utils.navigation

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.tracking.CarWebViewTracking
import com.expedia.bookings.utils.WebViewIntentBuilderUtil
import com.expedia.ui.LOBWebViewActivity

class CarNavUtils : NavUtils() {
    companion object {

        @JvmStatic fun goToCars(context: Context, expediaFlags: Int) {
            sendKillActivityBroadcast(context)
            CarWebViewTracking().trackAppCarAAtest()
            var builder = LOBWebViewActivity.IntentBuilder(context)
            CarWebViewTracking().trackAppCarFlexViewABTest()
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppCarsFlexView)) {
                builder.setUrl("https://www." + PointOfSale.getPointOfSale().url + "/carshomepage?mcicid=App.Cars.WebView")
            } else {
                builder.setUrl(PointOfSale.getPointOfSale().carsTabWebViewURL)
            }
            builder = WebViewIntentBuilderUtil.setDefaultWebViewIntentProperties(builder)
            builder.setTitle(context.getString(R.string.nav_car_rentals))
            builder.setTrackingName("CarWebView")
            startActivity(context, builder.intent, null)
            finishIfFlagged(context, expediaFlags)
        }
    }
}
