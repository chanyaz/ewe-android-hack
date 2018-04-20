package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.TotalPriceDetails
import io.reactivex.subjects.PublishSubject

class HotelItinPricingSummaryViewModel<out S>(val scope: S) : IHotelItinPricingSummaryViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {
    var observer: LiveDataObserver<ItinHotel>
    override val clearPriceSummaryContainerSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    override val roomPriceBreakdownSubject: PublishSubject<List<HotelItinRoomPrices>> = PublishSubject.create<List<HotelItinRoomPrices>>()
    override val priceLineItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create<HotelItinPriceLineItem>()

    init {
        observer = LiveDataObserver { hotel ->
            //remove existing views
            clearPriceSummaryContainerSubject.onNext(Unit)

            //rooms price breakdown
            val rooms = hotel?.rooms ?: return@LiveDataObserver
            val roomPrices = rooms
                    .mapNotNull { it.totalPriceDetails }
                    .fold(emptyList<Pair<HotelItinPriceLineItem, List<HotelItinPriceLineItem>>>(), { accumulator, priceDetails ->
                        val mutableAccumulator = accumulator.toMutableList()
                        val roomPrice = getRoomTotalPriceItem(priceDetails)
                        val roomPricesPerDay = getRoomPricePerDayItems(priceDetails)

                        if (roomPrice != null && roomPricesPerDay != null) {
                            mutableAccumulator.add(Pair(roomPrice, roomPricesPerDay))
                        }

                        return@fold mutableAccumulator.toList()
                    })
                    .map { pair: Pair<HotelItinPriceLineItem, List<HotelItinPriceLineItem>> -> HotelItinRoomPrices(pair.first, pair.second) }

            if (roomPrices.isNotEmpty()) {
                roomPriceBreakdownSubject.onNext(roomPrices)
            }

            //extra guest charges
            val extraGuestCharges = hotel.totalPriceDetails?.extraGuestChargesFormatted
            if (extraGuestCharges != null && !extraGuestCharges.isBlank()) {
                val extraGuestChargesItem = HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_multiple_guest_fees_label), extraGuestCharges, R.color.itin_price_summary_label_gray_light)
                priceLineItemSubject.onNext(extraGuestChargesItem)
            }

            //property fee

            //taxes and fees
            val taxesAndFees = hotel.totalPriceDetails?.taxesAndFeesFormatted
            if (taxesAndFees != null && !taxesAndFees.isBlank()) {
                val taxesAndFeesItem = HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_taxes_and_fees_label), taxesAndFees, R.color.itin_price_summary_label_gray_dark)
                priceLineItemSubject.onNext(taxesAndFeesItem)
            }

            //coupons
            val adjustmentsForCoupons = hotel.totalPriceDetails?.adjustmentForCouponFormatted
            if (adjustmentsForCoupons != null && !adjustmentsForCoupons.isBlank()) {
                val couponItem = HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_coupons_label), adjustmentsForCoupons, R.color.itin_price_summary_label_green)
                priceLineItemSubject.onNext(couponItem)
            }

            //points
        }

        scope.itinHotelRepo.liveDataHotel.observe(scope.lifecycleOwner, observer)
    }

    private fun getRoomTotalPriceItem(details: TotalPriceDetails): HotelItinPriceLineItem? {
        val totalFormatted = details.totalFormatted ?: return null
        return HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_details_cost_summary_room_price_text), totalFormatted, R.color.itin_price_summary_label_gray_dark)
    }

    private fun getRoomPricePerDayItems(details: TotalPriceDetails): List<HotelItinPriceLineItem>? {
        val priceDetailsPerDay = details.priceDetailsPerDay ?: return null
        return priceDetailsPerDay
                .map { Pair(it.localizedDay?.localizedFullDate, it.amountFormatted) }
                .filter { it.first != null && it.second != null }
                .map { HotelItinPriceLineItem(it.first!!, it.second!!, R.color.itin_price_summary_label_gray_light) }
    }
}

data class HotelItinPriceLineItem(val labelString: String, val priceString: String, val colorRes: Int)
data class HotelItinRoomPrices(val totalRoomPriceItem: HotelItinPriceLineItem, val perDayRoomPriceItems: List<HotelItinPriceLineItem>)
