package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.FlightRichContentService
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isRichContentEnabled
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.extensions.ObservableOld
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

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        if (showRichContent) {
            ObservableOld.combineLatest(isOutboundResults, flightResultsObservable.filter { it.isNotEmpty() }, { isOutboundResult, flightLegs ->
                val richContentRequestPayload = RichContentUtils.getRichContentRequestPayload(context, flightLegs)
                if (isOutboundResult) {
                    flightRichContentService.getOutboundFlightRichContent(richContentRequestPayload, makeRichContentObserver())
                } else {
                    flightRichContentService.getInboundFlightRichContent(richContentRequestPayload, makeRichContentObserver())
                }
            }).subscribe()

            ObservableOld.zip(richContentStream, flightResultsObservable, { richContentLegs, flightLegs ->
                for (flightLeg in flightLegs) {
                    val richContent = richContentLegs[flightLeg.legId]
                    if (richContent != null) {
                        flightLeg.richContent = richContent
                    }
                }
                updateFlightsStream.onNext(Unit)
            }).subscribe()
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
}
