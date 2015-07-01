package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.presenter.hotel.createHotelMarker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import rx.subjects.PublishSubject
import java.util.ArrayList

public class HotelMarkerPreviewRecycler(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    val mapSubject = PublishSubject.create<Marker>()

    val layoutManager = LinearLayoutManager(getContext())

    init {
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL)
        setLayoutManager(layoutManager)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        snapTo(velocityX)
        val position: Int = layoutManager.findLastVisibleItemPosition()
        val sortedHotelList: ArrayList<HotelResultsPresenter.MarkerDistance> = (getAdapter() as HotelMarkerPreviewAdapter).sortedHotelList
        val marker = sortedHotelList.get(position).marker

        // Reset markers
        for (item in sortedHotelList) {
            item.marker.setIcon(createHotelMarker(getResources(), item.hotel, false))
        }

        marker.setIcon(createHotelMarker(getResources(), sortedHotelList.get(position).hotel, true))
        mapSubject.onNext(marker)

        return true
    }

    private fun getScrollDistance(v: View, offset: Int): Int {
        return v.getLeft()
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
            offset = layoutManager.getRightDecorationWidth(v)
        } else {
            position = layoutManager.findLastVisibleItemPosition()
            v = layoutManager.findViewByPosition(position)
            if (v == null) {
                return
            }
            offset = layoutManager.getLeftDecorationWidth(v)
        }

        smoothScrollBy(getScrollDistance(v, offset), 0)
    }

}
