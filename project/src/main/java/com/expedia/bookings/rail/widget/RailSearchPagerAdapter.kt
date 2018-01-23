package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class RailSearchPagerAdapter(val context: Context) : PagerAdapter() {

    enum class Tab(val titleResourceId: Int) {
        ONE_WAY(R.string.rail_one_way),
        RETURN(R.string.rail_return),
    }

    override fun getCount(): Int {
        return Tab.values().size
    }

    override fun isViewFromObject(p0: View?, p1: Any?): Boolean {
        return true
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        // create some mock views to listen for swipes.
        // We're actually not going to show anything here
        return createMockView(container)
    }

    private fun createMockView(container: ViewGroup): View {
        val view = View(context)
        container.addView(view)
        return view
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(Tab.values()[position].titleResourceId)
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        // normally would want to remove the view from the container,
        // but since we're keeping the view on the screen the whole time, we're just going to no-op
    }
}
