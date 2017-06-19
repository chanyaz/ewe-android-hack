package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.HotelCouponViewModel
import com.mobiata.android.util.Ui
import rx.subjects.BehaviorSubject
import javax.inject.Inject
import com.mobiata.android.Log
import kotlin.properties.Delegates

class CouponWidget(context: Context, attrs: AttributeSet?) : ExpandableCardView(context, attrs) {
    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    lateinit var userStateManager: UserStateManager
        @Inject set

    val unexpanded: TextView by bindView(R.id.unexpanded)
    val expanded: LinearLayout by bindView(R.id.expanded)
    val applied: LinearLayout by bindView(R.id.applied)
    val couponCode: AccessibleEditText by bindView(R.id.edit_coupon_code)
    val error: TextView by bindView(R.id.error_message)
    val appliedCouponMessage: TextView by bindView(R.id.applied_coupon_text)
    val removeCoupon: ImageButton by bindView(R.id.remove_coupon_button)
    var progress: View by Delegates.notNull()

    val onCouponSubmitClicked = BehaviorSubject.create<Unit>()

    var viewmodel: HotelCouponViewModel by notNullAndObservable {
        viewmodel.errorMessageObservable.subscribeText(error)
        viewmodel.applyObservable.subscribe {
            showProgress(true)
            showError(false)
        }
        viewmodel.errorObservable.subscribe {
            showProgress(false)
            if (viewmodel.hasDiscountObservable.value != null && viewmodel.hasDiscountObservable.value) {
                isExpanded = false
            } else {
                showError(true)
            }
        }
        viewmodel.couponObservable.subscribe {
            showProgress(false)
            showError(false)
            isExpanded = false
        }
        viewmodel.discountObservable.subscribe {
            appliedCouponMessage.text = context.getString(R.string.applied_coupon_message, it)
        }
        viewmodel.enableSubmitButtonObservable.subscribe { showButton ->
            mToolbarListener.enableRightActionButton(showButton)
        }
    }

    val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            mToolbarListener.enableRightActionButton(s.length > 3)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

    }

    init {
        com.expedia.bookings.utils.Ui.getApplication(getContext()).hotelComponent().inject(this)
        onCouponSubmitClicked
                .withLatestFrom(paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse, { unit, paymentSplitsAndTripResponse -> paymentSplitsAndTripResponse})
                .subscribe { submitCoupon(it.paymentSplits, it.tripResponse) }

        View.inflate(getContext(), R.layout.coupon_widget, this)
        //Tests hates progress bars
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.CENTER_VERTICAL
        if (ExpediaBookingApp.isAutomation()) {
            //Espresso hates progress bars
            progress = View(getContext(), null)
            lp.width = 0
        } else {
            progress = ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall)
            (progress as ProgressBar).isIndeterminate = true
        }
        progress.layoutParams = lp
        couponCode.setOnEditorActionListener({ textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && textView.text.length > 3) {
                onMenuButtonPressed()
            }
            false
        })
        expanded.addView(progress)
        showProgress(false)

        removeCoupon.setOnClickListener {
            resetFields()
            viewmodel.removeObservable.onNext(true)
            HotelTracking.trackHotelCouponRemove(couponCode.text.toString())
        }
    }

    override fun getMenuDoneButtonFocus(): Boolean {
        if (couponCode.text.isNotEmpty()) {
            return true
        }
        return false
    }

    override fun setExpanded(expand: Boolean, animate: Boolean) {
        super.setExpanded(expand, animate)
        if (expand) {
            couponCode.addTextChangedListener(textWatcher)
            background = null
            expanded.visibility = View.VISIBLE
            unexpanded.visibility = View.GONE
            applied.visibility = View.GONE
            if (mToolbarListener != null) {
                mToolbarListener.onEditingComplete()
                mToolbarListener.showRightActionButton(false)
            }
            HotelTracking.trackHotelExpandCoupon()
            couponCode.requestFocus()
            Ui.showKeyboard(couponCode, null)
        } else {
            couponCode.removeTextChangedListener(textWatcher)
            resetFields()
            setBackgroundResource(R.drawable.card_background)
            expanded.visibility = View.GONE
            if (viewmodel.hasDiscountObservable.value != null && viewmodel.hasDiscountObservable.value) {
                applied.visibility = View.VISIBLE
                unexpanded.visibility = View.GONE
            }
            else {
                applied.visibility = View.GONE
                unexpanded.visibility = View.VISIBLE
            }
        }
    }

    override fun getActionBarTitle(): String? {
        return context.getString(R.string.coupon_promo_title)
    }

    override fun onMenuButtonPressed() {
        viewmodel.enableSubmitButtonObservable.onNext(false)
        onCouponSubmitClicked.onNext(Unit)
    }

    private fun submitCoupon(paymentSplits: PaymentSplits, tripResponse: TripResponse) {
        Log.e("**************HAHAHAHAHAHAHAHA")
        var userPointsPreference: List<UserPreferencePointsDetails> = emptyList()
        //Send 'User Preference Points' only in case Trip is Redeemable
        if (userStateManager.isUserAuthenticated() && tripResponse.isRewardsRedeemable()) {
            val payingWithPointsSplit = paymentSplits.payingWithPoints
            userPointsPreference = listOf(UserPreferencePointsDetails(tripResponse.getProgramName()!!, payingWithPointsSplit))
        }

        val couponParams = HotelApplyCouponParameters.Builder()
                .tripId(Db.getTripBucket().hotelV2.mHotelTripResponse.tripId)
                .couponCode(couponCode.text.toString())
                .isFromNotSignedInToSignedIn(false)
                .userPreferencePointsDetails(userPointsPreference)
                .build()
        viewmodel.couponParamsObservable.onNext(couponParams)
    }

    override fun onLogin() {

    }

    override fun onLogout() {

    }

    override fun isComplete(): Boolean {
        return true
    }

    override fun onClick(v: View) {
        if (applied.visibility == View.VISIBLE) {
            return
        }
        else if (!isExpanded) {
            isExpanded = true
        }
    }

    private fun showProgress(show: Boolean) {
        progress.visibility = if (show) { View.VISIBLE } else { View.INVISIBLE }
    }

    private fun showError(show: Boolean) {
        error.visibility = if (show) { View.VISIBLE } else { View.INVISIBLE }
    }

    override fun getMenuButtonTitle(): String? {
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            return resources.getString(R.string.coupon_submit_button_ally)
        } else {
            return resources.getString(R.string.coupon_submit_button)
        }
    }

    private fun resetFields() {
        couponCode.text = null
        error.text = null
    }
}
