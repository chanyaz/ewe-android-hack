package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.vm.HotelCouponViewModel
import kotlin.properties.Delegates

/**
 * Created by malnguyen on 8/12/15.
 */
public class CouponWidget(context: Context, attrs: AttributeSet?) : ExpandableCardView(context, attrs) {

    val unexpanded: TextView by bindView(R.id.unexpanded)
    val expanded: LinearLayout by bindView(R.id.expanded)
    val couponCode: EditText by bindView(R.id.edit_coupon_code)

    var viewmodel: HotelCouponViewModel by Delegates.notNull()

    init {
        View.inflate(getContext(), R.layout.coupon_widget, this)
    }

    override fun onFinishInflate() {

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
        viewmodel.doneButtonObservable.onNext(couponCode.getText().toString())
    }

    override fun onLogin() {

    }

    override fun onLogout() {

    }

    override fun isComplete(): Boolean {
        return true;
    }

}