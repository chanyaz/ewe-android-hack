package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.extensions.setMaterialFormsError
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.presenter.shared.StoredCouponListAdapter
import com.expedia.bookings.presenter.shared.StoredCouponWidget
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isShowSavedCoupons
import com.expedia.bookings.extensions.withLatestFrom

class MaterialFormsCouponWidget(context: Context, attrs: AttributeSet?) : AbstractCouponWidget(context, attrs) {

    val storedCouponWidget: StoredCouponWidget by bindView(R.id.stored_coupon_widget)
    val appliedCouponSubtitle: TextView by bindView(R.id.applied_coupon_subtitle_text)

    init {
        if (isShowSavedCoupons(context)) {
            expanded.setBackgroundColor(ContextCompat.getColor(context, R.color.material_checkout_background_color))
        }
    }

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
        val errorMessage = viewmodel.applyCouponViewModel.errorMessageObservable.value
        if (show) {
            errorMessage?.let { announceForAccessibility(errorMessage) }
        }
        couponCode.setMaterialFormsError(!show, errorMessage ?: "")
    }

    override fun setUpViewModelSubscriptions() {

        getStoredCouponListAdapter().applyStoredCouponObservable.subscribe { clickPosition ->
            viewmodel.storedCouponViewModel.applyStoredCouponObservable.onNext(getStoredCouponListAdapter().coupons[clickPosition].savedCoupon)
        }

        viewmodel.storedCouponViewModel.errorMessageObservable.withLatestFrom(viewmodel.storedCouponViewModel.storedCouponActionParam, { errorText, storedCouponActionParam ->
            errorText?.let { announceForAccessibility(errorText) }
            storedCouponWidget.viewModel.errorObservable.onNext(Pair(errorText, storedCouponActionParam.instanceId))
            enableCouponUi(true)
        }).subscribe()

        viewmodel.storedCouponViewModel.applyStoredCouponObservable.subscribe {
            enableCouponUi(false)
        }

        viewmodel.storedCouponWidgetVisibilityObservable.subscribeVisibility(storedCouponWidget)
        storedCouponWidget.viewModel.storedCouponsSubject
                .map { it.isNotEmpty() && isShowSavedCoupons(context) }
                .subscribe(viewmodel.storedCouponWidgetVisibilityObservable)

        viewmodel.storedCouponViewModel.applyStoredCouponObservable.withLatestFrom(paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse, { coupon, paymentSplitsAndTripResponse ->
            Pair(coupon, paymentSplitsAndTripResponse)
        }).subscribe {
            viewmodel.submitStoredCoupon(it.second.paymentSplits, it.second.tripResponse, userStateManager, it.first)
        }

        viewmodel.couponSubtitleObservable.subscribeText(appliedCouponSubtitle)
    }

    override fun getViewToInflate(): Int {
        return R.layout.material_forms_coupon_widget
    }

    override fun injectViewInHotelComponent() {
        com.expedia.bookings.utils.Ui.getApplication(context).hotelComponent().inject(this)
    }

    override fun getMenuButtonTitle(): String? {
        return resources.getString(R.string.coupon_apply_button)
    }

    override fun setExpanded(expand: Boolean, animate: Boolean) {
        super.setExpanded(expand, animate)
        viewmodel.onCouponWidgetExpandSubject.onNext(expand && isShowSavedCoupons(context))
    }

    override fun showHotelCheckoutView(couponInstanceId: String?): Boolean {
        val couponAppliedFromStoredCoupon = getStoredCouponListAdapter().coupons.find { it.savedCoupon.instanceId == couponInstanceId } != null
        if (isExpanded) {
            return !couponAppliedFromStoredCoupon
        } else {
            return true
        }
    }

    override fun addProgressView() {
        textViewLayout.addView(progress)
    }

    private fun getStoredCouponListAdapter(): StoredCouponListAdapter {
        return storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter
    }

    override fun enableCouponUi(enable: Boolean) {
        super.enableCouponUi(enable)
        storedCouponWidget.viewModel.enableStoredCouponsSubject.onNext(enable)
    }
}
