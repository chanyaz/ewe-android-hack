package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.services.FlightRichContentService
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.RichContentUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

abstract class BaseResultsViewModel(context: Context) {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    val updateFlightsStream = PublishSubject.create<Unit>()
    open val doNotOverrideFilterButton = false
    open val showLoadingStateV1 = false
    val richContentGuide = PublishSubject.create<Unit>()
    val abortRichContentOutboundObservable = PublishSubject.create<Unit>()
    val abortRichContentInboundObservable = PublishSubject.create<Unit>()
    abstract fun getLineOfBusiness(): LineOfBusiness
    var isRoutehappyOmnitureTrigerred = false
    val richContentStream = PublishSubject.create<Map<String, RichContent>>()
    var richContentOutboundSubscription: Disposable? = null
    var richContentInboundSubscription: Disposable? = null
    var isRichContentGuideDisplayed = false
    val sharedPref = FlightV2Utils.getRichContentSharedPref(context)
    abstract fun injectComponents(context: Context)
    abstract fun trackRouteHappyResultCountRatio(isOutbound: Boolean, routeHappyCount: Int, totalCount: Int)
    abstract fun trackRouteHappyEmptyResults(isOutboundFlight: Boolean)
    abstract fun shouldShowRichContent(context: Context): Boolean

    lateinit var flightRichContentService: FlightRichContentService
        @Inject set

    init {
        injectComponents(context)
        if (shouldShowRichContent(context)) {
            richContentStream.withLatestFrom(flightResultsObservable, isOutboundResults, { richContentLegs, flightLegs, isOutboundResults ->
                object {
                    val richContentLegs = richContentLegs
                    val flightLegs = flightLegs
                    val isOutboundResults = isOutboundResults
                }
            }).subscribe {
                for (flightLeg in it.flightLegs) {
                    val richContent = it.richContentLegs[flightLeg.legId]
                    if (richContent != null) {
                        flightLeg.richContent = richContent
                    }
                }
                updateFlightsStream.onNext(Unit)
                if (!isRoutehappyOmnitureTrigerred) {
                    if (it.richContentLegs.isNotEmpty()) {
                        trackRouteHappyResultCountRatio(it.isOutboundResults, it.richContentLegs.size, it.flightLegs.size)
                    } else {
                        trackRouteHappyEmptyResults(it.isOutboundResults)
                    }
                }
            }
            ObservableOld.combineLatest(isOutboundResults, flightResultsObservable.filter { it.isNotEmpty() }, { isOutboundResult, flightLegs ->
                val richContentRequestPayload = RichContentUtils.getRichContentRequestPayload(context, flightLegs)
                isRoutehappyOmnitureTrigerred = false
                if (isOutboundResult) {
                    if (showRichContentGuide()) {
                        richContentGuide.onNext(Unit)
                    }
                    updateRichContentCounter()
                    richContentOutboundSubscription = flightRichContentService.getOutboundFlightRichContent(richContentRequestPayload, makeRichContentObserver())
                    richContentOutboundSubscription
                } else {
                    richContentInboundSubscription = flightRichContentService.getInboundFlightRichContent(richContentRequestPayload, makeRichContentObserver())
                    richContentInboundSubscription
                }
            }).subscribe()
        }
        abortRichContentOutboundObservable.subscribe {
            richContentOutboundSubscription?.dispose()
        }
        abortRichContentInboundObservable.subscribe {
            richContentInboundSubscription?.dispose()
        }
    }

    fun makeRichContentObserver(): Observer<RichContentResponse> {
        return object : DisposableObserver<RichContentResponse>() {
            override fun onComplete() {
                // DO Nothing...
            }

            override fun onNext(resp: RichContentResponse) {
                richContentStream.onNext(getRichContentMap(resp.richContentList))
            }

            override fun onError(error: Throwable) {
                richContentStream.onNext(emptyMap<String, RichContent>())
            }
        }
    }

    fun getRichContentMap(richContentList: List<RichContent>): MutableMap<String, RichContent> {
        val richContentMap = mutableMapOf<String, RichContent>()
        for (richContent in richContentList) {
            richContentMap.put(richContent.legId, richContent)
        }
        return richContentMap
    }

    fun showRichContentGuide(): Boolean {
        val counter = sharedPref.getInt("counter", 1)
        if ((counter in 1..4 step 2) && !isRichContentGuideDisplayed) {
            return true
        }
        return false
    }

    fun updateRichContentCounter() {
        if (!isRichContentGuideDisplayed) {
            var counter = sharedPref.getInt("counter", 1)
            counter++
            val editor = sharedPref.edit()
            editor.putInt("counter", counter)
            editor.apply()
            isRichContentGuideDisplayed = true
        }
    }
}
