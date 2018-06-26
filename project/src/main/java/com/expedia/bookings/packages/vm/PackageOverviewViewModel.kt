package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class PackageOverviewViewModel(private val context: Context) {
    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContDescSubject = PublishSubject.create<String>()
    val performMIDCreateTripSubject = PublishSubject.create<Unit>()
    val bottomCheckoutContainerStateObservable = PublishSubject.create<TwoScreenOverviewState>()
    val checkoutErrorObservable = PublishSubject.create<ApiError>()
    val obFeeDetailsUrlSubject = BehaviorSubject.create<String>()
    private val e3Endpoint: String by lazy {
        Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl
    }

    fun updateMayChargeFees(selectedFlight: FlightLeg) {
        if (selectedFlight.airlineMessageModel?.hasAirlineWithCCfee == true || selectedFlight.mayChargeObFees) {
            val hasAirlineFeeLink = !selectedFlight.airlineMessageModel?.airlineFeeLink.isNullOrBlank()
            if (hasAirlineFeeLink) {
                obFeeDetailsUrlSubject.onNext(e3Endpoint + selectedFlight.airlineMessageModel.airlineFeeLink)
            }
//            val paymentFeeText = context.resources.getString(R.string.payment_and_baggage_fees_may_apply)
//            selectedFlightChargesFees.onNext(paymentFeeText)
        } else {
            obFeeDetailsUrlSubject.onNext("")
//            selectedFlightChargesFees.onNext("")
        }
    }
}
