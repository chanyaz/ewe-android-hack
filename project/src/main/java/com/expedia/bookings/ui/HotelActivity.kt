package com.expedia.bookings.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.Ui
import kotlin.properties.Delegates

public class HotelActivity : AppCompatActivity() {

    val hotelPresenter: HotelPresenter by Delegates.lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultHotelComponents()
        setContentView(R.layout.activity_hotel)
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            super.onBackPressed()
        }
    }

}

