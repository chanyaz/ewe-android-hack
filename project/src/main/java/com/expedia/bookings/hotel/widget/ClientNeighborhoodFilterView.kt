package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelsNeighborhoodFilter

class ClientNeighborhoodFilterView(context: Context, attrs: AttributeSet?) : BaseNeighborhoodFilterView(context, attrs) {
    private val neighborhoodGroup: LinearLayout by bindView(R.id.neighborhoods)

    init {
        View.inflate(context, R.layout.client_neighborhood_filter_view, this)
        moreLessView.setOnClickListener {
            showMoreLessClick()
        }
    }

    override fun updateNeighborhoods(list: List<Neighborhood>) {
        super.updateNeighborhoods(list)
        neighborhoodGroup.removeAllViews()

        if (list.size > 0) {
            for (i in 0..list.size - 1) {
                val neighborhoodView = LayoutInflater.from(context)
                        .inflate(R.layout.section_hotel_neighborhood_row, neighborhoodGroup, false) as HotelsNeighborhoodFilter
                neighborhoodView.bind(list[i])
                neighborhoodView.neighborhoodSelectedSubject.subscribe(neighborhoodOnSubject)
                neighborhoodView.neighborhoodDeselectedSubject.subscribe(neighborhoodOffSubject)
                neighborhoodGroup.addView(neighborhoodView)
            }
        }
    }

    override fun getNeighborhoodContainer(): LinearLayout {
        return neighborhoodGroup
    }

    override fun clear() {
        for (i in 0..neighborhoodGroup.childCount - 1) {
            val v = neighborhoodGroup.getChildAt(i)
            if (v is HotelsNeighborhoodFilter) {
                v.clear()
            }
        }
    }
}
