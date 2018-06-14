package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.hotel.vm.HotelReviewSearchResultsViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelReviewsPageView
import com.expedia.vm.HotelReviewsPageViewModel
import io.reactivex.disposables.CompositeDisposable

class HotelReviewSearchResultsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    @VisibleForTesting
    val compositeDisposable = CompositeDisposable()
    @VisibleForTesting
    val viewModel = HotelReviewSearchResultsViewModel(compositeDisposable)
    @VisibleForTesting
    val reviewsPageView: HotelReviewsPageView by bindView(R.id.hotel_reviews_search_results_list)
    @VisibleForTesting
    val reviewsEmptyContainer: LinearLayout by bindView(R.id.hotel_reviews_search_empty_container)

    init {
        View.inflate(context, R.layout.hotel_review_search_results_layout, this)
        reviewsPageView.viewModel = HotelReviewsPageViewModel()
        reviewsPageView.recyclerAdapter.showSummary = false

        viewModel.reviewsObservable.subscribe { searchResults ->
            reviewsPageView.viewModel.reviewsObserver.onNext(searchResults)
            reviewsPageView.recyclerAdapter.addReviews(searchResults)
        }

        reviewsPageView.recyclerAdapter.loadMoreObservable.subscribe {
            viewModel.getNextPage()
        }

        viewModel.noReviewsObservable.subscribe {
            reviewsEmptyContainer.setVisibility(true)
            reviewsPageView.setVisibility(false)
        }
    }

    fun doSearch(query: String?, hotelId: String?) {
        clearReviewsList()
        viewModel.doSearch(query, hotelId)
        reviewsPageView.startLoadingAnimation()
        reviewsPageView.setVisibility(true)
    }

    fun onCollapse() {
        reviewsPageView.setVisibility(false)
        clearReviewsList()
    }

    fun clearReviewsList() {
        compositeDisposable.clear()
        reviewsPageView.recyclerAdapter.clearReviews()
        reviewsEmptyContainer.setVisibility(false)
        reviewsPageView.setVisibility(false)
        reviewsPageView.viewModel.hasReviews = false
    }
}
