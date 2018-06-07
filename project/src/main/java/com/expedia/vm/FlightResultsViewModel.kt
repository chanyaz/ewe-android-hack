package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.FlightRichContentService
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isRichContentEnabled
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class FlightResultsViewModel(context: Context) : BaseResultsViewModel() {

    lateinit var flightRichContentService: FlightRichContentService
        @Inject set

    override val showLoadingStateV1 = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)
    override val showRichContent = isRichContentEnabled(context)
    val richContentStream = PublishSubject.create<Map<String, RichContent>>()
    val sharedPref = context.getSharedPreferences("richContentGuide", Context.MODE_PRIVATE)
    var isRichContentGuideDisplayed = false
    var richContentOutboundSubscription: Disposable? = null
    var richContentInboundSubscription: Disposable? = null

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        if (showRichContent) {
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
                        FlightsV2Tracking.trackRouteHappyResultCountRatio(it.isOutboundResults, Db.getFlightSearchParams().isRoundTrip(),
                                it.richContentLegs.size, it.flightLegs.size)
                    } else {
                        FlightsV2Tracking.trackRouteHappyEmptyResults(it.isOutboundResults, Db.getFlightSearchParams().isRoundTrip())
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
        abortRichContentCallObservable.subscribe {
            richContentOutboundSubscription?.dispose()
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
