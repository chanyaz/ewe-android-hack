package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking

class CustomerFirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_first_support_activity)
    }

    override fun onResume() {
        super.onResume()
        OmnitureTracking.trackCustomerFirstSupportPageLoad()
    }
}
