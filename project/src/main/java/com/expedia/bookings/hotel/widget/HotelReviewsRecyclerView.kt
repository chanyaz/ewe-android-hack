package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.widget.HotelReviewsLoadingWidget
import com.expedia.bookings.widget.HotelReviewsSummaryWidget
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelReviewRowViewModel
import com.expedia.vm.HotelReviewsSummaryWidgetViewModel
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class HotelReviewsRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    init {
        layoutManager = LinearLayoutManager(getContext())
        addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, resources.getDimensionPixelSize(R.dimen.hotel_review_divider_height), true))
    }

    class HotelReviewsRecyclerAdapter(val isBucketedToUseBoxRating: Boolean) : RecyclerView.Adapter<HotelReviewsViewHolder>() {
        val VIEW_TYPE_HEADER = 0
        val VIEW_TYPE_REVIEW = 1
        val VIEW_TYPE_LOADING = 2
        val VIEW_TYPE_HEADER_BOX_RATING = 3

        val loadMoreObservable = BehaviorSubject.create<Unit>()

        var moreReviewsAvailable = false

        val toggleReviewTranslationSubject = PublishSubject.create<String>()
        val translationUpdatedSubject = endlessObserver<String> { reviewId ->
            val reviewIndex = reviews.indexOfFirst { reviewInList -> reviewInList.reviewId == reviewId }
            if (reviewIndex >= 0) {
                notifyItemChanged(reviewIndex + getHeaderCount())
            }
        }
        var translationMap: HashMap<String, TranslatedReview>? = null
        var showSummary = true

        private var reviews: ArrayList<HotelReviewsResponse.Review> = arrayListOf()
        private var reviewsSummary: ReviewSummary = ReviewSummary()

        override fun getItemCount(): Int {
            // Summary and loading progress footer should count
            return reviews.size + getHeaderCount() + getFooterCount()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelReviewsViewHolder {
            val view: View
            when (viewType) {
                VIEW_TYPE_HEADER -> view = HotelReviewsSummaryWidget(parent.context)
                VIEW_TYPE_HEADER_BOX_RATING -> view = HotelReviewsSummaryBoxRatingWidget(parent.context, null)
                VIEW_TYPE_LOADING -> view = HotelReviewsLoadingWidget(parent.context)
                else -> view = HotelReviewRowView(parent.context)
            }
            return HotelReviewsViewHolder(view)
        }

        override fun onBindViewHolder(holder: HotelReviewsViewHolder, position: Int) {
            when (holder.itemView) {
                is HotelReviewsSummaryWidget -> {
                    val hotelReviewsSummaryViewModel = HotelReviewsSummaryWidgetViewModel(holder.itemView.context)
                    hotelReviewsSummaryViewModel.reviewsSummaryObserver.onNext(reviewsSummary)
                    holder.itemView.bindData(hotelReviewsSummaryViewModel)
                }
                is HotelReviewsSummaryBoxRatingWidget -> {
                    holder.itemView.viewModel.reviewsSummaryObserver.onNext(reviewsSummary)
                }
                is HotelReviewRowView -> {
                    val hotelReviewRowViewModel = HotelReviewRowViewModel(holder.itemView.context)
                    hotelReviewRowViewModel.reviewObserver.onNext(reviews[position - getHeaderCount()])
                    translationMap?.get(reviews[position - getHeaderCount()].reviewId)?.let { translatedReview ->
                        hotelReviewRowViewModel.translatedReviewObserver.onNext(translatedReview)
                    }
                    hotelReviewRowViewModel.toggleReviewTranslationObservable.subscribe(toggleReviewTranslationSubject)
                    holder.itemView.bindData(hotelReviewRowViewModel)
                }
                is HotelReviewsLoadingWidget -> loadMoreReviews()
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position == getHeaderPosition()) return if (isBucketedToUseBoxRating) VIEW_TYPE_HEADER_BOX_RATING else VIEW_TYPE_HEADER
            if (position >= reviews.size + getHeaderCount()) return VIEW_TYPE_LOADING
            return VIEW_TYPE_REVIEW
        }

        fun addReviews(addedReviews: List<HotelReviewsResponse.Review>) {
            reviews.addAll(addedReviews)
            notifyDataSetChanged()
        }

        fun clearReviews() {
            moreReviewsAvailable = false
            reviews.clear()
            notifyDataSetChanged()
        }

        fun updateSummary(reviewsSummary: ReviewSummary) {
            this.reviewsSummary = reviewsSummary
            notifyDataSetChanged()
        }

        private fun loadMoreReviews() {
            loadMoreObservable.onNext(Unit)
        }

        private fun getHeaderPosition() = if (showSummary) 0 else -1
        private fun getHeaderCount() = if (showSummary) 1 else 0
        private fun getFooterCount() = if (moreReviewsAvailable) 1 else 0
    }

    class HotelReviewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
