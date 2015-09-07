package com.expedia.vm

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class HotelResultsViewModel(private val context: Context, private val hotelServices: HotelServices) {

    private val hotelDownloadsObservable = PublishSubject.create<Observable<HotelSearchResponse>>()
    val hotelResultsObservable: Observable<HotelSearchResponse> = Observable.concat(hotelDownloadsObservable)
    val paramsSubject = PublishSubject.create<HotelSearchParams>()
    val titleSubject = PublishSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            titleSubject.onNext(params.suggestion.regionNames.shortName)

            subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                    .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                    .put("guests", params.children.size() + 1)
                    .format())

            hotelDownloadsObservable.onNext(hotelServices.regionSearch(params))
        })
    }
}

public class HotelResultsPricingStructureHeaderViewModel(private val resources: Resources, private val hotelResultsCount: Int, private val priceType: HotelRate.UserPriceType) {
    val pricingStructureHeaderObservable = BehaviorSubject.create(Phrase.from(resources,
            if (priceType == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES)
                R.string.hotel_results_pricing_header_total_price_for_stay_TEMPLATE
            else
                R.string.hotel_results_pricing_header_prices_avg_per_night_TEMPLATE)
            .put("count", hotelResultsCount).format().toString())
}
