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
import com.expedia.bookings.data.flights.RouteHappyRichContent
import com.expedia.bookings.extensions.ObservableOld
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class FlightResultsViewModel(context: Context) : BaseResultsViewModel() {

    lateinit var kongFlightServices: KongFlightServices
        @Inject set

    override val showLoadingStateV1 = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)
    override val showRichContent = isRouteHappyEnabled(context)
    val richContentStream = PublishSubject.create<Map<String, RouteHappyRichContent>>()

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
        if (showRichContent) {
            ObservableOld.zip(richContentStream, flightResultsObservable, { richContentLegs, flightLegs ->
                for (flightLeg in flightLegs) {
                    val richContent = richContentLegs[flightLeg.legId]
                    if (richContent != null) {
                        flightLeg.richContent = richContent
                    }
                }
                updateFlightsStream.onNext(flightLegs.size - 1)
            }).subscribe()
        }
    }

    fun makeRouteHappyObserver(): Observer<RouteHappyResponse> {
        return object : DisposableObserver<RouteHappyResponse>() {
            override fun onComplete() {
                // DO Nothing...
            }

            override fun onNext(resp: RouteHappyResponse) {
                richContentStream.onNext(getRichContentMap(resp.richContentList))
            }

            override fun onError(error: Throwable) {
                richContentStream.onNext(emptyMap<String, RouteHappyRichContent>())
            }
        }
    }

    fun getRichContentMap(richContentList: List<RouteHappyRichContent>): MutableMap<String, RouteHappyRichContent> {
        val richContentMap = mutableMapOf<String, RouteHappyRichContent>()
        for (richContent in richContentList) {
            richContentMap.put(richContent.legId, richContent)
        }
        return richContentMap
    }
}
