package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.vm.HotelReviewRowViewModel
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewsPageViewModel
import com.expedia.vm.HotelReviewsSummaryViewModel

public class HotelReviewsAdapter(val context: Context, val viewPager: ViewPager, val vm: HotelReviewsAdapterViewModel) : PagerAdapter() {

    init {
        viewPager.adapter = this

        vm.reviewsSummaryObservable.subscribe { reviewsSummary ->
            val hotelReviewsSummaryViewModel = HotelReviewsSummaryViewModel(context)
            hotelReviewsSummaryViewModel.reviewsSummaryObserver.onNext(reviewsSummary)
            for (reviewSort: ReviewSort in ReviewSort.values) {
                val hotelReviewsView = viewPager.findViewWithTag(reviewSort) as HotelReviewsPageView
                if (hotelReviewsView.summaryContainer.childCount == 0) {
                    hotelReviewsView.summaryContainer.addView(HotelReviewsSummaryWidget(context, hotelReviewsSummaryViewModel))
                }
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
        val reviewsTable = hotelReviewsView.reviewsTable
        reviews.forEachIndexed { index, review ->
            val hotelReviewRowViewModel = HotelReviewRowViewModel(context)
            val view = HotelReviewRowView(context, hotelReviewRowViewModel)
            hotelReviewRowViewModel.reviewObserver.onNext(review)
            reviewsTable.addView(view)
        }
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
        hotelReviewsView.reviewsScrollviewContainer.addOnScrollListener { scrollView, x1, y1, x2, y2 ->
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = (view.bottom - (scrollView.height + scrollView.scrollY));
            if (diff == 0) {
                vm.reviewsObserver.onNext(getReviewSort(position))
            }
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
        return ReviewSort.values.size
    }

    override public fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as HotelReviewsPageView)
    }

    fun startDownloads() {
        vm.reviewsObserver.onNext(ReviewSort.HIGHEST_RATING_FIRST)
        vm.reviewsObserver.onNext(ReviewSort.LOWEST_RATING_FIRST)
        vm.reviewsObserver.onNext(ReviewSort.NEWEST_REVIEW_FIRST)
    }
}
