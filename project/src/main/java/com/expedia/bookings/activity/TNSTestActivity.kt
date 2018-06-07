package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R

class TNSTestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tns_test)

        recyclerView = findViewById<RecyclerView>(R.id.tns_test_list).apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = TNSTestAdapter()
        }
    }
}
