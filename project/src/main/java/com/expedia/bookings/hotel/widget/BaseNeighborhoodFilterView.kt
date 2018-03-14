package com.expedia.bookings.hotel.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.annotation.CallSuper
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import io.reactivex.subjects.PublishSubject

abstract class BaseNeighborhoodFilterView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val neighborhoodOnSubject = PublishSubject.create<Neighborhood>()
    val neighborhoodOffSubject = PublishSubject.create<Neighborhood>()

    protected val collapseViewCount = 3
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
    protected abstract fun getNeighborhoodContainer(): LinearLayout

    @CallSuper
    open fun updateNeighborhoods(list: List<Neighborhood>) {
        moreLessView.visibility = if (list.size > collapseViewCount) View.VISIBLE else View.GONE
    }

    fun collapse() {
        val neighborhoodGroup = getNeighborhoodContainer()
        collapseAnimator.addViewSpec(neighborhoodGroup, neighborhoodGroup.getChildAt(0).measuredHeight * collapseViewCount)
        collapseAnimator.start()
        expanded = false
    }

    protected fun showMoreLessClick() {
        if (expanded) {
            collapse()
        } else {
            val neighborhoodGroup = getNeighborhoodContainer()
            expandAnimator.addViewSpec(neighborhoodGroup, neighborhoodGroup.getChildAt(0).measuredHeight * neighborhoodGroup.childCount)
            expandAnimator.start()
            expanded = true
        }
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
