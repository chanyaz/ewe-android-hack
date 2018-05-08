package com.expedia.bookings.itin.scopes

import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.lx.details.LxItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.lx.details.LxItinMapWidgetViewModel

interface ManageBookingWidgetViewModelSetter {
    fun setUpViewModel(vm: LxItinManageBookingWidgetViewModel<LxItinManageBookingWidgetScope>)
}

interface ToolBarViewModelSetter {
    fun setUpViewModel(vm: NewItinToolbarViewModel)
}

interface MapWidgetViewModelSetter {
    fun setUpViewModel(vm: LxItinMapWidgetViewModel<LxItinMapWidgetViewModelScope>)
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
