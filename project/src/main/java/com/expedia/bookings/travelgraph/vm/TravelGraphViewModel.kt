package com.expedia.bookings.travelgraph.vm

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResponse
import com.expedia.bookings.services.travelgraph.TravelGraphServices
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.subjects.PublishSubject

class TravelGraphViewModel(val context: Context, private val travelGraphServices: TravelGraphServices) {
    val userHistoryResponseSubject = PublishSubject.create<TravelGraphUserHistoryResponse>()

    fun fetchUserHistory() {
        //fetch history only if a user is logged in
        val userManager = Ui.getApplication(context).appComponent().userStateManager()
        val userId = userManager.getExpediaUserId()
        if (!userId.isNullOrBlank()) {
            travelGraphServices.fetchUserHistory(userId!!,
                    Integer.toString(PointOfSale.getPointOfSale().siteId),
                    PointOfSale.getPointOfSale().localeIdentifier,
                    createTravelGraphResponseObserver())
        }
    }

    private fun createTravelGraphResponseObserver(): Observer<TravelGraphUserHistoryResponse> {
        return object : Observer<TravelGraphUserHistoryResponse> {
            override fun onNext(response: TravelGraphUserHistoryResponse) {
                //TODO extract useful stuff. Handle invalid results?
                userHistoryResponseSubject.onNext(response)
            }

            override fun onCompleted() {}

            override fun onError(e: Throwable?) {
                //TODO handle errors
            }
        }
    }
}