package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.BaseCreateTripViewModel
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class FlightCreateTripViewModel(val context: Context) : BaseCreateTripViewModel() {

    lateinit var flightServices: FlightServices
        @Inject set

    val tripParams = BehaviorSubject.create<FlightCreateTripParams>()
    val showNoInternetRetryDialog = PublishSubject.create<Unit>()
    val showCreateTripDialogIfNotBucketed = PublishSubject.create<Boolean>()

    init {
        Ui.getApplication(context).flightComponent().inject(this)

        showCreateTripDialogIfNotBucketed.filter { !FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightRateDetailsFromCache, R.string.preference_flight_rate_detail_from_cache) }.subscribe {
            showCreateTripDialogObservable.onNext(it)
        }
        performCreateTrip.subscribe {
            showCreateTripDialogIfNotBucketed.onNext(true)
            flightServices.createTrip(tripParams.value, makeCreateTripResponseObserver())
        }
    }

    fun makeCreateTripResponseObserver(): Observer<FlightCreateTripResponse> {
        return object : DisposableObserver<FlightCreateTripResponse>() {
            override fun onNext(response: FlightCreateTripResponse) {
                showCreateTripDialogIfNotBucketed.onNext(false)
                if (response.hasErrors() && !response.hasPriceChange()) {
                    val error = response.firstError
                    createTripErrorObservable.onNext(error)
                } else {
                    Db.getTripBucket().clearFlight()
                    response.isFareFamilyUpgraded = (Strings.isNotEmpty(tripParams.value.fareFamilyCode) && response.createTripStatus != FlightTripResponse.CreateTripError.FARE_FAMILY_UNAVAILABLE)
                    Db.getTripBucket().add(TripBucketItemFlightV2(response))
                    createTripResponseObservable.onNext(Optional(response))
                }
            }

            override fun onError(e: Throwable) {
                showCreateTripDialogIfNotBucketed.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    FlightsV2Tracking.trackFlightCreateTripNoResponseError()
                    showNoInternetRetryDialog.onNext(Unit)
                }
            }

            override fun onComplete() {
                // ignore
            }
        }
    }
}
