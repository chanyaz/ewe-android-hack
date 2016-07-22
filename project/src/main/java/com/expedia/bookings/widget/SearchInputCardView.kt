package com.expedia.bookings.widget

import android.content.Context
import android.graphics.drawable.Drawable
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
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchInput, 0, 0)
            try {
                text.text = ta.getString(R.styleable.SearchInput_input_text)
                val drawableLeft = ta.getDrawable(R.styleable.SearchInput_input_icon)
                val color = ta.getColor(R.styleable.SearchInput_input_icon_tint_color, 0)
                text.setTintedDrawable(drawableLeft, color)
            } finally {
                ta.recycle()
            }
        }
    }

    fun setEndDrawable(endDrawable: Drawable) {
        val startDrawable = text.compoundDrawables[0]
        val topDrawable = text.compoundDrawables[1]
        val bottomDrawable = text.compoundDrawables[3]
        text.setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
    }

    fun setText(inputText: String) {
        text.text = inputText
    }
}
