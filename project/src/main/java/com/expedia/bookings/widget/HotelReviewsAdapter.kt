package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewsPageViewModel

class HotelReviewsAdapter(val context: Context, val viewPager: ViewPager, val vm: HotelReviewsAdapterViewModel) : PagerAdapter() {

    init {
        viewPager.adapter = this

        vm.reviewsSummaryObservable.subscribe { reviewsSummary ->
            for (reviewSort: ReviewSort in ReviewSort.values()) {
                val hotelReviewsView = viewPager.findViewWithTag(reviewSort) as HotelReviewsPageView
                hotelReviewsView.recyclerAdapter.updateSummary(reviewsSummary)
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
        hotelReviewsView.viewModel.reviewsObserver.onNext(reviews)
        hotelReviewsView.recyclerAdapter.addReviews(reviews);
    }

    fun getReviewSort(position: Int): ReviewSort {
        return when (position) {
            1 -> ReviewSort.HIGHEST_RATING_FIRST
            2 -> ReviewSort.LOWEST_RATING_FIRST
            else -> ReviewSort.NEWEST_REVIEW_FIRST
        }
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
        val hotelReviewsView = HotelReviewsPageView(context)
        hotelReviewsView.viewModel = HotelReviewsPageViewModel()
        hotelReviewsView.recyclerAdapter.loadMoreObservable.subscribe {
            vm.reviewsObserver.onNext(getReviewSort(position))
        }
        val sort = getReviewSort(position)
        hotelReviewsView.tag = sort
        container.addView(hotelReviewsView)
        return hotelReviewsView
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

    fun startDownloads() {
        vm.reviewsObserver.onNext(ReviewSort.HIGHEST_RATING_FIRST)
        vm.reviewsObserver.onNext(ReviewSort.LOWEST_RATING_FIRST)
        vm.reviewsObserver.onNext(ReviewSort.NEWEST_REVIEW_FIRST)
    }
}
