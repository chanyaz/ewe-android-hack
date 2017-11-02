package com.expedia.bookings.travelgraph.vm

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResponse
import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResult
import com.expedia.bookings.services.travelgraph.TravelGraphServices
import com.expedia.bookings.utils.Ui
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class TravelGraphViewModel(val context: Context, private val travelGraphServices: TravelGraphServices) {
    val searchHistoryResultSubject = PublishSubject.create<TravelGraphUserHistoryResult>()

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
        return object : DisposableObserver<TravelGraphUserHistoryResponse>() {
            override fun onNext(response: TravelGraphUserHistoryResponse) {
                val searchResult = getHotelSearchHistoryItems(response)
                if (searchResult != null) {
                    searchHistoryResultSubject.onNext(searchResult)
                }
            }

            private fun getHotelSearchHistoryItems(response: TravelGraphUserHistoryResponse): TravelGraphUserHistoryResult? {
                return response.getSearchHistoryResultFor(TravelGraphUserHistoryResponse.TravelGraphItemLOB.HOTEL)
            }

            override fun onComplete() {}

            override fun onError(e: Throwable) {
                //TODO handle errors
            }
        }
    }
}
