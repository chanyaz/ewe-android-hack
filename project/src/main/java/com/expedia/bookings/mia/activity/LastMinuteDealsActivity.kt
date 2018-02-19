package com.expedia.bookings.mia.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.mia.LastMinuteDealsListAdapter
import com.expedia.bookings.mia.arch.LastMinuteDealsArchViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled

open class LastMinuteDealsActivity : AppCompatActivity() {

    private val toolBar by bindView<Toolbar>(R.id.lmd_search_toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.last_minute_deal_recycler_view)

    protected open val viewModel: LastMinuteDealsArchViewModel by lazy {
        val factory = LastMinuteDealsArchViewModel.Factory(
                Ui.getApplication(this).appComponent().offerService(), createServiceRequest())
        ViewModelProviders.of(this, factory).get(LastMinuteDealsArchViewModel::class.java)
    }
    protected lateinit var adapter: LastMinuteDealsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.last_minute_deal_activity)

        setupToolbar()

        adapter = LastMinuteDealsListAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.responseLiveData.observe(this, adapter.responseObserver)
    }

    private fun setupToolbar() {
        toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
        if (isBrandColorEnabled(this@LastMinuteDealsActivity)) {
            toolBar.setBackgroundColor(ContextCompat.getColor(this@LastMinuteDealsActivity, R.color.brand_primary))
            window.statusBarColor = ContextCompat.getColor(this@LastMinuteDealsActivity, R.color.brand_primary_dark)
        }
    }

    private fun createServiceRequest(): LastMinuteDealsRequest {
        val tuid = Ui.getApplication(this).appComponent().userStateManager().userSource.tuid
        val request = LastMinuteDealsRequest(tuid.toString())
        val pos = PointOfSale.getPointOfSale()
        request.siteId = pos.tpid.toString()
        request.locale = pos.localeIdentifier.toString()
        return request
    }

    override fun onResume() {
        super.onResume()
        OmnitureTracking.trackLastMinuteDealsPageLoad()
    }
}
