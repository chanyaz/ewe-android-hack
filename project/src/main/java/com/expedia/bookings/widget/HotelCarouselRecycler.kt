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
    val sortedHotelMarkerDistanceList by lazy { (adapter as HotelMarkerPreviewAdapter).sortedHotelMarkerDistanceList }
    var lastDisplayedItemPosition:Int = 0

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
            smoothScrollBy(nextView.getLeft(), 0)
        }

        mapSubject.onNext(sortedHotelMarkerDistanceList.get(position).marker)

        sortedHotelMarkerDistanceList[lastDisplayedItemPosition].icon = createHotelMarkerIcon(getResources(), sortedHotelMarkerDistanceList.get(lastDisplayedItemPosition).hotel, false)
        sortedHotelMarkerDistanceList[position].icon = createHotelMarkerIcon(getResources(), sortedHotelMarkerDistanceList.get(position).hotel, true)

        resetMarkersDelayed()

        lastDisplayedItemPosition = position
        return true
    }

    private fun resetMarkersDelayed() {
        val mainHandler = Handler(context.mainLooper);
        val resetMarkersRunnable = Runnable() {
            @Override
            fun run() {
                sortedHotelMarkerDistanceList.forEach { it.marker.setIcon(it.icon) }
            }
        };
        mainHandler.post(resetMarkersRunnable);
    }
}
