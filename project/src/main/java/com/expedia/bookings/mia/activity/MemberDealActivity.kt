package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R

class MemberDealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)

        val toolBar = findViewById(R.id.mod_search_toolbar) as Toolbar
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }
    }
}