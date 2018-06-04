package com.expedia.bookings.hotel.widget

import android.content.Context
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

    private val compositeDisposable = CompositeDisposable()
    val viewModel = HotelReviewSearchResultsViewModel(compositeDisposable)
    private val reviewsPageView: HotelReviewsPageView by bindView(R.id.hotel_reviews_search_results_list)

    init {
        View.inflate(context, R.layout.hotel_review_search_results_layout, this)
        reviewsPageView.viewModel = HotelReviewsPageViewModel()
        reviewsPageView.recyclerAdapter.showSummary = false

        viewModel.reviewsObservable.subscribe { searchResults ->
            reviewsPageView.viewModel.reviewsObserver.onNext(searchResults)
            reviewsPageView.recyclerAdapter.addReviews(searchResults)
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

    private fun clearReviewsList() {
        compositeDisposable.clear()
        reviewsPageView.recyclerAdapter.clearReviews()
        reviewsPageView.viewModel.hasReviews = false
    }
}
