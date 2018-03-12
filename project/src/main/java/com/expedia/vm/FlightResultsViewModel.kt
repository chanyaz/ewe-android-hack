package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.RouteHappyResponse
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.KongFlightServices
import com.expedia.bookings.utils.RouteHappyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isRouteHappyEnabled
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

class FlightResultsViewModel(context: Context) : BaseResultsViewModel() {

    lateinit var kongFlightServices: KongFlightServices
        @Inject set

    override val showLoadingStateV1 = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        flightResultsObservable.subscribe { flightLegs ->
            if (isRouteHappyEnabled(context) && flightLegs.isNotEmpty()) {
                val routeHappyRequestPayload = RouteHappyUtils.getRouteHappyRequestPayload(context, flightLegs)
                kongFlightServices.getFlightRouteHappy(routeHappyRequestPayload, makeRouteHappyObserver())
            }
        }
    }

    private fun makeRouteHappyObserver(): Observer<RouteHappyResponse> {
        return object : DisposableObserver<RouteHappyResponse>() {
            override fun onComplete() {
                // DO Nothing...
            }

            override fun onNext(resp: RouteHappyResponse) {
                // TODO process for Flight Results
            }

            override fun onError(error: Throwable) {
                // DO Nothing...
            }
        }
    }
}
