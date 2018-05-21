package com.expedia.bookings.mia.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.mia.MemberDealListAdapter
import com.expedia.bookings.mia.arch.MemberDealsArchViewModel
import com.expedia.bookings.mia.vm.DealsErrorViewModel
import com.expedia.bookings.presenter.DealsErrorPresenter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.util.endlessObserver

open class MemberDealsActivity : AppCompatActivity() {

    val errorPresenter: DealsErrorPresenter by bindView(R.id.deals_error)
    private val toolBar by bindView<Toolbar>(R.id.mod_search_toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.member_deal_recycler_view)

    protected open val viewModel: MemberDealsArchViewModel by lazy {
        val factory = MemberDealsArchViewModel
                .Factory(Ui.getApplication(this).appComponent().smartOfferService(), createServiceRequest())
        ViewModelProviders.of(this, factory).get(MemberDealsArchViewModel::class.java)
    }
    protected val adapter: MemberDealListAdapter by lazy {
        MemberDealListAdapter(this, endlessObserver {
            HotelNavUtils.goToHotels(this@MemberDealsActivity, NavUtils.DEAL_SEARCH)
            OmnitureTracking.trackMemberPricingShop()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)
        setupToolbar()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupErrorPresenter()
        setupDealResponseObservers()
    }

    private fun setupDealResponseObservers() {
        viewModel.responseLiveData.observe(this, adapter.responseObserver)
        viewModel.responseLiveData.observe(this, LiveDataObserver { response ->
            if (response?.destinations?.isEmpty() == true) {
                errorPresenter.visibility = View.VISIBLE
            }
        })
    }

    private fun setupErrorPresenter() {
        errorPresenter.viewmodel = DealsErrorViewModel(this)
        errorPresenter.viewmodel.showLaunchScreen.subscribe {
            NavUtils.goToLaunchScreen(this)
            errorPresenter.viewmodel.getButtonActionSubscription()?.dispose()
        }
    }

    private fun createServiceRequest(): MemberDealsRequest {
        val request = MemberDealsRequest()
        val pos = PointOfSale.getPointOfSale()
        request.siteId = pos.tpid.toString()
        request.locale = pos.localeIdentifier.toString()
        return request
    }

    private fun setupToolbar() {
        toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
        if (isBrandColorEnabled(this@MemberDealsActivity)) {
            toolBar.setBackgroundColor(ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary))
            window.statusBarColor = ContextCompat.getColor(this@MemberDealsActivity, R.color.brand_primary_dark)
        }
    }

    override fun onResume() {
        super.onResume()
        OmnitureTracking.trackMemberPricingPageLoad()
    }
}
