package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class HotelFavoritesActivity : AppCompatActivity() {

    private val toolbar by bindView<Toolbar>(R.id.hotel_favorites_toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_favorites_layout)
        setTitle(R.string.hotel_favorites_page_toolbar_title)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
