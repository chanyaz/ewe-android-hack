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
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewRowViewModel
import com.expedia.vm.HotelReviewsSummaryViewModel

public class HotelReviewsAdapter(val context: Context, val viewPager: ViewPager, val vm: HotelReviewsAdapterViewModel) : PagerAdapter() {

    init {
        viewPager.setAdapter(this)

        vm.reviewsSummaryObservable.subscribe { reviewsSummary ->
            val hotelReviewsSummaryViewModel = HotelReviewsSummaryViewModel(context)
            hotelReviewsSummaryViewModel.reviewsSummaryObserver.onNext(reviewsSummary)
            for (reviewSort: ReviewSort in ReviewSort.values()) {
                val hotelReviewsView = viewPager.findViewWithTag(reviewSort) as HotelReviewsPageView
                hotelReviewsView.summaryContainer.addView(HotelReviewsSummaryWidget(context, hotelReviewsSummaryViewModel))
            }
        }

        vm.favorableReviewsObservable.subscribe { reviews ->
            addReviews(ReviewSort.HIGHEST_RATING_FIRST, reviews)
        }

        vm.criticalReviewsObservable.subscribe { reviews ->
            addReviews(ReviewSort.LOWEST_RATING_FIRST, reviews)
        }

        vm.newestReviewsObservable.subscribe { reviews ->
            addReviews(ReviewSort.NEWEST_REVIEW_FIRST, reviews)
        }

    }

    private fun addReviews(reviewSort: ReviewSort, reviews: List<Review>) {
        val hotelReviewsView = viewPager.findViewWithTag(reviewSort) as HotelReviewsPageView
        val reviewsTable = hotelReviewsView.reviewsTable
        reviews.forEachIndexed { index, review ->
            val hotelReviewRowViewModel = HotelReviewRowViewModel(context)
            val view = HotelReviewRowView(context, hotelReviewRowViewModel)
            hotelReviewRowViewModel.reviewObserver.onNext(review)
            reviewsTable.addView(view)
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
        hotelReviewsView.setTag(getReviewSort(position))
        container?.addView(hotelReviewsView)
        vm.reviewsObserver.onNext(getReviewSort(position))
        return hotelReviewsView
    }

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return 3
    }

    override public fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as HotelReviewsPageView)
    }


}