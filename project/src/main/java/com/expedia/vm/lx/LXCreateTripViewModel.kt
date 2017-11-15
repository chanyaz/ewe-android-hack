package com.expedia.vm.lx

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.LxServices
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.BaseCreateTripViewModel
import rx.Observer
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
        return object : Observer<LXCreateTripResponseV2> {
            override fun onCompleted() {
                showCreateTripDialogObservable.onNext(false)

            }

            override fun onError(e: Throwable) {
                showCreateTripDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        performCreateTrip.onNext(Unit)
                    }
                    val cancelFun = fun() {
                        val activity = context as AppCompatActivity
                        activity.onBackPressed()
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
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