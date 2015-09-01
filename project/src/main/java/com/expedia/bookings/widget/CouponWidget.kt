package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.vm.HotelCouponViewModel
import kotlin.properties.Delegates

/**
 * Created by malnguyen on 8/12/15.
 */
public class CouponWidget(context: Context, attrs: AttributeSet?) : ExpandableCardView(context, attrs) {

    val unexpanded: TextView by bindView(R.id.unexpanded)
    val expanded: LinearLayout by bindView(R.id.expanded)
    val couponCode: EditText by bindView(R.id.edit_coupon_code)
    val error: TextView by bindView(R.id.error_message)
    var progress: View by Delegates.notNull()

    var viewmodel: HotelCouponViewModel by notNullAndObservable {
        viewmodel.errorMessageObservable.subscribe(error)
        viewmodel.applyObservable.subscribe {
            showProgress(true)
            showError(false)
        }
        viewmodel.errorObservable.subscribe {
            showProgress(false)
            showError(true)
        }
        viewmodel.couponObservable.subscribe {
            showProgress(false)
            showError(false)
            setExpanded(false)
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
        expanded.addView(progress)
        showProgress(false)
    }

    override fun getDoneButtonFocus(): Boolean {
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
            if (mToolbarListener != null) {
                mToolbarListener.onEditingComplete()
            }
        } else {
            setBackgroundResource(R.drawable.card_background)
            expanded.setVisibility(View.GONE)
            unexpanded.setVisibility(View.VISIBLE)
        }
    }

    override fun getActionBarTitle(): String? {
        return getContext().getString(R.string.coupon_promo_title)
    }

    override fun onDonePressed() {
        viewmodel.couponParamsObservable.onNext(HotelApplyCouponParams(Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId, couponCode.getText().toString()))
    }

    override fun onLogin() {

    }

    override fun onLogout() {

    }

    override fun isComplete(): Boolean {
        return true;
    }

    private fun showProgress(show: Boolean) {
        progress.setVisibility(if (show) { View.VISIBLE } else { View.INVISIBLE })
    }

    private fun showError(show: Boolean) {
        error.setVisibility(if (show) { View.VISIBLE } else { View.INVISIBLE })
    }
}