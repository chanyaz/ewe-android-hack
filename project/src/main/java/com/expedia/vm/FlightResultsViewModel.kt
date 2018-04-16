package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.KongFlightServices
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

    lateinit var kongFlightServices: KongFlightServices
        @Inject set

    override val showLoadingStateV1 = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)
    override val showRichContent = isRichContentEnabled(context)
    val richContentStream = PublishSubject.create<Map<String, RichContent>>()

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        flightResultsObservable.subscribe { flightLegs ->
            if (isRichContentEnabled(context) && flightLegs.isNotEmpty()) {
                val richContentRequestPayload = RichContentUtils.getRichContentRequestPayload(context, flightLegs)
                kongFlightServices.getFlightRichContent(richContentRequestPayload, makeRichContentObserver())
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
