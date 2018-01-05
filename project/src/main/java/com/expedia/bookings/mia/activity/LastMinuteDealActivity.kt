package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.mia.LastMinuteDealListAdapter
import com.expedia.bookings.mia.LastMinuteDealsResponseProvider
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled

class LastMinuteDealActivity : AppCompatActivity() {

    private lateinit var lastMinuteDealsResponseProvider: LastMinuteDealsResponseProvider
    private lateinit var adapter: LastMinuteDealListAdapter
    val recyclerView by bindView<RecyclerView>(R.id.last_minute_deal_recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.last_minute_deal_activity)

        val toolBar = findViewById<Toolbar>(R.id.lmd_search_toolbar)
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        adapter = LastMinuteDealListAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lastMinuteDealsResponseProvider = LastMinuteDealsResponseProvider(Ui.getApplication(this).appComponent().smartOfferService())
        lastMinuteDealsResponseProvider.dealsResponseSubject.subscribe(adapter.resultSubject)

        if (isBrandColorEnabled(this@LastMinuteDealActivity)) {
            toolBar.setBackgroundColor(ContextCompat.getColor(this@LastMinuteDealActivity, R.color.brand_primary))
            window.statusBarColor = ContextCompat.getColor(this@LastMinuteDealActivity, R.color.brand_primary_dark)
        }
    }

    override fun onResume() {
        super.onResume()
        lastMinuteDealsResponseProvider.fetchDeals()
        OmnitureTracking.trackLastMinuteDealsPageLoad()
    }
}
