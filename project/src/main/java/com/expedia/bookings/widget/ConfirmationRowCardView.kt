package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.flights.FlightConfirmationCardViewModel

open class ConfirmationRowCardView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val title: TextView by bindView(R.id.confirmation_title)
    val subTitle: TextView by bindView(R.id.confirmation_subtitle)
    val icon: ImageView by bindView(R.id.icon)
    val titleSupplement: TextView by bindView(R.id.confirmation_title_supplement)

    var viewModel: FlightConfirmationCardViewModel by notNullAndObservable { vm ->
        vm.titleSubject.subscribeText(title)
        vm.subtitleSubject.subscribeText(subTitle)
        title.typeface = Typeface.DEFAULT_BOLD
        vm.departureDateTitleSubject.subscribeTextAndVisibility(titleSupplement)
        vm.urlSubject.subscribe { url ->
            if (!url.isNullOrBlank()) {
                val fallbackDrawable = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationRow, 0, 0)
                        .getResourceId(R.styleable.ConfirmationRow_row_icon, R.drawable.packages_flight1_icon)
                PicassoHelper.Builder(icon)
                        .setError(fallbackDrawable)
                        .build()
                        .load(url)
            }
        }
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
            title.maxLines = ta.getInt(R.styleable.ConfirmationRow_row_title_max_lines, 1)
            val drawable = ta.getDrawable(R.styleable.ConfirmationRow_row_icon)
            val color = ta.getColor(R.styleable.ConfirmationRow_row_icon_tint_color, 0)
            drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            icon.setImageDrawable(drawable)
        } finally {
            ta.recycle()
        }
    }
}