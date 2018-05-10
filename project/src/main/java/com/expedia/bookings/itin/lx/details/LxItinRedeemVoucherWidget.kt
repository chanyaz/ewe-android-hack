package com.expedia.bookings.itin.lx.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.itin.common.ItinRedeemVoucherViewModel
import com.expedia.bookings.itin.scopes.RedeemVoucherViewModelSetter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class LxItinRedeemVoucherWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr), RedeemVoucherViewModelSetter {

    val redeemVoucherButton: TextView by bindView(R.id.itin_lx_redeem_voucher_button)

    init {
        View.inflate(context, R.layout.widget_lx_itin_redeem_button, this)
    }

    override fun setUpViewModel(vm: ItinRedeemVoucherViewModel) {
        viewModel = vm
    }

    var viewModel: ItinRedeemVoucherViewModel by notNullAndObservable { vm ->
        vm.showRedeemVoucher.subscribe {
            AccessibilityUtil.appendRoleContDesc(redeemVoucherButton, R.string.accessibility_cont_desc_role_button)
            with(redeemVoucherButton) {
                visibility = View.VISIBLE
                subscribeOnClick(viewModel.redeemVoucherClickSubject)
            }
        }
    }
}
