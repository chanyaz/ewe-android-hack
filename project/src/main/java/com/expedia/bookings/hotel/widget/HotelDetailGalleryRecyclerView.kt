package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.AccessibilityUtil
import com.mobiata.android.util.AndroidUtils
import rx.subjects.PublishSubject
import java.lang.ref.WeakReference

class HotelDetailGalleryRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    val galleryScrolledSubject = PublishSubject.create<Int>()

    private val layoutManager = object: LinearLayoutManager(context, HORIZONTAL, false) {
        var canA11yScroll = false

        override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
            if (state.hasTargetScrollPosition() || Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelDetailsGalleryPeek)) {
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

    fun updateAccessibility(collapsed: Boolean) {
        layoutManager.canA11yScroll = !collapsed

        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()

        (findViewHolderForAdapterPosition(first) as? HotelDetailGalleryViewHolder)?.updateContDesc(collapsed)
        if (first != last) {
            (findViewHolderForAdapterPosition(last) as? HotelDetailGalleryViewHolder)?.updateContDesc(collapsed)
        }
    }

    fun peekSecondImage() {
        if (adapter.itemCount > 1) {
            val visibleView = layoutManager.findViewByPosition(0)
            val peekingView = layoutManager.findViewByPosition(1)

            if (visibleView != null && peekingView != null) {
                animatePeekWithBounce(peekingView)
                animatePeekWithBounce(visibleView)
            }
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
            v!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            offset = layoutManager.getRightDecorationWidth(v)
        } else {
            position = layoutManager.findLastVisibleItemPosition()
            v = layoutManager.findViewByPosition(position)
            if (v == null) {
                return
            }
            v!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            offset = layoutManager.getLeftDecorationWidth(v)
        }

        galleryScrolledSubject.onNext(position)
        smoothScrollBy(v.left - offset * 4, 0)
    }

    private fun animatePeekWithBounce(view: View) {
        val duration = 1000L
        val peekAmount = resources.getDimension(R.dimen.hotel_gallery_peek_amount).toInt()
        view.animate().translationX((-peekAmount).toFloat()).setStartDelay(duration / 2)
                .setInterpolator(AccelerateInterpolator()).setDuration(duration / 4)
                .withEndAction(TranslateViewRunnable(view, 0, BounceInterpolator(), duration))
    }

    private class TranslateViewRunnable(view: View, private val translationX: Int, private val interpolator: Interpolator, private val duration: Long?) : Runnable {
        private val viewReference: WeakReference<View>

        init {
            this.viewReference = WeakReference(view)
        }

        override fun run() {
            val view = viewReference.get()
            if (view != null) {
                view.animate().translationX(translationX.toFloat()).setStartDelay(0).setInterpolator(interpolator).setDuration(duration!!).start()
            }
        }
    }
}