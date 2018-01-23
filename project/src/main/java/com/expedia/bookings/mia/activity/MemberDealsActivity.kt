package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.mia.MemberDealListAdapter
import com.expedia.bookings.mia.MemberDealsResponseProvider
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.util.endlessObserver

class MemberDealsActivity : AppCompatActivity() {

    private lateinit var memberDealResponseProvider: MemberDealsResponseProvider
    private lateinit var adapter: MemberDealListAdapter
    val recyclerView by bindView<RecyclerView>(R.id.member_deal_recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)

        val toolBar = findViewById<Toolbar>(R.id.mod_search_toolbar)
        toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        adapter = MemberDealListAdapter(this, endlessObserver {
            HotelNavUtils.goToHotels(this@MemberDealsActivity, NavUtils.MEMBER_ONLY_DEAL_SEARCH)
            OmnitureTracking.trackMemberPricingShop()
        })

        if (isBrandColorEnabled(this@MemberDealsActivity)) {
            toolBar.setBackgroundColor(ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary))
            window.statusBarColor = ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary_dark)
        }

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
