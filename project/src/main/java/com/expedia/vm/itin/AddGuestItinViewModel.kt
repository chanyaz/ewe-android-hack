package com.expedia.vm.itin

import android.content.Context
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject

class AddGuestItinViewModel(val context: Context) {

    val showSearchDialogObservable = PublishSubject.create<Boolean>()
    val performGuestTripSearch = PublishSubject.create<Pair<String, String>>()

    lateinit var tripServices: ItinTripServices
        @Inject set

    init {
        Ui.getApplication(context).tripComponent().inject(this)

        performGuestTripSearch.subscribe { guestEmailItinNumPair ->
            showSearchDialogObservable.onNext(true)
            tripServices.getGuestTrip(guestEmailItinNumPair.first, guestEmailItinNumPair.second, makeGuestTripResponseObserver())
        }
    }

    fun makeGuestTripResponseObserver(): Observer<AbstractItinDetailsResponse> {
        return object : Observer<AbstractItinDetailsResponse> {
            override fun onCompleted() {
                showSearchDialogObservable.onNext(false)
            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
                showSearchDialogObservable.onNext(false)
            }

            override fun onNext(t: AbstractItinDetailsResponse?) {
            }
        }

    }
}