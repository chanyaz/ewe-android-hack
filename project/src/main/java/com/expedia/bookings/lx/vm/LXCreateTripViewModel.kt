package com.expedia.bookings.lx.vm

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.services.LxServices
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.BaseCreateTripViewModel
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

class LXCreateTripViewModel(val context: Context) : BaseCreateTripViewModel() {

    lateinit var lxServices: LxServices
        @Inject set

    lateinit var lxState: LXState
        @Inject set

    init {
        Ui.getApplication(context).lxComponent().inject(this)

        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
            lxServices.createTripV2(lxState.createTripParams(context), lxState.originalTotalPrice(), makeCreateTripResponseObserver())
        }
    }

    private fun makeCreateTripResponseObserver(): Observer<LXCreateTripResponseV2> {
        return object : DisposableObserver<LXCreateTripResponseV2>() {
            override fun onComplete() {
                showCreateTripDialogObservable.onNext(false)
            }

            override fun onError(e: Throwable) {
                showCreateTripDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    noNetworkObservable.onNext(Unit)
                }
            }

            override fun onNext(response: LXCreateTripResponseV2) {
                Db.getTripBucket().clearLX()
                showCreateTripDialogObservable.onNext(false)
                createTripResponseObservable.onNext(Optional(response))
            }
        }
    }
}
