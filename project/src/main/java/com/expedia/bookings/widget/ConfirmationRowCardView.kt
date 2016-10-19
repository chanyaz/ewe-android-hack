package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.flights.FlightConfirmationCardViewModel

open class ConfirmationRowCardView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val title: TextView by bindView(R.id.first_row)
    val subTitle: TextView by bindView(R.id.second_row)
    val icon: ImageView by bindView(R.id.icon)

    var viewModel: FlightConfirmationCardViewModel by notNullAndObservable { vm ->
        vm.titleSubject.subscribeText(title)
        vm.subtitleSubject.subscribeText(subTitle)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        orientation = HORIZONTAL
    }

    init {
        View.inflate(context, R.layout.confirmation_row, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationRow, 0, 0)
        try {
            title.text = ta.getString(R.styleable.ConfirmationRow_row_title)
            subTitle.text = ta.getString(R.styleable.ConfirmationRow_row_subTitle)
            val drawable = ta.getDrawable(R.styleable.ConfirmationRow_row_icon)
            val color = ta.getColor(R.styleable.ConfirmationRow_row_icon_tint_color, 0)
            drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            icon.setImageDrawable(drawable)
        } finally {
            ta.recycle()
        }
    }

    fun setTitle(inputText: String) {
        title.text = inputText
    }

    fun setSubTitle(inputText: String) {
        subTitle.text = inputText
    }

    fun setIcon(drawable: Int) {
        icon.setImageDrawable(ContextCompat.getDrawable(context, drawable))
    }
}