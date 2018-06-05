package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.FlightRichContentService
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isRichContentEnabled
import io.reactivex.Observer
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

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        if (showRichContent) {
            ObservableOld.combineLatest(isOutboundResults, flightResultsObservable.filter { it.isNotEmpty() }, { isOutboundResult, flightLegs ->
                val richContentRequestPayload = RichContentUtils.getRichContentRequestPayload(context, flightLegs)
                if (isOutboundResult) {
                    if (showRichContentGuide()) {
                        richContentGuide.onNext(Unit)
                    }
                    updateRichContentCounter()
                    flightRichContentService.getOutboundFlightRichContent(richContentRequestPayload, makeRichContentObserver())
                } else {
                    flightRichContentService.getInboundFlightRichContent(richContentRequestPayload, makeRichContentObserver())
                }
            }).subscribe()

            richContentStream.withLatestFrom(flightResultsObservable, { richContentLegs, flightLegs ->
                object {
                    val richContentLegs = richContentLegs
                    val flightLegs = flightLegs
                }
            }).subscribe {
                for (flightLeg in it.flightLegs) {
                    val richContent = it.richContentLegs[flightLeg.legId]
                    if (richContent != null) {
                        flightLeg.richContent = richContent
                    }
                }
                updateFlightsStream.onNext(Unit)
            }
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
