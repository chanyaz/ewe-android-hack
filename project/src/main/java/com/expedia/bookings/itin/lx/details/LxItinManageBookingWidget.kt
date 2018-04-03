package com.expedia.bookings.itin.lx.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardView
import com.expedia.bookings.itin.scopes.LxItinManageBookingWidgetScope
import com.expedia.bookings.itin.scopes.ManageBookingWidgetViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class LxItinManageBookingWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr), ManageBookingWidgetViewModelSetter {

    override fun setUpViewModel(vm: LxItinManageBookingWidgetViewModel<LxItinManageBookingWidgetScope>) {
        viewModel = vm
    }

    val moreHelp: ItinBookingInfoCardView by bindView(R.id.itin_lx_booking_info_more_help)
    val priceSummary: ItinBookingInfoCardView by bindView(R.id.itin_lx_booking_info_price_summary)
    val additionalInfo: ItinBookingInfoCardView by bindView(R.id.itin_lx_booking_info_additional_info)

    init {
        View.inflate(context, R.layout.widget_lx_itin_booking, this)
    }

    var viewModel: LxItinManageBookingWidgetViewModel<LxItinManageBookingWidgetScope> by notNullAndObservable {
        moreHelp.viewModel = viewModel.moreHelpViewModel
        priceSummary.viewModel = viewModel.priceSummaryViewModel
        additionalInfo.viewModel = viewModel.additionalInfoViewModel
    }
}
