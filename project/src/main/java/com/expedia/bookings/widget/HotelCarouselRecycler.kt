package com.expedia.bookings.widget

import android.content.Context
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.maps.model.Marker
import com.mobiata.android.util.AndroidUtils
import rx.subjects.PublishSubject

public class HotelCarouselRecycler(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    val mapSubject = PublishSubject.create<Marker>()
    val sortedHotelList by lazy { (getAdapter() as HotelMarkerPreviewAdapter).sortedHotelList }
    val sortedHotelMarkerList by lazy { Array(sortedHotelList.size(), {createHotelMarker(getResources(), sortedHotelList.elementAt(it).hotel, false)}) }
    var lastDisplayedItemPosition:Int = 0

    val layoutManager = object: LinearLayoutManager(getContext()) {
        override protected fun getExtraLayoutSpace(state: RecyclerView.State): Int {
            return AndroidUtils.getScreenSize(context).x
        }
    }

    init {
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL)
        setLayoutManager(layoutManager)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        var position = if (velocityX < 0) layoutManager.findFirstVisibleItemPosition() else layoutManager.findLastVisibleItemPosition()

        val nextView: View? = layoutManager.findViewByPosition(position)
        if (nextView != null) {
            smoothScrollBy(nextView.getLeft(), 0)
        }

        mapSubject.onNext(sortedHotelList.get(position).marker)

        sortedHotelMarkerList[lastDisplayedItemPosition] = createHotelMarker(getResources(), sortedHotelList.get(lastDisplayedItemPosition).hotel, false)
        sortedHotelMarkerList[position] = createHotelMarker(getResources(), sortedHotelList.get(position).hotel, true)

        resetMarkersDelayed();

        lastDisplayedItemPosition = position
        return true
    }

    private fun resetMarkersDelayed() {
        val mainHandler = Handler(context.mainLooper);
        val resetMarkersRunnable = Runnable() {
            @Override
            fun run() {
                (0..sortedHotelList.size() - 1).forEach { sortedHotelList.elementAt(it).marker.setIcon(sortedHotelMarkerList.elementAt(it)) }
            }
        };
        mainHandler.post(resetMarkersRunnable);
    }
}
