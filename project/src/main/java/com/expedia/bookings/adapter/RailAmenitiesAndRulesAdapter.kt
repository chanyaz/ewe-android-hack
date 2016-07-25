package com.expedia.bookings.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.widget.rail.RailAmenitiesWidget

class RailAmenitiesAndRulesAdapter(val context: Context) : PagerAdapter() {

    enum class Tab(val titleResourceId: Int) {
        AMENITIES(R.string.amenities),
        FARE_RULES(R.string.fare_rules)
    }

    val amenitiesWidget = RailAmenitiesWidget(context)

    override fun getCount(): Int {
        return Tab.values().size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(Tab.values().get(position).titleResourceId)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        if (position == 0) {
            container.addView(amenitiesWidget)
            return amenitiesWidget
        } else  {
            //TODO returns fare rules...
            val view = View(context);
            container.addView(view)

            return view
        }
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        super.destroyItem(container, position, `object`)
    }
}
