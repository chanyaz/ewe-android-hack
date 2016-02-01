package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

open class SearchInputCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val text: TextView by bindView(R.id.input_label)

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    init {
        View.inflate(context, R.layout.cardview_search_input, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchInput, 0, 0)
        try {
            text.text = ta.getString(R.styleable.SearchInput_input_text)
            val drawableLeft = ta.getDrawable(R.styleable.SearchInput_input_icon)
            val color = ta.getColor(R.styleable.SearchInput_input_icon_tint_color, 0)
            drawableLeft.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            text.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null)

        } finally {
            ta.recycle()
        }

    }

    fun setText(inputText: String) {
        text.text = inputText
    }
}
