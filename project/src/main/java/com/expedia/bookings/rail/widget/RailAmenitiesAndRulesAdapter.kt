package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class RailAmenitiesAndRulesAdapter(val context: Context) : PagerAdapter() {

    enum class Tab(val titleResourceId: Int) {
        AMENITIES(R.string.amenities),
        FARE_RULES(R.string.fare_rules)
    }

    val amenitiesWidget = RailAmenitiesWidget(context)
    val fareRulesWidget = RailFareRulesWidget(context)

    override fun getCount(): Int {
        return Tab.values().size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(Tab.values()[position].titleResourceId)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (position == 0) {
            container.addView(amenitiesWidget)
            return amenitiesWidget
        } else {
            container.addView(fareRulesWidget)
            return fareRulesWidget
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }
}
