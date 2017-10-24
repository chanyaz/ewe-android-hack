package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class TrendingDestinationDetailActivity : AppCompatActivity() {

    val destinationText: TextView by bindView<TextView>(R.id.textView_destination)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trending_destination_detail_activity)

        destinationText.text = intent.getStringExtra("CITY_NAME") + "." + intent.getStringExtra("REGION_ID")

        val toolBar = findViewById(R.id.trending_destination_detail_toolbar) as Toolbar
        toolBar.title = intent.getStringExtra("CITY_NAME") + ", " + intent.getStringExtra("COUNTRY_NAME")
        toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}