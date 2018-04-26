package com.expedia.bookings.itin.hotel.manageBooking

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinModifyReservationWidget
import com.expedia.bookings.itin.hotel.details.HotelItinRoomDetails
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class HotelItinManageRoomWidget(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {
    val roomDetailsView by bindView<HotelItinRoomDetails>(R.id.widget_hotel_itin_room_details)
    val hotelManageBookingHelpView by bindView<HotelItinManageBookingHelp>(R.id.widget_hotel_itin_manage_booking_help)
    val hotelCustomerSupportDetailsView by bindView<HotelItinCustomerSupportDetails>(R.id.widget_hotel_itin_customer_support)
    val modifyReservationWidget by bindView<ItinModifyReservationWidget>(R.id.hotel_itin_modify_reservation_widget)

    var viewModel: HotelItinManageRoomViewModel by notNullAndObservable { vm ->
        vm.roomDetailsSubject.subscribe { room ->
            roomDetailsView.setUpRoomAndOccupantInfo(room)
            roomDetailsView.expandRoomDetailsView()
            hotelManageBookingHelpView.showConfirmationNumberIfAvailable(room.hotelConfirmationNumber)
            modifyReservationWidget.viewModel.roomChangeSubject.onNext(room)
        }

        vm.roomChangeAndCancelRulesSubject.subscribe {
            roomDetailsView.setupAndShowChangeAndCancelRules(it)
        }

        vm.itinCardDataHotelSubject.subscribe { itinCardDataHotel ->
            hotelManageBookingHelpView.setUpWidget(itinCardDataHotel)
            hotelCustomerSupportDetailsView.setUpWidget(itinCardDataHotel.tripNumber)
            modifyReservationWidget.viewModel.itinCardSubject.onNext(itinCardDataHotel)
        }
    }

    init {
        View.inflate(context, R.layout.hotel_itin_manage_room_widget, this)
        modifyReservationWidget.viewModel = HotelItinModifyReservationViewModel(context)
    }
}
