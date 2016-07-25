package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.adapter.RailAmenitiesAndRulesAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.rail.RailAmenitiesViewModel

class RailAmenitiesFareRulesWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val viewPager: ViewPager by bindView(R.id.viewpager)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val tabs: TabLayout by bindView(R.id.tabs)
    val container: RelativeLayout by bindView(R.id.amenities_rules_container)

    lateinit var adapter: RailAmenitiesAndRulesAdapter

    init {
        View.inflate(context, R.layout.rail_amenities_rules_widget, this)
        setupTabs()
        adapter.amenitiesWidget.viewModel = RailAmenitiesViewModel()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupStatusBar(context)
    }

    private fun setupTabs() {
        adapter = RailAmenitiesAndRulesAdapter(context)
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupStatusBar(context: Context) {
        val statusBarHeight = Ui.getStatusBarHeight(context)

        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, container, color)
            addView(statusBar)
        }
    }
}