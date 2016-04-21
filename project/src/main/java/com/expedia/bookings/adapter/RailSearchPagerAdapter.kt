package com.expedia.bookings.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.presenter.rail.RailSearchWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailSearchViewModel

class RailSearchPagerAdapter(val context: Context) : PagerAdapter() {

    enum class Tab(val titleResourceId: Int) {
        SINGLE(R.string.rail_single),
        RETURN(R.string.rail_return),
        OPEN_RETURN(R.string.rail_open_return);
    }

    override fun getCount(): Int {
        return Tab.values().size
    }

    override fun isViewFromObject(p0: View?, p1: Any?): Boolean {
        return true
    }

    val searchWidget = RailSearchWidget(context)

    var searchViewModel by notNullAndObservable<RailSearchViewModel>() {
        searchWidget.searchViewModel = it
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        if (position == 0) {
            container?.addView(searchWidget)
            return searchWidget
        } else {
            val view = View(context);
            container?.addView(view)
            view.setOnTouchListener { view, motionEvent ->
                false
            }
            return view
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(Tab.values().get(position).titleResourceId)
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        // normally would want to remove the view from the container,
        // but since we're keeping the view on the screen the whole time, we're just going to no-op

    }
}