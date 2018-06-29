package com.expedia.bookings.itin.scopes

import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.common.ItinRedeemVoucherViewModel
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.common.NewItinToolbarViewModel

interface ManageBookingWidgetViewModelSetter {
    fun setUpViewModel(vm: ItinManageBookingWidgetViewModel)
}

interface ToolBarViewModelSetter {
    fun setUpViewModel(vm: NewItinToolbarViewModel)
}

interface MapWidgetViewModelSetter {
    fun setUpViewModel(vm: ItinMapWidgetViewModel)
}

interface HasManageBookingWidgetViewModelSetter {
    val manageBooking: ManageBookingWidgetViewModelSetter
}

interface HasToolbarViewModelSetter {
    val toolbar: ToolBarViewModelSetter
}

interface HasMapWidgetViewModelSetter {
    val map: MapWidgetViewModelSetter
}

interface RedeemVoucherViewModelSetter {
    fun setUpViewModel(vm: ItinRedeemVoucherViewModel)
}

interface HasRedeemVoucherViewModelSetter {
    val redeemVoucher: RedeemVoucherViewModelSetter
}

interface HasItinImageViewModelSetter {
    val itinImage: ItinImageViewModelSetter
}

interface ItinImageViewModelSetter {
    fun setupViewModel(vm: ItinImageViewModel)
}

interface ItinTimingsViewModelSetter {
    fun setupViewModel(vm: ItinTimingsWidgetViewModel)
}

interface HasItinTimingsViewModelSetter {
    val itinTimings: ItinTimingsViewModelSetter
}
