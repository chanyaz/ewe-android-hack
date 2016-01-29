package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

open class SearchInputCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val icon: ImageView by bindView(R.id.input_icon)
    val text: TextView by bindView(R.id.input_label)


    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    init {
        View.inflate(context, R.layout.cardview_search_input, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchInput, 0, 0)
        try {
            text.text = ta.getString(R.styleable.SearchInput_input_text)
            icon.setImageDrawable(ta.getDrawable(R.styleable.SearchInput_input_icon))
            val color = ta.getColor(R.styleable.SearchInput_input_icon_tint_color, 0)
            icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

        } finally {
            ta.recycle()
        }

    }

    fun setText(inputText: String) {
        text.text = inputText
    }
}
