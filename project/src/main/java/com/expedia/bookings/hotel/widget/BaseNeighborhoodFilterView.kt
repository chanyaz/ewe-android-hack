package com.expedia.bookings.hotel.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.annotation.CallSuper
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelsNeighborhoodFilter
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import rx.subjects.PublishSubject

abstract class BaseNeighborhoodFilterView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val neighborhoodOnSubject = PublishSubject.create<HotelSearchResponse.Neighborhood>()
    val neighborhoodOffSubject = PublishSubject.create<HotelSearchResponse.Neighborhood>()

    protected  val collapseViewCount = 3
    private val ANIMATION_DURATION = 500L
    private var expanded = false

    private val expandAnimator: ResizeHeightAnimator
    private val collapseAnimator: ResizeHeightAnimator

    protected val moreLessView: NeighborhoodMoreLessView by bindView(R.id.neighborhood_more_less_view)

    init {
        expandAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        collapseAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        addAnimationListeners()
    }

    abstract fun clear()

    @CallSuper
    open fun updateNeighborhoods(list: List<HotelSearchResponse.Neighborhood>) {
        moreLessView.visibility = if (list.size > collapseViewCount) View.VISIBLE else View.GONE
    }

    protected fun showMoreLessClick(neighborhoodGroup: LinearLayout) {
        if (expanded) {
            collapse(neighborhoodGroup)
        } else {
            expandAnimator.addViewSpec(neighborhoodGroup, neighborhoodGroup.getChildAt(0).measuredHeight * neighborhoodGroup.childCount)
            expandAnimator.start()
            expanded = true
        }
    }

    protected fun collapse(neighborhoodGroup: LinearLayout) {
        collapseAnimator.addViewSpec(neighborhoodGroup, neighborhoodGroup.getChildAt(0).measuredHeight * collapseViewCount)
        collapseAnimator.start()
        expanded = false
    }

    private fun addAnimationListeners() {
        expandAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                moreLessView.showLess()
            }
        })

        collapseAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                moreLessView.showMore()
            }
        })
    }
}