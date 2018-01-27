package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeOnClick
import io.reactivex.subjects.PublishSubject

class TravelerCountSelector(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val minusClickedSubject = PublishSubject.create<Unit>()
    val plusClickedSubject = PublishSubject.create<Unit>()

    val travelerText: TextView by bindView(R.id.traveler_type)
    val travelerAgeLabel: TextView by bindView(R.id.traveler_age_label)
    val travelerPlus: ImageButton by bindView(R.id.traveler_plus)
    val travelerMinus: ImageButton by bindView(R.id.traveler_minus)

    val enabledColor = ContextCompat.getColor(context, R.color.hotel_guest_selector_enabled_color)
    val disabledColor = ContextCompat.getColor(context, R.color.hotel_guest_selector_disabled_color)

    init {
        View.inflate(context, R.layout.traveler_count_selector, this)
        if (attrs != null) {
            val attrSet = context.theme.obtainStyledAttributes(attrs, R.styleable.TravelerCountSelector, 0, 0)
            try {
                travelerMinus.contentDescription = attrSet.getString(R.styleable.TravelerCountSelector_traveler_minus)
                travelerPlus.contentDescription = attrSet.getString(R.styleable.TravelerCountSelector_traveler_plus)
                travelerAgeLabel.text = attrSet.getString(R.styleable.TravelerCountSelector_traveler_age_label)
                travelerAgeLabel.contentDescription = attrSet.getString(R.styleable.TravelerCountSelector_traveler_age_label_desc)
            } finally {
                attrSet.recycle()
            }
        }

        travelerPlus.subscribeOnClick(plusClickedSubject)
        travelerMinus.subscribeOnClick(minusClickedSubject)
    }

    private fun getColorFilter(enabled: Boolean): Int {
        if (enabled)
            return enabledColor
        else return disabledColor
    }

    fun enablePlus(enabled: Boolean) {
        travelerPlus.isEnabled = enabled
        travelerPlus.setColorFilter(getColorFilter(enabled), PorterDuff.Mode.SRC_IN)
    }

    fun enableMinus(enabled: Boolean) {
        travelerMinus.isEnabled = enabled
        travelerMinus.setColorFilter(getColorFilter(enabled), PorterDuff.Mode.SRC_IN)
    }
}
