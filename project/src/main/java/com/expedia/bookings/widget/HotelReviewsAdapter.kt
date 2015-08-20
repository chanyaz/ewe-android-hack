package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.ReviewSort
import com.expedia.bookings.data.ReviewsResponse
import com.expedia.bookings.utils.UserReviewsUtils
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewRowViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.mobiata.android.BackgroundDownloader
import java.util.*

public class HotelReviewsAdapter(val context: Context, val viewPager: ViewPager, val vm: HotelReviewsAdapterViewModel) : PagerAdapter() {

    // Keeps track of the last position selected in the pager adapter.
    private var lastPosition = -1

    init {
        viewPager.setAdapter(this)
        vm.reviewsObservable.subscribe { reviewWrapper ->
            val linearLayout = viewPager.findViewWithTag(reviewWrapper.reviewSort) as TableLayout
            linearLayout.removeAllViewsInLayout()
            reviewWrapper.reviews?.forEachIndexed { index, review ->
                val hotelReviewRowViewModel = HotelReviewRowViewModel(context)
                val view = HotelReviewRowView(context, hotelReviewRowViewModel)
                hotelReviewRowViewModel.reviewObserver.onNext(review)
                linearLayout.addView(view)
            }
        }
    }

    fun getReviewSort(position: Int): ReviewSort {
        var reviewSort: ReviewSort
        when (position) {
            1 -> reviewSort = ReviewSort.HIGHEST_RATING_FIRST
            2 -> reviewSort = ReviewSort.LOWEST_RATING_FIRST
            else -> {
                reviewSort = ReviewSort.NEWEST_REVIEW_FIRST
            }
        }
        return reviewSort
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String = ""
        when (position) {
            0 -> title = context.getResources().getString(R.string.user_review_sort_button_recent)
            1 -> title = context.getResources().getString(R.string.user_review_sort_button_favorable)
            2 -> title = context.getResources().getString(R.string.user_review_sort_button_critical)
        }
        return title
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val hotelReviewsView = HotelReviewsPageView(context)
        val tableLayout: TableLayout = hotelReviewsView.reviewsTable
        tableLayout.setTag(getReviewSort(position))
        container?.addView(hotelReviewsView)
        return hotelReviewsView
    }

    override fun isViewFromObject(view: View?, `obj`: Any?): Boolean {
        return view == `obj`
    }

    override fun setPrimaryItem(container: ViewGroup?, position: Int, `obj`: Any?) {
        // To prevent continuous fetching of reviews, fetch them only on position change in pager.
        if (position != lastPosition) {
            lastPosition = position
            vm.reviewsObserver.onNext(getReviewSort(position))
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override public fun destroyItem(container: ViewGroup, position: Int, `obj`: Any) {
        container.removeView(`obj` as HotelReviewsPageView)
    }


}