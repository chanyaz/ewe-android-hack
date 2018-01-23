package com.expedia.bookings.hotel.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.TextView

open class TwoTabCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    protected val leftTabContainer: FrameLayout by bindView(R.id.left_tab_container)
    protected val rightTabContainer: FrameLayout by bindView(R.id.right_tab_container)

    private val leftTabTextView: TextView by bindView(R.id.left_tab_text_view)
    private val rightTabTextView: TextView by bindView(R.id.right_tab_text_view)

    init {
        View.inflate(context, R.layout.two_tab_card_view, this)
        if (attrs != null) {
            val attrSet = context.theme.obtainStyledAttributes(attrs, R.styleable.TwoTabCardView, 0, 0)
            try {
                leftTabTextView.text = attrSet.getString(R.styleable.TwoTabCardView_tab_left_text)
                leftTabTextView.contentDescription = attrSet.getString(R.styleable.TwoTabCardView_tab_left_cont_desc)

                rightTabTextView.text = attrSet.getString(R.styleable.TwoTabCardView_tab_right_text)
                rightTabTextView.contentDescription = attrSet.getString(R.styleable.TwoTabCardView_tab_right_cont_desc)

                val allCaps = attrSet.getBoolean(R.styleable.TwoTabCardView_android_textAllCaps, false)
                leftTabTextView.setAllCaps(allCaps)
                rightTabTextView.setAllCaps(allCaps)
            } finally {
                attrSet.recycle()
            }
        }
    }

    protected fun selectLeft() {
        leftTabContainer.isSelected = true
        rightTabContainer.isSelected = false
    }

    protected fun selectRight() {
        leftTabContainer.isSelected = false
        rightTabContainer.isSelected = true
    }

    protected fun setLeftTabDrawableLeft(drawable: Drawable?) {
        leftTabTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    protected fun setRightTabDrawableLeft(drawable: Drawable?) {
        rightTabTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }
}
