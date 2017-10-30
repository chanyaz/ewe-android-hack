package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.BaggageInfoParams
import com.expedia.bookings.data.flights.BaggageInfoResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.services.BaggageInfoService
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject


class BaggageInfoViewModel(val context: Context) {
    lateinit var baggageInfoService: BaggageInfoService
        @Inject set
    var baggageParamsSubject = PublishSubject.create<FlightLeg>()
    var airlineNameSubject = PublishSubject.create<String>()
    var baggageChargeSubject = PublishSubject.create<Pair<String, String>>()
    var showBaggageInfoDialogSubject = PublishSubject.create<Unit>()
    var baggageInfoParamClass = BaggageInfoParams()
    var showBaggageInfoWebViewSubject = PublishSubject.create<Unit>()
    var doNotShowLastHorizontalLineSubject = PublishSubject.create<Boolean>()
    var showLoaderSubject = PublishSubject.create<Boolean>()

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        baggageParamsSubject.subscribe {
            val baggageInfoParam = baggageInfoParamClass.makeBaggageParams(it)
            baggageInfoService.getBaggageInfo(baggageInfoParam, makeBaggageInfoObserver())
        }
    }

    fun makeBaggageInfoObserver(): Observer<BaggageInfoResponse> {
        return object : Observer<BaggageInfoResponse> {
            override fun onNext(response: BaggageInfoResponse) {
                val chargesArraySize = response.charges.size
                if (chargesArraySize == 0) {
                    showBaggageInfoWebViewSubject.onNext(Unit)
                } else {
                    airlineNameSubject.onNext(response.airlineName)
                    showBaggageInfoDialogSubject.onNext(Unit)
                    for ((index, charge) in response.charges.withIndex()) {
                        val baggageChargesKey = charge.keys.toTypedArray()[0]
                        baggageChargeSubject.onNext(Pair(baggageChargesKey, charge.getValue(baggageChargesKey)))
                        if (index == chargesArraySize - 1) {
                            doNotShowLastHorizontalLineSubject.onNext(true)
                        } else {
                            doNotShowLastHorizontalLineSubject.onNext(false)
                        }
                    }
                }
                showLoaderSubject.onNext(false)
            }

            override fun onError(e: Throwable?) {
                showBaggageInfoWebViewSubject.onNext(Unit)
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}