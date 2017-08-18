package com.expedia.bookings.utils.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.CarServices
import com.expedia.bookings.tracking.CarWebViewTracking
import com.expedia.ui.CarActivity
import com.expedia.ui.CarWebViewActivity

class CarNavUtils : NavUtils() {
    companion object {

        @JvmStatic fun goToCars(context: Context, animOptions: Bundle?) {
            sendKillActivityBroadcast(context)
            CarWebViewTracking().trackAppCarAAtest();
            CarWebViewTracking().trackAppCarWebViewABTest()
            if (PointOfSale.getPointOfSale().supportsCarsWebView() && Db.getAbacusResponse().isUserBucketedForTest(PointOfSale.getPointOfSale().carsWebViewABTestID)) {
                val builder = CarWebViewActivity.IntentBuilder(context)
                CarWebViewTracking().trackAppCarFlexViewABTest()
                if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppCarsFlexView)) {
                    builder.setUrl("https://www." + PointOfSale.getPointOfSale().url + "/carshomepage?mcicid=App.Cars.WebView")
                } else {
                    builder.setUrl(PointOfSale.getPointOfSale().carsTabWebViewURL)
                }
                builder.setInjectExpediaCookies(true)
                builder.setAllowMobileRedirects(true)
                builder.setLoginEnabled(true)
                builder.setHandleBack(true)
                builder.setRetryOnFailure(true)
                builder.setTitle(context.getString(R.string.nav_car_rentals))
                builder.setTrackingName("CarWebView")
                startActivity(context, builder.intent, null)
            } else {
                val intent = Intent(context, CarActivity::class.java)
                startActivity(context, intent, animOptions)
            }
        }

        @JvmStatic fun goToCars(context: Context, animOptions: Bundle?, searchParams: CarSearchParam?,
                                productKey: String, expediaFlags: Int) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, CarActivity::class.java)
            if (searchParams != null) {
                val gson = CarServices.generateGson()
                intent.putExtra("carSearchParams", gson.toJson(searchParams))
            }

            if (expediaFlags and FLAG_DEEPLINK != 0) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(Codes.FROM_DEEPLINK, true)
            }

            intent.putExtra(Codes.CARS_PRODUCT_KEY, productKey)
            startActivity(context, intent, animOptions)
            finishIfFlagged(context, expediaFlags)
        }

        @JvmStatic fun goToCars(context: Context, animOptions: Bundle?, searchParams: CarSearchParam?,
                                expediaFlags: Int) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, CarActivity::class.java)
            if (searchParams != null) {
                val gson = CarServices.generateGson()
                intent.putExtra("carSearchParams", gson.toJson(searchParams))
                intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
            }

            if (expediaFlags and FLAG_OPEN_SEARCH != 0) {
                intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true)
            }

            if (expediaFlags and FLAG_DEEPLINK != 0) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(Codes.FROM_DEEPLINK, true)
            }

            startActivity(context, intent, animOptions)
            finishIfFlagged(context, expediaFlags)
        }
    }
}