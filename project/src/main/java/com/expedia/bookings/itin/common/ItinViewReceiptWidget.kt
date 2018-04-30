package com.expedia.bookings.itin.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.features.Features
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinViewReceiptWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val viewReceiptButton by bindView<TextView>(R.id.view_receipt_button)

    init {
        View.inflate(context, R.layout.widget_itin_view_receipt, this)
    }

    var viewModel: ItinViewReceiptViewModel by notNullAndObservable { vm ->
        vm.showReceipt.subscribe {
            setUpViews()
        }
    }

    private fun setUpViews() {
        val isViewReceiptFeatureEnabled = Features.all.viewReceipt.enabled()
        if (isViewReceiptFeatureEnabled) {
            AccessibilityUtil.appendRoleContDesc(viewReceiptButton, R.string.accessibility_cont_desc_role_button)
            viewReceiptButton.visibility = View.VISIBLE
            viewReceiptButton.alpha = 1f
            viewReceiptButton.subscribeOnClick(viewModel.viewReceiptClickSubject)
        }
    }
}
