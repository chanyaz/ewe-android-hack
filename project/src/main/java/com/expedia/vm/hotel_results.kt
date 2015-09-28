package com.expedia.vm

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class HotelResultsViewModel(private val context: Context, private val hotelServices: HotelServices) {

    // Inputs
    val paramsSubject = PublishSubject.create<HotelSearchParams>()

    // Outputs
    private val hotelDownloadsObservable = PublishSubject.create<Observable<HotelSearchResponse>>()
    private val hotelDownloadResultsObservable = Observable.concat(hotelDownloadsObservable)
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()

    val titleSubject = PublishSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            titleSubject.onNext(params.suggestion.regionNames.shortName)

            subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                    .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                    .put("guests", context.resources.getQuantityString(R.plurals.number_of_guests, params.adults + params.children.size(), params.adults + params.children.size()))
                    .format())

            hotelDownloadsObservable.onNext(hotelServices.regionSearch(params))
        })

        hotelDownloadResultsObservable.subscribe {
            hotelResultsObservable.onNext(it)
        }

        hotelResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }
    }
}

public class HotelResultsPricingStructureHeaderViewModel(private val resources: Resources) {
    // Inputs
    val loadingStartedObserver = PublishSubject.create<Unit>()
    val resultsDeliveredObserver = PublishSubject.create<HotelSearchResponse>()

    // Outputs
    val pricingStructureHeaderObservable = BehaviorSubject.create<String>()

    init {
        loadingStartedObserver.subscribe {
            pricingStructureHeaderObservable.onNext(resources.getString(R.string.progress_searching_hotels_hundreds))
        }

        resultsDeliveredObserver.subscribe { response ->
            val hotelResultsCount = response.hotelList?.size() ?: 0
            val header =
                    when (response.userPriceType) {
                        HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_total_price_for_stay_TEMPLATE, hotelResultsCount, hotelResultsCount)
                        else -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_prices_avg_per_night_TEMPLATE, hotelResultsCount, hotelResultsCount)
                    }

            pricingStructureHeaderObservable.onNext(header)
        }
    }
}
