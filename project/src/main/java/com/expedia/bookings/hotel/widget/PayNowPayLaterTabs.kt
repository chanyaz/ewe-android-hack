package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.util.subscribeOnClick
import com.expedia.util.unsubscribeOnClick
import io.reactivex.subjects.PublishSubject

class PayNowPayLaterTabs(context: Context, attrs: AttributeSet?) : TwoTabCardView(context, attrs) {
    val payNowClickedSubject = PublishSubject.create<Unit>()
    val payLaterClickedSubject = PublishSubject.create<Unit>()

    private val checkMarkIcon = ContextCompat.getDrawable(context, R.drawable.sliding_radio_selector_left)

    fun selectPayNowTab() {
         selectLeft()
         setLeftTabDrawableLeft(checkMarkIcon)
         setRightTabDrawableLeft(null)
         rightTabContainer.subscribeOnClick(payLaterClickedSubject)
         leftTabContainer.unsubscribeOnClick()
    }

    fun selectPayLaterTab() {
        selectRight()
        setLeftTabDrawableLeft(null)
        setRightTabDrawableLeft(checkMarkIcon)
        leftTabContainer.subscribeOnClick(payNowClickedSubject)
        rightTabContainer.unsubscribeOnClick()
    }

    fun unsubscribeClicks() {
        leftTabContainer.unsubscribeOnClick()
        rightTabContainer.unsubscribeOnClick()
    }
}
