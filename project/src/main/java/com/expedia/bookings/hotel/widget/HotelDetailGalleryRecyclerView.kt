package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.utils.AccessibilityUtil
import com.mobiata.android.util.AndroidUtils
import io.reactivex.subjects.PublishSubject

class HotelDetailGalleryRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    val galleryScrolledSubject = PublishSubject.create<Int>()

    private val layoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
        var canA11yScroll = false

        override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
            if (state.hasTargetScrollPosition()) {
                return AndroidUtils.getScreenSize(getContext()).x
            } else {
                return 0
            }
        }

        override fun canScrollHorizontally(): Boolean {
            if (AccessibilityUtil.isTalkBackEnabled(getContext())) {
                return canA11yScroll
            } else {
                return super.canScrollHorizontally()
            }
        }
    }

    init {
        setLayoutManager(layoutManager)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        super.fling(velocityX, velocityY)

        if (Math.abs(velocityX) < ViewConfiguration.get(context).scaledMinimumFlingVelocity) {
            return false
        }
        snapTo(velocityX)
        return true
    }

    fun getPosition(): Int {
        return layoutManager.findFirstVisibleItemPosition()
    }

    fun updateAccessibility(collapsed: Boolean) {
        layoutManager.canA11yScroll = !collapsed

        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()

        (findViewHolderForAdapterPosition(first) as? HotelDetailGalleryViewHolder)?.updateContDesc(collapsed)
        if (first != last) {
            (findViewHolderForAdapterPosition(last) as? HotelDetailGalleryViewHolder)?.updateContDesc(collapsed)
        }
    }

    private fun snapTo(velocityX: Int) {
        val position: Int
        val offset: Int
        val v: View?

        if (velocityX < 0) {
            position = layoutManager.findFirstVisibleItemPosition()
            v = layoutManager.findViewByPosition(position)
            if (v == null) {
                return
            }
            v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            offset = layoutManager.getRightDecorationWidth(v)
        } else {
            position = layoutManager.findLastVisibleItemPosition()
            v = layoutManager.findViewByPosition(position)
            if (v == null) {
                return
            }
            v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            offset = layoutManager.getLeftDecorationWidth(v)
        }

        galleryScrolledSubject.onNext(position)
        smoothScrollBy(v.left - offset * 4, 0)
    }
}
