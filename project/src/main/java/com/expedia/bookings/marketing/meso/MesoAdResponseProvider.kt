package com.expedia.bookings.marketing.meso

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.enums.MesoDestinationAdState
import com.expedia.bookings.marketing.meso.model.MesoAdResponse
import com.expedia.bookings.marketing.meso.model.MesoDestinationAdResponse
import com.expedia.bookings.marketing.meso.model.MesoHotelAdResponse
import com.expedia.bookings.utils.Constants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.mobiata.android.Log
import io.reactivex.subjects.PublishSubject

open class MesoAdResponseProvider {

    companion object {
        private lateinit var hotelAdLoader: AdLoader
        private lateinit var destinationAdLoader: AdLoader

        @JvmStatic
        fun fetchHotelMesoAd(context: Context, mesoHotelAdResponseSubject: PublishSubject<MesoAdResponse>) {
            val hotelTemplateID = if (BuildConfig.RELEASE) Constants.MESO_PROD_HOTEL_TEMPLATEID else Constants.MESO_DEV_HOTEL_TEMPLATEID

            hotelAdLoader = AdLoader.Builder(context, getUrlPath()).forCustomTemplateAd(hotelTemplateID,
                    { hotelAdResponse ->
                        if (!mesoHotelAdResponseSubject.hasComplete()) {
                            if (hotelAdResponse == null) {
                                mesoHotelAdResponseSubject.onError(Throwable("Hotel Ad Response was null."))
                            } else {
                                val mesoAdResponse = MesoAdResponse(generateHotelAdResponse(hotelAdResponse))
                                mesoHotelAdResponseSubject.onNext(mesoAdResponse)
                            }
                            mesoHotelAdResponseSubject.onComplete()
                        }
                    }) { _, _ -> }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(errorCode: Int) {
                            val errorMessage = "Hotel ad failed to load: " + errorCode
                            Log.d(errorMessage)
                            mesoHotelAdResponseSubject.onComplete()
                        }
                    }).build()

            hotelAdLoader.loadAd(PublisherAdRequest.Builder().build())
        }

        @JvmStatic
        fun fetchDestinationMesoAd(context: Context, mesoDestinationAdResponseSubject: PublishSubject<MesoAdResponse>) {
            val mesoAdResponse = MesoAdResponse(null, generateDestinationAdResponse(context))
            mesoDestinationAdResponseSubject.onNext(mesoAdResponse)
            mesoDestinationAdResponseSubject.onComplete()
        }

        private fun getUrlPath(): String {
            return if (BuildConfig.RELEASE) Constants.MESO_PROD_URL_PATH else Constants.MESO_DEV_URL_PATH
        }

        private fun generateHotelAdResponse(adResponse: NativeCustomTemplateAd): MesoHotelAdResponse =
                MesoHotelAdResponse(adResponse.getImage("BackgroundImage"), adResponse.getText("Headline"), adResponse.getText("HotelID"),
                        adResponse.getText("HotelName"), adResponse.getText("OfferPrice"), adResponse.getText("PercentageOff"),
                        adResponse.getText("PropertyLocation"), adResponse.getText("RegionID"), adResponse.getText("StrikeThroughPrice"))

        private fun generateDestinationAdResponse(context: Context): MesoDestinationAdResponse {
            // randomly pick one destination for meso smoke test
            val destination = MesoDestinationAdState.values()[(Math.random() * MesoDestinationAdState.values().size).toInt()]
            return MesoDestinationAdResponse(context.resources.getString(destination.titleID), context.resources.getString(destination.subtitleID),
                    context.resources.getString(R.string.meso_sponsored_text), destination.webviewUrl, destination.backgroundUrl) }
    }
}
