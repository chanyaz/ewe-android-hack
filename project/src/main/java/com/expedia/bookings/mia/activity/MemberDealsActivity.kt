package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.mia.MemberDealListAdapter
import com.expedia.bookings.mia.MemberDealsResponseProvider
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils

class MemberDealsActivity : AppCompatActivity() {

    private lateinit var memberDealResponseProvider: MemberDealsResponseProvider
    private lateinit var adapter: MemberDealListAdapter
    val recyclerView by bindView<RecyclerView>(R.id.member_deal_recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)

        val toolBar = findViewById<Toolbar>(R.id.mod_search_toolbar)
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        val shopButton = findViewById<Button>(R.id.mod_shop_button)
        shopButton.setOnClickListener { view ->
            HotelNavUtils.goToHotels(this, NavUtils.MEMBER_ONLY_DEAL_SEARCH)
            OmnitureTracking.trackMemberPricingShop()
        }

        if (isBrandColorEnabled(this@MemberDealsActivity)) {
            toolBar.setBackgroundColor(ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary))
            shopButton.setBackgroundColor(ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary))
            window.statusBarColor = ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary_dark)
        }

        adapter = MemberDealListAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        memberDealResponseProvider = MemberDealsResponseProvider(Ui.getApplication(this).appComponent().smartOfferService())
        memberDealResponseProvider.dealsResponseSubject.subscribe(adapter.resultSubject)
    }

    override fun onResume() {
        super.onResume()
        memberDealResponseProvider.fetchDeals()
        OmnitureTracking.trackMemberPricingPageLoad()
    }
}