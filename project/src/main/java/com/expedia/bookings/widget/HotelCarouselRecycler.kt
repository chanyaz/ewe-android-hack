package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.hotels.Hotel
import com.mobiata.android.util.AndroidUtils
import rx.subjects.PublishSubject

public class HotelCarouselRecycler(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    val mapSubject = PublishSubject.create<Hotel>()

    val layoutManager = object: LinearLayoutManager(getContext()) {
        override protected fun getExtraLayoutSpace(state: RecyclerView.State): Int {
            return AndroidUtils.getScreenSize(context).x
        }
    }

    init {
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        setLayoutManager(layoutManager)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        var position = if (velocityX < 0) layoutManager.findFirstVisibleItemPosition() else layoutManager.findLastVisibleItemPosition()

        val nextView: View? = layoutManager.findViewByPosition(position)
        if (nextView != null) {
            smoothScrollBy(nextView.left, 0)
        }

        val adapter = adapter as HotelMapCarouselAdapter
        if (adapter.hotels.isNotEmpty()) {
            mapSubject.onNext(adapter.hotels[position])
        }
        return true
    }

}
