package com.expedia.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

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
    val currencyCode = hotelRoomResponse.rateInfo.chargeableRateInfo.currencySymbol
    val dailyPricePerNightObservable = BehaviorSubject.create<String>(currencyCode + hotelRoomResponse.rateInfo.chargeableRateInfo.averageRate.toInt() + context.getResources().getString(R.string.per_night))
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

    val expandCollapseRoomRateInfo: Observer<Unit> = endlessObserver {
        if (roomRateInfoVisible == View.VISIBLE) roomRateInfoVisible = View.GONE else roomRateInfoVisible = View.VISIBLE
        roomInfoObservable.onNext(roomRateInfoVisible)
    }

    init {
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

