package com.expedia.util

import android.content.Context
import android.view.View
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.widget.HotelRoomRateView
import com.expedia.bookings.widget.ScrollView
import com.expedia.vm.HotelRoomRateViewModel
import rx.Observable
import rx.Observer
import rx.subjects.PublishSubject
import java.util.ArrayList

public class HotelRoomRateViewFactory() {

    companion object {
        val viewsPool: ArrayList<HotelRoomRateView> = ArrayList()

        fun makeHotelRoomRateView(context: Context, scrollAncestor: ScrollView, rowTopConstraintViewObservable: Observable<View>, selectedRoomObserver: Observer<HotelOffersResponse.HotelRoomResponse>, rowIndex: Int,
                    hotelId: String, hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, amenity: String, rowExpanding: PublishSubject<Int>, hasETP: Boolean): HotelRoomRateView {

            val poolOfViews = viewsPool

            if (rowIndex < poolOfViews.size) {
                val view = poolOfViews.get(rowIndex)
                view.viewRoom.isChecked = false
                view.viewSetup(scrollAncestor, rowTopConstraintViewObservable, rowIndex)
                view.viewmodel.setupModel(hotelRoomResponse, hotelId, amenity, rowIndex, rowExpanding, selectedRoomObserver)
                return view
            }
            else {
                val view = HotelRoomRateView(context, scrollAncestor, rowTopConstraintViewObservable, rowIndex)
                view.viewmodel = HotelRoomRateViewModel(context, hotelId, hotelRoomResponse, amenity, rowIndex, rowExpanding, selectedRoomObserver, hasETP)

                poolOfViews.add(view)
                return view
            }
        }
    }
}
