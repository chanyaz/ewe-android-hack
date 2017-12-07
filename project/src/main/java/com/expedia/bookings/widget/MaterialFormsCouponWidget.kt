package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.util.subscribeMaterialFormsError

class MaterialFormsCouponWidget(context: Context, attrs: AttributeSet?) : AbstractCouponWidget(context, attrs) {

    override val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            clearError()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }
    }

    private fun clearError() {
        couponCode.setMaterialFormsError(true, "")
    }


    override fun onExpand() {
        clearError()
        mToolbarListener.showRightActionButton(true)
    }

    override fun showError(show: Boolean) {
        couponCode.setMaterialFormsError(!show, viewmodel.errorMessageObservable.value ?: "")
    }

    override fun setUpViewModelSubscriptions() {
    }

    override fun getViewToInflate(): Int {
        return R.layout.material_forms_coupon_widget
    }

    override fun injectViewInHotelComponent() {
        com.expedia.bookings.utils.Ui.getApplication(getContext()).hotelComponent().inject(this)
    }

    override fun getMenuButtonTitle(): String? {
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            return resources.getString(R.string.coupon_apply_button_ally)
        } else {
            return resources.getString(R.string.coupon_apply_button)
        }
    }

}