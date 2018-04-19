package com.expedia.bookings.travelPulse.vm

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.travelpulse.TravelPulseFetchResponse
import com.expedia.bookings.services.TravelPulseServices
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver

class TravelPulseViewModel(private val context: Context, private val travelPulseServices: TravelPulseServices) {

    fun fetchFavoriteHotels() {
        val userManager = Ui.getApplication(context).appComponent().userStateManager()
        val userId = userManager.getExpediaUserId()
        if (!userId.isNullOrBlank()) {
            val siteId = ServicesUtil.generateSiteId()
            val clientId = ServicesUtil.getTravelPulseClientId(context)
            val expUserId = userId!!
            val guid = Db.sharedInstance.abacusGuid
            val langId = ServicesUtil.generateLangId()
            val configId = "hotel"

            travelPulseServices.fetchFavoriteHotels(siteId, clientId, expUserId, guid, langId, configId,
                    createTravelPulseResponseObserver())
        }
    }

    private fun createTravelPulseResponseObserver(): Observer<TravelPulseFetchResponse> {
        return object : DisposableObserver<TravelPulseFetchResponse>() {
            override fun onNext(response: TravelPulseFetchResponse) {
                System.out.println(response)
            }

            override fun onComplete() {}

            override fun onError(e: Throwable) {
                System.out.println(e)
            }
        }
    }
}
