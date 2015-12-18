package com.expedia.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import com.expedia.bookings.R

public class PackageActivity : AppCompatActivity() {
    val HOTEL_REQUEST_CODE = 101
    val FLIGHT_REQUEST_CODE = 102

    val selectHotelsButton: CardView by lazy {
        findViewById(R.id.hotels_card_view) as CardView
    }
    val selectDepartureButton: CardView by  lazy {
        findViewById(R.id.flight_departure_card_view)  as CardView
    }
    val selectArrivalButton: CardView by  lazy {
        findViewById(R.id.flight_arrival_card_view)  as CardView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.package_activity)
        selectHotelsButton.setOnClickListener {
            openHotels()
        }
        selectDepartureButton.setOnClickListener {
            openFlights()
        }
        selectArrivalButton.setOnClickListener {
            openFlights()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun openHotels() {
        val intent = Intent(this, HotelPackageActivity::class.java)
        startActivityForResult(intent, HOTEL_REQUEST_CODE , null)
    }

    fun openFlights() {
        val intent = Intent(this, HotelPackageActivity::class.java)
        startActivityForResult(intent, FLIGHT_REQUEST_CODE , null)
    }

    fun showCheckout() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
    }

}