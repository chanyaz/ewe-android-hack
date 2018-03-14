package com.expedia.bookings.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.bindView
import com.larvalabs.svgandroid.widget.SVGView
import com.mobiata.android.util.AndroidUtils.dpToPx

class FlightLoadingWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val originTextView: TextView by bindView(R.id.origin_place_holder)
    private val destinationTextView: TextView by bindView(R.id.destination_place_holder)
    private val airplaneAnimator: LottieAnimationView by bindView(R.id.airplane_animation)
    private val searchingAirlineMessage: TextView by bindView(R.id.searching_flights)
    private val rightHandArrow: SVGView by bindView(R.id.right_hand_arrow)
    private var airplaneAnimatorHeight = 0
    private var flightLoadingWidgetHeight = 0
    private var anim: ValueAnimator? = null

    init {
        View.inflate(context, R.layout.flight_loading_view, this)
    }

    fun setupLoadingState() {
        initialiseLoadingState()
        originTextView.text = Db.getFlightSearchParams()?.departureAirport?.hierarchyInfo?.airport?.airportCode
        destinationTextView.text = Db.getFlightSearchParams()?.arrivalAirport?.hierarchyInfo?.airport?.airportCode
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (height != 0) {
                    flightLoadingWidgetHeight = height
                    airplaneAnimatorHeight = airplaneAnimator.height
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    animateWidget(flightLoadingWidgetHeight, dpToPx(context, 168), 1200, 5000, false)
                }
            }
        })
    }

    fun setResultReceived() {
        if (searchingAirlineMessage.visibility == View.VISIBLE) {
            searchingAirlineMessage.visibility = View.GONE
        }
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (height != 0) {
                    flightLoadingWidgetHeight = height
                    airplaneAnimatorHeight = airplaneAnimator.height
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    animateWidget(flightLoadingWidgetHeight, 0, 1000, 0, true)
                }
            }
        })
    }

    private fun initialiseLoadingState() {
        setHeight(this, LayoutParams.MATCH_PARENT)
        searchingAirlineMessage.visibility = View.VISIBLE
        LayoutUtils.setSVG(rightHandArrow, R.raw.flight_recent_search_one_way)
    }

    private fun animateWidget(fromHeight: Int, toHeight: Int, animDuration: Long, startDelay: Long, isResultReceived: Boolean) {
        anim?.cancel()
        anim = ValueAnimator.ofInt(fromHeight, toHeight)
        anim?.duration = animDuration
        anim?.startDelay = startDelay
        anim?.addUpdateListener { valueAnimator ->
            if (searchingAirlineMessage.visibility == View.VISIBLE) {
                searchingAirlineMessage.visibility = View.GONE
            }
            val calculatedWidgetHeight = valueAnimator.animatedValue as Int
            setHeight(this, calculatedWidgetHeight)
        }
        anim?.start()
    }

    private fun setHeight(view: View, height: Int) {
        val params = view.layoutParams
        params.height = height
        view.layoutParams = params
    }
}
