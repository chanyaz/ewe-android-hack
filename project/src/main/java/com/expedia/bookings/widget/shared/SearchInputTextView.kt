package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.widget.TextView

open class SearchInputTextView(context: Context, attrs: AttributeSet?) : TextView(context, attrs) {
    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchInput, 0, 0)
            try {
                text = ta.getString(R.styleable.SearchInput_input_text)
                val drawableLeft = ta.getDrawable(R.styleable.SearchInput_input_icon)
                val color = ta.getColor(R.styleable.SearchInput_input_icon_tint_color, 0)
                setTintedDrawable(drawableLeft, color)
            } finally {
                ta.recycle()
            }
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], compoundDrawables[3])
    }

    fun setEndDrawable(endDrawable: Drawable?) {
        val startDrawable = compoundDrawablesRelative[0]
        val topDrawable = compoundDrawablesRelative[1]
        val bottomDrawable = compoundDrawablesRelative[3]
        setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
    }

    fun setText(inputText: String) {
        text = inputText
    }
}
