package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.bindView
import com.expedia.vm.rail.RailAmenitiesViewModel
import com.expedia.vm.rail.RailFareRulesViewModel

class RailAmenitiesFareRulesWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val viewPager: ViewPager by bindView(R.id.amenities_rules_view_pager)
    val toolbar: Toolbar by bindView(R.id.amenities_rules_toolbar)
    val tabs: TabLayout by bindView(R.id.amenities_rules_tabs)

    lateinit var adapter: RailAmenitiesAndRulesAdapter

    private val amenitiesViewModel = RailAmenitiesViewModel()
    private val fareRulesViewModel = RailFareRulesViewModel(context)

    init {
        View.inflate(context, R.layout.rail_amenities_rules_widget, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
        setupTabs()
        adapter.amenitiesWidget.viewModel = amenitiesViewModel
        adapter.fareRulesWidget.viewModel = fareRulesViewModel
    }

    fun showAmenitiesForOffer(legOption: RailLegOption, railProduct: RailProduct) {
        updateAmenitiesData(legOption, railProduct)
        updateFareRulesData(railProduct)

        viewPager.currentItem = RailAmenitiesAndRulesAdapter.Tab.AMENITIES.ordinal
        updateToolbar(RailAmenitiesAndRulesAdapter.Tab.AMENITIES.ordinal)
    }

    fun showFareRulesForOffer(legOption: RailLegOption, railProduct: RailProduct) {
        updateAmenitiesData(legOption, railProduct)
        updateFareRulesData(railProduct)

        viewPager.currentItem = RailAmenitiesAndRulesAdapter.Tab.FARE_RULES.ordinal
        updateToolbar(RailAmenitiesAndRulesAdapter.Tab.FARE_RULES.ordinal)
    }

    private fun updateAmenitiesData(legOption: RailLegOption, railProduct: RailProduct) {
        amenitiesViewModel.legOptionObservable.onNext(legOption)
        amenitiesViewModel.railProductObservable.onNext(railProduct)
    }

    private fun updateFareRulesData(railProduct: RailProduct) {
        fareRulesViewModel.railProductObservable.onNext(railProduct)
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

    private fun updateToolbar(tab: Int) {
        if (tab == RailAmenitiesAndRulesAdapter.Tab.AMENITIES.ordinal) {
            toolbar.title = context.getString(R.string.amenities)
            RailTracking().trackRailAmenities()
        } else {
            toolbar.title = context.getString(R.string.fare_rules)
            RailTracking().trackRailFares()
        }
    }
}
