package com.expedia.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import java.math.BigDecimal

public data class HotelRoomRate(val hotelRoom: HotelOffersResponse.HotelRoomResponse)

var lastExpanded: Int = 0

public class HotelRoomRateViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val index: Int) {

    var roomRateInfoVisible: Int = View.GONE

    //Output
    val rateObservable = BehaviorSubject.create(HotelRoomRate(hotelRoomResponse))
    val roomBackgroundViewObservable = BehaviorSubject.create<Drawable>()
    val roomSelectedObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val roomTypeObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomTypeDescription)
    val collapsedBedTypeObservable = BehaviorSubject.create<String>()
    val expandedBedTypeObservable = BehaviorSubject.create<String>()
    val currencyCode = hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode

    var dailyPricePerNightObservable = BehaviorSubject.create<String>()

    val totalPricePerNightObservable = BehaviorSubject.create<String>(context.getResources().getString(R.string.cars_total_template, currencyCode + hotelRoomResponse.rateInfo.chargeableRateInfo.total))
    val roomHeaderImageObservable = BehaviorSubject.create<String>(Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl)
    val expandRoomObservable = BehaviorSubject.create<Boolean>()
    val collapseRoomObservable = BehaviorSubject.create<Int>()
    val roomRateInfoTextObservable = BehaviorSubject.create<String>(hotelRoomResponse.roomLongDescription)
    val roomInfoObservable = BehaviorSubject.create<Int>()

    val expandCollapseRoomRate: Observer<Boolean> = endlessObserver {
        isChecked ->
        if (!isChecked) {
            roomSelectedObservable.onNext(hotelRoomResponse)
        } else {
            // expand row if it's not expanded
            if (lastExpanded != index) {
                collapseRoomObservable.onNext(lastExpanded)

                if (isChecked) {
                    expandRoomObservable.onNext(true)
                    roomBackgroundViewObservable.onNext(context.getResources().getDrawable(R.drawable.card_background))
                    lastExpanded = index
                }
            }
        }
    }

    val payLaterObserver: Observer<Unit> = endlessObserver {
        val depositAmount = hotelRoomResponse.payLaterOffer?.rateInfo?.chargeableRateInfo?.depositAmountToShowUsers?.toDouble() ?: 0.0
        val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
        val payLaterText = depositAmountMoney.getFormattedMoney()+" " + context.getResources().getString(R.string.room_rate_pay_later_due_now)
        dailyPricePerNightObservable.onNext(payLaterText)
    }

    val expandCollapseRoomRateInfo: Observer<Unit> = endlessObserver {
        if (roomRateInfoVisible == View.VISIBLE) roomRateInfoVisible = View.GONE else roomRateInfoVisible = View.VISIBLE
        roomInfoObservable.onNext(roomRateInfoVisible)
    }

    init {
        val dailyPrice = Money(BigDecimal(hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers.toDouble()), currencyCode)
        dailyPricePerNightObservable.onNext(dailyPrice.getFormattedMoney() + context.getResources().getString(R.string.per_night))
        rateObservable.subscribe { update ->
            val bedTypes = update.hotelRoom.bedTypes.map { it.description }.join("")
            collapsedBedTypeObservable.onNext(bedTypes)
            expandedBedTypeObservable.onNext(bedTypes)
        }

        if (index == 0) {
            expandRoomObservable.onNext(true)
            roomBackgroundViewObservable.onNext(context.getResources().getDrawable(R.drawable.card_background))
        } else {
            expandRoomObservable.onNext(false)
        }
    }
}

