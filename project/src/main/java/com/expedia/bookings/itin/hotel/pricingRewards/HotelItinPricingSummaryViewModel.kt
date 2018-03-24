package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.TotalPriceDetails
import io.reactivex.subjects.PublishSubject

class HotelItinPricingSummaryViewModel<out S>(val scope: S) where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {
    var observer: LiveDataObserver<ItinHotel>
    val lineItemViewModelSubject: PublishSubject<List<HotelItinPricingSummary>> = PublishSubject.create<List<HotelItinPricingSummary>>()

    init {
        observer = LiveDataObserver { hotel ->
            val rooms = hotel?.rooms ?: return@LiveDataObserver
            val items = rooms
                    .mapNotNull { it.totalPriceDetails }
                    .fold(emptyList<Pair<HotelItinLineItem, List<HotelItinLineItem>>>(), { acc, details ->
                        val mutable = acc.toMutableList()
                        val total = getTotalPriceLineItem(details)
                        val items = getLineItemViewModels(details)

                        if (total != null && items != null) {
                            mutable.add(Pair(total, items))
                        }

                        return@fold mutable.toList()
                    })
                    .map { pair: Pair<HotelItinLineItem, List<HotelItinLineItem>> -> HotelItinPricingSummary(pair.first, pair.second) }

            if (items.isNotEmpty()) {
                lineItemViewModelSubject.onNext(items)
            }
        }

        scope.itinHotelRepo.liveDataHotel.observe(scope.lifecycleOwner, observer)
    }

    private fun getTotalPriceLineItem(details: TotalPriceDetails): HotelItinLineItem? {
        val totalFormatted = details.totalFormatted ?: return null

        return HotelItinLineItem(scope.strings.fetch(R.string.itin_hotel_details_cost_summary_room_price_text), totalFormatted)
    }

    private fun getLineItemViewModels(details: TotalPriceDetails): List<HotelItinLineItem>? {
        val priceDetailsPerDay = details.priceDetailsPerDay ?: return null

        return priceDetailsPerDay
                .map { Pair(it.localizedDay?.localizedFullDate, it.amountFormatted) }
                .filter { it.first != null && it.second != null }
                .map { HotelItinLineItem(it.first!!, it.second!!) }
    }
}

data class HotelItinLineItem(val labelString: String, val priceString: String)
data class HotelItinPricingSummary(val totalLineItem: HotelItinLineItem, val perDayLineItems: List<HotelItinLineItem>)
