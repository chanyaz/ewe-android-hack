package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.BookingStatus
import com.expedia.bookings.itin.tripstore.data.HotelRoom
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.PaymentModel
import com.expedia.bookings.itin.tripstore.data.TotalPriceDetails
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.isPointOfSaleDifferentFromPointOfSupply
import com.expedia.bookings.itin.utils.AnimationDirection
import com.expedia.bookings.utils.FontCache.Font
import io.reactivex.subjects.PublishSubject

class HotelItinPricingSummaryViewModel<out S>(val scope: S) : IHotelItinPricingSummaryViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo, S : HasActivityLauncher {
    var itinObserver: LiveDataObserver<Itin>
    var hotelObserver: LiveDataObserver<ItinHotel>

    override val roomContainerClearSubject: PublishSubject<Unit> = PublishSubject.create()
    override val roomContainerItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val multipleGuestItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val taxesAndFeesItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val couponsItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val pointsItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val totalPriceItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val totalPriceInPosCurrencyItemSubject: PublishSubject<HotelItinPriceLineItem> = PublishSubject.create()
    override val currencyDisclaimerSubject: PublishSubject<String> = PublishSubject.create()
    override val additionalPricingInfoSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        hotelObserver = LiveDataObserver {
            val hotel = it ?: return@LiveDataObserver

            //room price details
            val rooms = hotel.rooms
            roomContainerClearSubject.onNext(Unit)
            rooms?.filter { it.bookingStatus == BookingStatus.BOOKED }?.forEach { room ->
                val priceDetails = room.totalPriceDetails
                priceDetails?.let {
                    val roomPrice = getRoomTotalPriceItem(priceDetails)
                    if (roomPrice != null) {
                        roomContainerItemSubject.onNext(roomPrice)
                    }

                    val roomPricesPerDay = getRoomPricePerDayItems(priceDetails)
                    roomPricesPerDay?.forEach {
                        roomContainerItemSubject.onNext(it)
                    }

                    val propertyFee = getRoomPropertyFeeItem(room)
                    if (propertyFee != null) {
                        roomContainerItemSubject.onNext(propertyFee)
                    }
                }
            }

            //extra guest charges
            val extraGuestCharges = hotel.totalPriceDetails?.extraGuestChargesFormatted
            if (extraGuestCharges != null && !extraGuestCharges.isBlank()) {
                val extraGuestChargesItem = HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_multiple_guest_fees_label), extraGuestCharges, R.color.itin_price_summary_label_gray_light)
                multipleGuestItemSubject.onNext(extraGuestChargesItem)
            }

            //taxes and fees
            val taxesAndFees = hotel.totalPriceDetails?.taxesAndFeesFormatted
            if (taxesAndFees != null && !taxesAndFees.isBlank()) {
                val taxesAndFeesItem = HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_taxes_and_fees_label), taxesAndFees, R.color.itin_price_summary_label_gray_dark)
                taxesAndFeesItemSubject.onNext(taxesAndFeesItem)
            }

            //coupons
            val adjustmentsForCoupons = hotel.totalPriceDetails?.adjustmentForCouponFormatted
            if (adjustmentsForCoupons != null && !adjustmentsForCoupons.isBlank()) {
                val couponItem = HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_coupons_label), adjustmentsForCoupons, R.color.itin_price_summary_label_green)
                couponsItemSubject.onNext(couponItem)
            }

            //currency disclaimer
            if (hotel.isPointOfSaleDifferentFromPointOfSupply()) {
                val dualCurrencyText = hotel.rules?.dualCurrencyText
                if (dualCurrencyText != null && !dualCurrencyText.isBlank()) {
                    currencyDisclaimerSubject.onNext(dualCurrencyText)
                }
                val totalInPos = hotel.totalPriceDetails?.totalPOSFormatted
                val posCurrencyCode = hotel.totalPriceDetails?.totalPOSCurrencyCode
                if (posCurrencyCode != null && !posCurrencyCode.isBlank() && totalInPos != null && !totalInPos.isBlank()) {
                    val totalPriceInPos = HotelItinPriceLineItem(
                            scope.strings.fetchWithPhrase(R.string.itin_hotel_price_summary_total_in_pos_label_TEMPLATE, mapOf("currencycode" to posCurrencyCode)),
                            totalInPos,
                            R.color.itin_price_summary_label_gray_light)
                    totalPriceInPosCurrencyItemSubject.onNext(totalPriceInPos)
                }
            } else {
                val currencyDisclaimer = hotel.rules?.currencyDisclaimer
                if (currencyDisclaimer != null && !currencyDisclaimer.isBlank()) {
                    currencyDisclaimerSubject.onNext(currencyDisclaimer)
                }
            }
        }

        itinObserver = LiveDataObserver { itin ->
            //points
            val points = itin?.paymentDetails?.priceByFormOfPayment?.points?.localizedPaidPrice
            if (points != null && !points.isBlank()) {
                val pointsItem = HotelItinPriceLineItem(
                        scope.strings.fetch(R.string.itin_hotel_price_summary_points_label),
                        scope.strings.fetchWithPhrase(R.string.itin_hotel_price_summary_points_value_TEMPLATE, mapOf("points" to points)),
                        R.color.itin_price_summary_label_green)
                pointsItemSubject.onNext(pointsItem)
            }

            //total price
            val paymentModel = itin?.firstHotel()?.paymentModel
            paymentModel?.let {
                when (it) {
                    PaymentModel.EXPEDIA_COLLECT -> {
                        val totalPrice = itin.paymentDetails?.localizedNetPricePaidForThisBooking
                                ?: itin.firstHotel()?.totalPriceDetails?.totalFormatted
                        totalPrice?.let { price ->
                            val totalPriceItem = HotelItinPriceLineItem(
                                    scope.strings.fetch(R.string.itin_hotel_price_summary_total_amount_paid_label),
                                    price,
                                    R.color.itin_price_summary_label_gray_dark,
                                    16.0f,
                                    Font.ROBOTO_MEDIUM
                            )
                            totalPriceItemSubject.onNext(totalPriceItem)
                        }
                    }
                    PaymentModel.HOTEL_COLLECT -> {
                        val totalPrice = itin.firstHotel()?.totalPriceDetails?.totalFormatted
                        totalPrice?.let { price ->
                            val totalPriceItem = HotelItinPriceLineItem(
                                    scope.strings.fetch(R.string.itin_hotel_price_summary_total_amount_due_label),
                                    price,
                                    R.color.itin_price_summary_label_gray_dark,
                                    16.0f,
                                    Font.ROBOTO_MEDIUM
                            )
                            totalPriceItemSubject.onNext(totalPriceItem)
                        }
                    }
                }
            }

            //additional pricing info
            additionalPricingInfoSubject.subscribe {
                val tripId = itin?.tripId
                tripId?.let {
                    scope.activityLauncher.launchActivity(HotelItinPricingAdditionalInfoActivity, it, AnimationDirection.SLIDE_UP)
                }
            }
        }

        scope.itinHotelRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
        scope.itinHotelRepo.liveDataHotel.observe(scope.lifecycleOwner, hotelObserver)
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

    private fun getRoomPropertyFeeItem(details: HotelRoom): HotelItinPriceLineItem? {
        val propertyFee = details.roomPropertyFeeFormatted ?: return null
        return HotelItinPriceLineItem(scope.strings.fetch(R.string.itin_hotel_price_summary_property_fee_label), propertyFee, R.color.itin_price_summary_label_gray_light)
    }
}

data class HotelItinPriceLineItem(val labelString: String, val priceString: String, val colorRes: Int, val textSize: Float = 14.0f, val font: Font = Font.ROBOTO_REGULAR)
