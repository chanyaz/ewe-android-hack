package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.HotelCouponViewModel
import com.mobiata.android.util.Ui
import kotlin.properties.Delegates

/**
 * Created by malnguyen on 8/12/15.
 */
public class CouponWidget(context: Context, attrs: AttributeSet?) : ExpandableCardView(context, attrs) {

    val unexpanded: TextView by bindView(R.id.unexpanded)
    val expanded: LinearLayout by bindView(R.id.expanded)
    val applied: LinearLayout by bindView(R.id.applied)
    val couponCode: EditText by bindView(R.id.edit_coupon_code)
    val error: TextView by bindView(R.id.error_message)
    val appliedCouponMessage: TextView by bindView(R.id.applied_coupon_text)
    val removeCoupon: ImageView by bindView(R.id.remove_coupon_button)
    var progress: View by Delegates.notNull()

    var removingCoupon: Boolean = false

    var viewmodel: HotelCouponViewModel by notNullAndObservable {
        viewmodel.errorMessageObservable.subscribeText(error)
        viewmodel.applyObservable.subscribe {
            showProgress(true)
            showError(false)
        }
        viewmodel.errorObservable.subscribe {
            showProgress(false)
            if (viewmodel.hasDiscountObservable.value != null && viewmodel.hasDiscountObservable.value) {
                setExpanded(false)
            } else {
                showError(true)
            }
        }
        viewmodel.couponObservable.subscribe {
            showProgress(false)
            showError(false)
            setExpanded(false)
            removingCoupon = false
        }
        viewmodel.discountObservable.subscribe {
            appliedCouponMessage.text = context.getString(R.string.applied_coupon_message, it)
        }
    }

    val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            mToolbarListener.showRightActionButton(s.length() > 3)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

    }

    init {
        View.inflate(getContext(), R.layout.coupon_widget, this)
        //Tests hates progress bars
        if (ExpediaBookingApp.isAutomation()) {
            //Espresso hates progress bars
            progress = View(getContext(), null)
        } else {
            progress = ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER_VERTICAL
            progress.setLayoutParams(lp)
            (progress as ProgressBar).setIndeterminate(true)
        }
        couponCode.addTextChangedListener(textWatcher)
        couponCode.setOnEditorActionListener(object : android.widget.TextView.OnEditorActionListener {
            override fun onEditorAction(textView: android.widget.TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE && textView.text.length() > 3) {
                    onMenuButtonPressed()
                }
                return false
            }
        })
        expanded.addView(progress)
        showProgress(false)

        removeCoupon.setOnClickListener {
            resetFields()
            removingCoupon = true
            viewmodel.removeObservable.onNext(Unit)
            viewmodel.hasDiscountObservable.onNext(false)
            HotelV2Tracking().trackHotelV2CouponRemove(couponCode.text.toString())
        }
    }

    override fun getMenuDoneButtonFocus(): Boolean {
        if (couponCode.getText().length() > 0) {
            return true
        }
        return false
    }

    override fun setExpanded(expand: Boolean, animate: Boolean) {
        super.setExpanded(expand, animate)
        if (expand) {
            setBackground(null)
            expanded.setVisibility(View.VISIBLE)
            unexpanded.setVisibility(View.GONE)
            applied.setVisibility(View.GONE)
            if (mToolbarListener != null) {
                mToolbarListener.onEditingComplete()
                mToolbarListener.showRightActionButton(false)
            }
            HotelV2Tracking().trackHotelV2ExpandCoupon()
            couponCode.requestFocus()
            Ui.showKeyboard(couponCode, null)
        } else {
            resetFields()
            setBackgroundResource(R.drawable.card_background)
            expanded.setVisibility(View.GONE)
            if (viewmodel.hasDiscountObservable.value != null && viewmodel.hasDiscountObservable.value) {
                applied.setVisibility(View.VISIBLE)
                unexpanded.setVisibility(View.GONE)
            }
            else {
                applied.setVisibility(View.GONE)
                unexpanded.setVisibility(View.VISIBLE)
            }
        }
    }

    override fun getActionBarTitle(): String? {
        return getContext().getString(R.string.coupon_promo_title)
    }

    override fun onMenuButtonPressed() {
        viewmodel.couponParamsObservable.onNext(HotelApplyCouponParams(Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId, couponCode.getText().toString(), false))
    }

    override fun onLogin() {

    }

    override fun onLogout() {

    }

    override fun isComplete(): Boolean {
        return true;
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
        progress.setVisibility(if (show) { View.VISIBLE } else { View.INVISIBLE })
    }

    private fun showError(show: Boolean) {
        error.setVisibility(if (show) { View.VISIBLE } else { View.INVISIBLE })
    }

    override fun getMenuButtonTitle(): String? {
       return getResources().getString(R.string.coupon_submit_button)
    }

    private fun resetFields() {
        couponCode.setText(null)
        error.setText(null)
    }
}
