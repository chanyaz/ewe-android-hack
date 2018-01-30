package com.expedia.bookings.widget

import android.content.Context
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
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.withLatestFrom
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelCouponViewModel
import com.mobiata.android.util.Ui
import javax.inject.Inject
import kotlin.properties.Delegates

abstract class AbstractCouponWidget(context: Context, attrs: AttributeSet?) : ExpandableCardView(context, attrs) {
    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    lateinit var userStateManager: UserStateManager
        @Inject set

    val unexpanded: TextView by bindView(R.id.unexpanded)
    val expanded: LinearLayout by bindView(R.id.expanded)
    val applied: LinearLayout by bindView(R.id.applied)
    val textViewLayout: LinearLayout by bindView(R.id.textview_layout)
    val couponCode: AccessibleEditText by bindView(R.id.edit_coupon_code)
    val appliedCouponMessage: TextView by bindView(R.id.applied_coupon_text)
    val removeCoupon: ImageButton by bindView(R.id.remove_coupon_button)
    var progress: View by Delegates.notNull()

    var viewmodel: HotelCouponViewModel by notNullAndObservable {

        viewmodel.applyCouponViewModel.applyCouponProgressObservable.subscribe {
            showProgress(true)
            showError(false)
            enableCouponUi(false)
        }

        viewmodel.applyCouponViewModel.errorMessageObservable.subscribe {
            showProgress(false)
            enableCouponUi(true)
            if (viewmodel.hasDiscountObservable.value != null && viewmodel.hasDiscountObservable.value) {
                isExpanded = false
            } else {
                showError(true)
            }
        }

        viewmodel.applyCouponViewModel.applyCouponSuccessObservable.subscribe {
            showProgress(false)
            showError(false)
            enableCouponUi(true)
            isExpanded = false
        }

        viewmodel.discountObservable.subscribe {
            appliedCouponMessage.text = context.getString(R.string.applied_coupon_message, it)
        }

        viewmodel.networkErrorAlertDialogObservable.subscribe {
            val retryFun = fun() {
                viewmodel.applyCouponViewModel.onCouponSubmitClicked.onNext(Unit)
            }
            val cancelFun = fun() {
                showProgress(false)
                enableCouponUi(true)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }

        viewmodel.enableSubmitButtonObservable.subscribe { showButton ->
            mToolbarListener.enableRightActionButton(showButton)
        }

        viewmodel.applyCouponViewModel.onCouponSubmitClicked
                .withLatestFrom(paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse, {
                    _, paymentSplitsAndTripResponse -> paymentSplitsAndTripResponse
                })
                .subscribe {
                    submitCoupon(it.paymentSplits, it.tripResponse)
                }

        setUpViewModelSubscriptions()
    }

    abstract fun showError(show: Boolean)

    abstract fun setUpViewModelSubscriptions()

    abstract fun showHotelCheckoutView(couponInstanceId: String?): Boolean

    abstract fun addProgressView()

    init {
        injectViewInHotelComponent()

        View.inflate(getContext(), getViewToInflate(), this)
        background = null

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
        addProgressView()
        showProgress(false)

        removeCoupon.setOnClickListener {
            resetFields()
            viewmodel.removeObservable.onNext(true)
        }
    }

    open fun resetFields() {
        couponCode.text = null
    }

    abstract fun injectViewInHotelComponent()

    abstract fun getViewToInflate(): Int

    override fun getMenuDoneButtonFocus(): Boolean {
        if (couponCode.text.isNotEmpty()) {
            return true
        }
        return false
    }

    abstract val textWatcher: TextWatcher

    override fun setExpanded(expand: Boolean, animate: Boolean) {
        super.setExpanded(expand, animate)
        if (expand) {
            couponCode.addTextChangedListener(textWatcher)
            showProgress(false)
            expanded.visibility = View.VISIBLE
            unexpanded.visibility = View.GONE
            applied.visibility = View.GONE
            onExpand()
            HotelTracking.trackHotelExpandCoupon()
            couponCode.requestFocus()
            viewmodel.onMenuClickedMethod.onNext { onMenuButtonPressed() }
            Ui.showKeyboard(couponCode, null)
        } else {
            couponCode.removeTextChangedListener(textWatcher)
            resetFields()
            expanded.visibility = View.GONE
            if (viewmodel.hasDiscountObservable.value != null && viewmodel.hasDiscountObservable.value) {
                applied.visibility = View.VISIBLE
                unexpanded.visibility = View.GONE
            } else {
                applied.visibility = View.GONE
                unexpanded.visibility = View.VISIBLE
            }
        }
    }

    abstract fun onExpand()

    override fun getActionBarTitle(): String? {
        return context.getString(R.string.coupon_promo_title)
    }

    override fun onMenuButtonPressed() {
        viewmodel.applyCouponViewModel.onCouponSubmitClicked.onNext(Unit)
    }

    private fun submitCoupon(paymentSplits: PaymentSplits, tripResponse: TripResponse) {
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

        viewmodel.applyCouponViewModel.applyActionCouponParam.onNext(couponParams)
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
        } else if (!isExpanded) {
            OmnitureTracking.trackUserEnterCouponWidget()
            isExpanded = true
        }
    }

    private fun showProgress(show: Boolean) {
        progress.visibility = if (show) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    open fun enableCouponUi(enable: Boolean) {
        couponCode.isEnabled = enable
        viewmodel.enableSubmitButtonObservable.onNext(enable)
    }
}
