package com.expedia.vm.hotel

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.pos.PointOfSale
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelResultsPricingStructureHeaderViewModel(private val resources: Resources) {
    // Inputs
    val loadingStartedObserver = PublishSubject.create<Unit>()
    val resultsDeliveredObserver = PublishSubject.create<HotelSearchResponse>()

    // Outputs
    val resultsDescriptionHeaderObservable = BehaviorSubject.create<String>()
    val loyaltyAvailableObservable = BehaviorSubject.create<Boolean>()
    val sortFaqLinkAvailableObservable = BehaviorSubject.create<Boolean>()

    init {
        loadingStartedObserver.subscribe {
            resultsDescriptionHeaderObservable.onNext(resources.getString(R.string.progress_searching_hotels_hundreds))
            loyaltyAvailableObservable.onNext(false)
            sortFaqLinkAvailableObservable.onNext(false)
        }

        resultsDeliveredObserver.subscribe { response ->
            val list = response.hotelList
            val priceType = response.userPriceType
            val doesSearchResultsHaveLoyaltyInformation = response.hasLoyaltyInformation
            val hotelResultsCount = list.size
            val header =
                    when (priceType) {
                        HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_total_price_for_stay_TEMPLATE, hotelResultsCount, hotelResultsCount)
                        HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_prices_avg_per_night_TEMPLATE, hotelResultsCount, hotelResultsCount)
                        else -> resources.getQuantityString(R.plurals.hotel_results_default_header_TEMPLATE, hotelResultsCount, hotelResultsCount)
                    }

            resultsDescriptionHeaderObservable.onNext(header)
            loyaltyAvailableObservable.onNext(doesSearchResultsHaveLoyaltyInformation)

            val faqUrl = PointOfSale.getPointOfSale().hotelsResultsSortFaqUrl
            sortFaqLinkAvailableObservable.onNext(faqUrl.isNotEmpty())
        }
    }
}
