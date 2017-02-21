package com.expedia.bookings.mia.vm

import com.expedia.bookings.data.sos.MemberOnlyDealRequest
import com.expedia.bookings.data.sos.MemberOnlyDealResponse
import com.expedia.bookings.services.sos.SmartOfferService
import rx.Observer
import rx.subjects.PublishSubject

class SmartOfferViewModel(private val smartOfferService: SmartOfferService) {
    val responseSubject = PublishSubject.create<String>()
    private val dealsObserver = DealsObserver()
    private var returnedResponse: MemberOnlyDealResponse? = null

    init {
        dealsObserver.errorSubject.subscribe(responseSubject)
        dealsObserver.successSubject.subscribe(responseSubject)
        dealsObserver.memberOnlyDealResponseSubject.subscribe {
            response -> returnedResponse = response 
        }
    }

    fun fetchDeals() {
        if (returnedResponse != null) {
            dealsObserver.onNext(returnedResponse)
        }
        else {
            val request = MemberOnlyDealRequest()
            smartOfferService.fetchMemberOnlyDeals(request, dealsObserver)
        }
    }

    class DealsObserver : Observer<MemberOnlyDealResponse> {
        val errorSubject = PublishSubject.create<String>()
        val successSubject = PublishSubject.create<String>()
        val memberOnlyDealResponseSubject = PublishSubject.create<MemberOnlyDealResponse>()

        override fun onError(e: Throwable?) {
            errorSubject.onNext("Retrofit error? ${e.toString()}")
        }

        override fun onNext(response: MemberOnlyDealResponse?) {
            if (response == null) {
                errorSubject.onNext("response == null")
            } else if (response.hasError()) {
                errorSubject.onNext(response.errorCode.toString())

            } else {
                memberOnlyDealResponseSubject.onNext(response)
                successSubject.onNext("Success!!")
            }
        }

        override fun onCompleted() {
            // nothing
        }
    }
}