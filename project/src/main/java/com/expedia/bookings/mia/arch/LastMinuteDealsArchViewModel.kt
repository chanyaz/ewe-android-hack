package com.expedia.bookings.mia.arch

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.services.os.IOfferService
import com.expedia.bookings.tracking.OmnitureTracking
import io.reactivex.observers.DisposableObserver

class LastMinuteDealsArchViewModel(val service: IOfferService, val request: LastMinuteDealsRequest) : ViewModel() {

    val responseLiveData: MutableLiveData<LastMinuteDealsResponse> by lazy {
        val liveData = MutableLiveData<LastMinuteDealsResponse>()
        service.fetchDeals(request, object : DisposableObserver<LastMinuteDealsResponse>() {
            override fun onComplete() {}
            override fun onNext(response: LastMinuteDealsResponse) {
                liveData.value = response
                if (response.offers.hotels.isEmpty()) {
                    OmnitureTracking.trackLastMinuteDealsNoResults()
                }
                dispose()
            }
            override fun onError(e: Throwable) {
                OmnitureTracking.trackLastMinuteDealsError()
                dispose()
            }
        })
        liveData
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val service: IOfferService, private val request: LastMinuteDealsRequest) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LastMinuteDealsArchViewModel(service, request) as T
        }
    }
}
