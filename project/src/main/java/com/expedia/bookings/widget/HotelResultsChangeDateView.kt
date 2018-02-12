package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.hotel.animation.TranslateYAnimator
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import io.reactivex.subjects.PublishSubject

class HotelResultsChangeDateView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    // Input
    val changeDateStringSubject = PublishSubject.create<String>()
    val guestStringSubject = PublishSubject.create<String>()

    // Output
    val calendarClickedSubject = PublishSubject.create<Unit>()

    private val changeDateContainer: RelativeLayout by bindView(R.id.change_date_container)
    private val changeDateCalendarIcon: ImageView by bindView(R.id.change_date_calendar_icon)
    private val changeDateCalendarTextView: TextView by bindView(R.id.change_date_calendar_text_view)
    private val changeDateGuestTextView: TextView by bindView(R.id.change_date_guest_text_view)

    private val changeDatesAnimationDuration = 250L
    private val containerHeight = context.resources.getDimensionPixelSize(R.dimen.hotel_results_change_date_height)
    private lateinit var translateInAnimator: TranslateYAnimator
    private lateinit var translateOutAnimator: TranslateYAnimator
    private var changeDateAnimationRunning = false

    init {
        View.inflate(context, R.layout.hotel_results_change_date_view, this)

        changeDateCalendarIcon.setColorFilter(ContextCompat.getColor(context, R.color.hotel_search_info_selectable_color), PorterDuff.Mode.SRC_IN)
        changeDateCalendarIcon.setOnClickListener {
            calendarClickedSubject.onNext(Unit)
        }

        changeDateCalendarTextView.setOnClickListener {
            calendarClickedSubject.onNext(Unit)
        }

        setUpAnimations()

        changeDateStringSubject.subscribeText(changeDateCalendarTextView)
        guestStringSubject.subscribeText(changeDateGuestTextView)
    }

    fun animateIn() {
        if (!changeDateAnimationRunning && changeDateContainer.visibility != View.VISIBLE) {
            translateInAnimator.start()
        }
    }

    fun animateOut() {
        if (!changeDateAnimationRunning && changeDateContainer.visibility == View.VISIBLE) {
            translateOutAnimator.start()
        }
    }

    private fun setUpAnimations() {
        translateInAnimator = TranslateYAnimator(this, -containerHeight.toFloat(), 0f, changeDatesAnimationDuration,
                { translateInStartAction() }, { translateInEndAction() })

        translateOutAnimator = TranslateYAnimator(this, 0f, -containerHeight.toFloat(), changeDatesAnimationDuration,
                { translateOutStartAction() }, { translateOutEndAction() })
    }

    private fun translateInStartAction() {
        changeDateContainer.visibility = View.VISIBLE
        changeDateAnimationRunning = true
    }

    private fun translateInEndAction() {
        changeDateAnimationRunning = false
    }

    private fun translateOutStartAction() {
        changeDateAnimationRunning = true
    }

    private fun translateOutEndAction() {
        changeDateContainer.visibility = View.INVISIBLE
        changeDateAnimationRunning = false
    }
}
