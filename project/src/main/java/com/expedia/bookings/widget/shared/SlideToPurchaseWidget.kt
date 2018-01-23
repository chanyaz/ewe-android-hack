package com.expedia.bookings.widget.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView

class SlideToPurchaseWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)
    val purchaseTotalText: TextView by bindView(R.id.purchase_total_text_view)

    init {
        View.inflate(context, R.layout.slide_to_purchase_widget, this)
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            slideToPurchase.setOnClickListener {
                slideToPurchase.fireSlideAllTheWay()
            }
        }
    }

    fun addSlideListener(slideListener: SlideToWidgetLL.ISlideToListener) {
        slideToPurchase.addSlideToListener(slideListener)
    }

    fun reset() {
        slideToPurchase.resetSlider()
    }

    fun show() {
        isFocusable = true
        visibility = View.VISIBLE

        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            //hide the slider for talkback users and show a purchase button
            slideToPurchase.setText(context.getString(R.string.accessibility_purchase_button))
            slideToPurchase.hideTouchTarget()
        }
    }

    fun updatePricingDisplay(totalPrice: String) {
        purchaseTotalText.visibility = View.VISIBLE
        purchaseTotalText.text = totalPrice
    }
}
