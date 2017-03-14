package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelsNeighborhoodFilter

class ClientNeighborhoodFilterView(context: Context, attrs: AttributeSet?) : BaseNeighborhoodFilterView(context, attrs) {

    private val neighborhoodContainer: LinearLayout by bindView(R.id.neighborhoods)

    init {
        View.inflate(context, R.layout.client_neighborhood_filter_view, this)
        moreLessView.setOnClickListener {
            showMoreLessClick(neighborhoodContainer)
        }
    }

    override fun updateNeighborhoods(list: List<HotelSearchResponse.Neighborhood>) {
        super.updateNeighborhoods(list)
        neighborhoodContainer.removeAllViews()

        if (list.size > 1) {
            for (i in 0..list.size - 1) {
                val neighborhoodView = LayoutInflater.from(getContext())
                        .inflate(R.layout.section_hotel_neighborhood_row, neighborhoodContainer, false) as HotelsNeighborhoodFilter
                neighborhoodView.bind(list[i])
                neighborhoodView.neighborhoodSelectedSubject.subscribe(neighborhoodOnSubject)
                neighborhoodView.neighborhoodDeselectedSubject.subscribe(neighborhoodOffSubject)
                neighborhoodContainer.addView(neighborhoodView)
            }
            neighborhoodContainer.post {
                //http://stackoverflow.com/questions/3602026/linearlayout-height-in-oncreate-is-0/3602144#3602144
                collapse(neighborhoodContainer)
            }
        }
    }

    override fun clear() {
        for (i in 0..neighborhoodContainer.childCount - 1) {
            val v = neighborhoodContainer.getChildAt(i)
            if (v is HotelsNeighborhoodFilter) {
                v.clear()
            }
        }
    }
}