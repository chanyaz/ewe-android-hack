package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText

class CouponWidget(context: Context, attrs: AttributeSet?) : AbstractCouponWidget(context, attrs) {

    override val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            mToolbarListener.enableRightActionButton(s.length > 3)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }
    }

    override fun onExpand() {
        if (mToolbarListener != null) {
            mToolbarListener.onEditingComplete()
            mToolbarListener.showRightActionButton(false)
        }
    }

    val error: TextView by bindView(R.id.error_message)

    override fun injectViewInHotelComponent() {
        com.expedia.bookings.utils.Ui.getApplication(getContext()).hotelComponent().inject(this)
    }

    override fun getViewToInflate(): Int {
        return R.layout.coupon_widget
    }

    override fun setUpViewModelSubscriptions() {
        viewmodel.errorMessageObservable.subscribeText(error)
    }

    override fun showError(show: Boolean) {
        error.visibility = if (show) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    override fun resetFields() {
        super.resetFields()
        error.text = null
    }

    override fun getMenuButtonTitle(): String? {
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            return resources.getString(R.string.coupon_submit_button_ally)
        } else {
            return resources.getString(R.string.coupon_submit_button)
        }
    }

    override fun showHotelCheckoutView(couponInstanceId: String?): Boolean {
        return true
    }

    override fun addProgressView() {
        expanded.addView(progress)
    }
}
