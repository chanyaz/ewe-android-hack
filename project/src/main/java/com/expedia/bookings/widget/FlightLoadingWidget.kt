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
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.bindView
import com.larvalabs.svgandroid.widget.SVGView
import com.mobiata.android.util.AndroidUtils.dpToPx
import java.util.Locale

class FlightLoadingWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val originTextView: TextView by bindView(R.id.origin_place_holder)
    private val destinationTextView: TextView by bindView(R.id.destination_place_holder)
    private val airplaneAnimator: LottieAnimationView by bindView(R.id.airplane_animation)
    private val searchingAirlineMessage: TextView by bindView(R.id.searching_flights)
    private val rightHandArrow: SVGView by bindView(R.id.right_hand_arrow)
    private var airplaneAnimatorHeight = 0
    private var flightLoadingWidgetHeight = 0
    private var anim: ValueAnimator? = null
    private val pointOfSale = PointOfSale.getPointOfSale()
    private val twoLetterCountryCode = pointOfSale.twoLetterCountryCode
    private val isPointOfSaleWithHundredsOfAirlines = !twoLetterCountryCode.toUpperCase(Locale.US).contains(Regex("PH|ID|KR"))

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
                    animateWidget(flightLoadingWidgetHeight, dpToPx(context, 168), 1200, 5000)
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
                    animateWidget(flightLoadingWidgetHeight, 0, 800, 0)
                }
            }
        })
    }

    private fun initialiseLoadingState() {
        setHeight(this, LayoutParams.MATCH_PARENT)
        if (!isPointOfSaleWithHundredsOfAirlines) {
            searchingAirlineMessage.text = context.resources.getString(R.string.loading_flights)
        }
        searchingAirlineMessage.visibility = View.VISIBLE
        LayoutUtils.setSVG(rightHandArrow, R.raw.flight_recent_search_one_way)
    }

    private fun animateWidget(fromHeight: Int, toHeight: Int, animDuration: Long, startDelay: Long) {
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
