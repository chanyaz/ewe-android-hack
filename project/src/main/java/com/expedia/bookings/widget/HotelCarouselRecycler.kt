package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.widget.createHotelMarker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import rx.subjects.PublishSubject
import java.util.ArrayList

public class HotelCarouselRecycler(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    val mapSubject = PublishSubject.create<Marker>()

    val layoutManager = LinearLayoutManager(getContext())

    init {
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL)
        setLayoutManager(layoutManager)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {

        var position : Int

        if (velocityX < 0) {
            position: Int = layoutManager.findFirstVisibleItemPosition()
        } else {
            position: Int = layoutManager.findLastVisibleItemPosition()
        }

        val nextView: View? = layoutManager.findViewByPosition(position)
        if (nextView != null) {
            smoothScrollBy(nextView.getLeft(), 0)
        }

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
}
