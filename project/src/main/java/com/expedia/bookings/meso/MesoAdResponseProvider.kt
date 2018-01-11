package com.expedia.bookings.meso

import android.accounts.NetworkErrorException
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.enums.MesoDestinationAdState
import com.expedia.bookings.meso.model.MesoAdResponse
import com.expedia.bookings.meso.model.MesoDestinationAdResponse
import com.expedia.bookings.meso.model.MesoHotelAdResponse
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
            hotelAdLoader = AdLoader.Builder(context, Constants.MESO_DEV_URL_PATH).forCustomTemplateAd(Constants.MESO_DEV_HOTEL_TEMPLATEID,
                    { hotelAdResponse ->
                        if (hotelAdResponse == null) {
                            mesoHotelAdResponseSubject.onError(Throwable("Hotel Ad Response was null."))
                        } else {
                            val mesoAdResponse = MesoAdResponse(generateHotelAdResponse(hotelAdResponse))
                            mesoHotelAdResponseSubject.onNext(mesoAdResponse)
                        }
                        mesoHotelAdResponseSubject.onComplete()
                    }) { hotelAdResponse, s -> }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(errorCode: Int) {
                            val errorMessage = "Hotel ad failed to load: " + errorCode
                            Log.d(errorMessage)
                            mesoHotelAdResponseSubject.onError(NetworkErrorException(errorMessage))
                        }
                    }).build()

            hotelAdLoader.loadAd(PublisherAdRequest.Builder().build())
        }

        @JvmStatic
        fun fetchDestinationMesoAd(context: Context, mesoDestinationAdResponseSubject: PublishSubject<MesoAdResponse>) {
            destinationAdLoader = AdLoader.Builder(context, Constants.MESO_DEV_URL_PATH).forCustomTemplateAd(Constants.MESO_DEV_DESTINATION_TEPLATEID,
                    { destinationAdResponse ->
                        if (destinationAdResponse == null) {
                            mesoDestinationAdResponseSubject.onError(Throwable("Destination Ad Response was null."))
                        } else {
                            val mesoAdResponse = MesoAdResponse(null, generateDestinationAdResponse(context))
                            mesoDestinationAdResponseSubject.onNext(mesoAdResponse)
                        }
                        mesoDestinationAdResponseSubject.onComplete()
                    }) { destinationAdResponse, s -> }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(errorCode: Int) {
                            val errorMessage = "Destination ad failed to load: " + errorCode
                            Log.d(errorMessage)
                            mesoDestinationAdResponseSubject.onError(NetworkErrorException(errorMessage))
                        }
                    }).build()

            destinationAdLoader.loadAd(PublisherAdRequest.Builder().build())
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
