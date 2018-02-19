package com.expedia.bookings.mia.arch

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.data.sos.MemberDealsResponse
import com.expedia.bookings.services.sos.ISmartOfferService
import io.reactivex.observers.DisposableObserver

class MemberDealsArchViewModel(val service: ISmartOfferService, val request: MemberDealsRequest) : ViewModel() {

    val responseLiveData: MutableLiveData<MemberDealsResponse> by lazy {
        val liveData = MutableLiveData<MemberDealsResponse>()
        service.fetchDeals(request, object : DisposableObserver<MemberDealsResponse>() {
            override fun onComplete() {}

            override fun onNext(response: MemberDealsResponse) {
                liveData.value = response
                dispose()
            }
            override fun onError(e: Throwable) {
                dispose()
            }
        })
        liveData
    }

    class Factory(private val service: ISmartOfferService, private val request: MemberDealsRequest) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemberDealsArchViewModel(service, request) as T
        }
    }
}
