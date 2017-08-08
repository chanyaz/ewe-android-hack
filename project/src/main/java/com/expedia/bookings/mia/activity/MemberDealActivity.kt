package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.mia.MemberDealListAdapter
import com.expedia.bookings.mia.MemberDealResponseProvider
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.HotelNavUtils

class MemberDealActivity : AppCompatActivity() {

    private lateinit var memberDealResponseProvider: MemberDealResponseProvider
    private lateinit var adapter: MemberDealListAdapter
    val recyclerView: RecyclerView by lazy {
        findViewById(R.id.member_deal_recycler_view) as RecyclerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)

        val toolBar = findViewById(R.id.mod_search_toolbar) as Toolbar
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        val shopButton = findViewById(R.id.mod_shop_button) as Button
        shopButton.setOnClickListener { view ->
            HotelNavUtils.goToHotels(this, NavUtils.MEMBER_ONLY_DEAL_SEARCH)
            OmnitureTracking.trackMemberPricingShop()
        }
        adapter = MemberDealListAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        memberDealResponseProvider = MemberDealResponseProvider(Ui.getApplication(this).appComponent().smartOfferService())
        memberDealResponseProvider.memberDealResponseSubject.subscribe(adapter.resultSubject)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        memberDealResponseProvider.fetchDeals()
        OmnitureTracking.trackMemberPricingPageLoad()
    }

    override fun onStop() {
        super.onStop()
    }
}