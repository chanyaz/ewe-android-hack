package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class HotelPoiDistanceActivity : AppCompatActivity() {
    private val toolbar by bindView<Toolbar>(R.id.hotel_poi_toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.hotel_poi_recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.hotel_poi_distance_activity)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
