package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.mia.TrendingDestinationListAdapter
import com.expedia.bookings.mia.TrendingDestinationResponseProvider
import com.expedia.bookings.utils.Ui

class TrendingDestinationActivity : AppCompatActivity() {

    private lateinit var trendingDestinationResponseProvider: TrendingDestinationResponseProvider
    private lateinit var adapter: TrendingDestinationListAdapter

    val recyclerView: RecyclerView by lazy {
        findViewById(R.id.trending_recycler_view) as RecyclerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trending_destination_activity)

        val toolBar = findViewById(R.id.trending_search_toolbar) as Toolbar
        toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        adapter = TrendingDestinationListAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        trendingDestinationResponseProvider = TrendingDestinationResponseProvider(Ui.getApplication(this).appComponent().smartOfferService())
        trendingDestinationResponseProvider.trendingDestinationResponseSubject.subscribe(adapter.resultSubject)
    }

    override fun onResume() {
        super.onResume()
        trendingDestinationResponseProvider.fetchDeals()
    }
}