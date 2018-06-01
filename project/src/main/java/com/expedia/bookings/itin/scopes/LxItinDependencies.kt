package com.expedia.bookings.itin.scopes

import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.common.ItinRedeemVoucherViewModel
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.lx.details.LxItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.tripstore.data.ItinLOB

interface ManageBookingWidgetViewModelSetter {
    fun setUpViewModel(vm: ItinManageBookingWidgetViewModel)
}

interface ToolBarViewModelSetter {
    fun setUpViewModel(vm: NewItinToolbarViewModel)
}

interface MapWidgetViewModelSetter<T :ItinLOB> {
    fun setUpViewModel(vm: ItinMapWidgetViewModel<T>)
}

interface HasManageBookingWidgetViewModelSetter {
    val manageBooking: ManageBookingWidgetViewModelSetter
}

interface HasToolbarViewModelSetter {
    val toolbar: ToolBarViewModelSetter
}

interface HasMapWidgetViewModelSetter<T :ItinLOB> {
    val map: MapWidgetViewModelSetter<T>
}

interface RedeemVoucherViewModelSetter {
    fun setUpViewModel(vm: ItinRedeemVoucherViewModel)
}

interface HasRedeemVoucherViewModelSetter {
    val redeemVoucher: RedeemVoucherViewModelSetter
}

interface HasItinImageViewModelSetter<T : ItinLOB>{
    val itinImage: ItinImageViewModelSetter<T>
}

interface ItinImageViewModelSetter<T : ItinLOB> {
    fun setupViewModel(vm: ItinImageViewModel<T>)
}

interface ItinTimingsViewModelSetter<T : ItinLOB> {
    fun setupViewModel(vm: ItinTimingsWidgetViewModel<T>)
}

interface HasItinTimingsViewModelSetter<T : ItinLOB> {
    val itinTimings: ItinTimingsViewModelSetter<T>
}
