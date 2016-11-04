package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.adapter.RailAmenitiesAndRulesAdapter
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.utils.bindView
import com.expedia.vm.rail.RailAmenitiesViewModel
import com.expedia.vm.rail.RailFareRulesViewModel

class RailAmenitiesFareRulesWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val viewPager: ViewPager by bindView(R.id.amenities_rules_view_pager)
    val toolbar: Toolbar by bindView(R.id.amenities_rules_toolbar)
    val tabs: TabLayout by bindView(R.id.amenities_rules_tabs)

    lateinit var adapter: RailAmenitiesAndRulesAdapter

    init {
        View.inflate(context, R.layout.rail_amenities_rules_widget, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
        setupTabs()
        adapter.amenitiesWidget.viewModel = RailAmenitiesViewModel()
        adapter.fareRulesWidget.viewModel = RailFareRulesViewModel(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun showAmenitiesForOffer(offer: RailOffer) {
        updateOffer(offer)
        viewPager.currentItem = RailAmenitiesAndRulesAdapter.Tab.AMENITIES.ordinal
        updateToolbar(RailAmenitiesAndRulesAdapter.Tab.AMENITIES.ordinal)
    }

    fun showFareRulesForOffer(offer: RailOffer) {
        updateOffer(offer)
        viewPager.currentItem = RailAmenitiesAndRulesAdapter.Tab.FARE_RULES.ordinal
        updateToolbar(RailAmenitiesAndRulesAdapter.Tab.FARE_RULES.ordinal)
    }

    private fun setupTabs() {
        adapter = RailAmenitiesAndRulesAdapter(context)
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                updateToolbar(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
    }

    private fun updateOffer(offer: RailOffer) {
        adapter.amenitiesWidget.viewModel.offerObservable.onNext(offer)
        adapter.fareRulesWidget.viewModel.offerObservable.onNext(offer)
    }

    private fun updateToolbar(tab: Int) {
        if (tab == RailAmenitiesAndRulesAdapter.Tab.AMENITIES.ordinal) {
            toolbar.title = context.getString(R.string.amenities)
        } else {
            toolbar.title = context.getString(R.string.fare_rules)
        }
    }

}