package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.widget.HotelReviewsPageView
import com.expedia.vm.HotelReviewsAdapterViewModel

class HotelReviewsAdapter(val context: Context, viewPager: ViewPager, val vm: HotelReviewsAdapterViewModel) : PagerAdapter() {

    init {
        viewPager.adapter = this
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.resources.getString(R.string.user_review_sort_button_recent)
            1 -> context.resources.getString(R.string.user_review_sort_button_favorable)
            2 -> context.resources.getString(R.string.user_review_sort_button_critical)
            else -> ""
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        val view = HotelReviewsPageView(context, getReviewSort(position), vm)
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return ReviewSort.values().size
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as HotelReviewsPageView)
    }

    private fun getReviewSort(position: Int): ReviewSort {
        return when (position) {
            1 -> ReviewSort.HIGHEST_RATING_FIRST
            2 -> ReviewSort.LOWEST_RATING_FIRST
            else -> ReviewSort.NEWEST_REVIEW_FIRST
        }
    }
}
