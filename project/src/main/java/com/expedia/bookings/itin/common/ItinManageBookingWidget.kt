package com.expedia.bookings.itin.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.ManageBookingWidgetViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class ItinManageBookingWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr), ManageBookingWidgetViewModelSetter {
    override fun setUpViewModel(vm: ItinManageBookingWidgetViewModel) {
        viewModel = vm
    }

    val moreHelp: ItinBookingInfoCardView by bindView(R.id.itin_lx_booking_info_more_help)
    val priceSummary: ItinBookingInfoCardView by bindView(R.id.itin_lx_booking_info_price_summary)
    val additionalInfo: ItinBookingInfoCardView by bindView(R.id.itin_lx_booking_info_additional_info)

    init {
        View.inflate(context, R.layout.widget_lx_itin_booking, this)
    }

    var viewModel: ItinManageBookingWidgetViewModel by notNullAndObservable { vm ->
        moreHelp.viewModel = vm.moreHelpViewModel
        priceSummary.viewModel = vm.priceSummaryViewModel
        additionalInfo.viewModel = vm.additionalInfoViewModel
    }
}
