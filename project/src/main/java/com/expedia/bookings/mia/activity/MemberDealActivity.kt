package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.mia.vm.SmartOfferViewModel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText

class MemberDealActivity : AppCompatActivity() {
    val dealsStatusView: TextView by lazy {
        findViewById(R.id.member_deals_status_view) as TextView
    }

    private lateinit var smartOfferViewModel: SmartOfferViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)

        val toolBar = findViewById(R.id.mod_search_toolbar) as Toolbar
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        smartOfferViewModel = SmartOfferViewModel(Ui.getApplication(this).appComponent().smartOfferService())
    }

    override fun onStart() {
        super.onStart()
        smartOfferViewModel.responseSubject.subscribeText(dealsStatusView)
    }

    override fun onResume() {
        super.onResume()
        smartOfferViewModel.fetchDeals()
    }

    override fun onStop() {
        super.onStop()
    }
}