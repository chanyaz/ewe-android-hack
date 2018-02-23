package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.widget.HotelReviewsLoadingWidget
import com.expedia.bookings.widget.HotelReviewsSummaryWidget
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.vm.HotelReviewRowViewModel
import com.expedia.vm.HotelReviewsSummaryViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
import java.util.Locale

class HotelReviewsRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    init {
        layoutManager = LinearLayoutManager(getContext())
        addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, resources.getDimensionPixelSize(R.dimen.hotel_review_divider_height), true))
    }

    class HotelReviewsRecyclerAdapter : RecyclerView.Adapter<HotelReviewsViewHolder>() {
        val VIEW_TYPE_HEADER = 0
        val VIEW_TYPE_REVIEW = 1
        val VIEW_TYPE_LOADING = 2

        val loadMoreObservable = BehaviorSubject.create<Unit>()

        var moreReviewsAvailable = false

        private var reviews: ArrayList<HotelReviewsResponse.Review> = arrayListOf()
        private var reviewsSummary: HotelReviewsResponse.ReviewSummary = HotelReviewsResponse.ReviewSummary()

        override fun getItemCount(): Int {
            // Summary and loading progress footer should count
            return reviews.size + 1 + if (moreReviewsAvailable) 1 else 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelReviewsViewHolder {
            val view: View
            when (viewType) {
                VIEW_TYPE_HEADER -> view = HotelReviewsSummaryWidget(parent.context)
                VIEW_TYPE_LOADING -> view = HotelReviewsLoadingWidget(parent.context)
                else -> view = HotelReviewRowView(parent.context)
            }
            return HotelReviewsViewHolder(view)
        }
        private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val clientBuilder = OkHttpClient.Builder().addInterceptor(logger)
        private val translateApi = Retrofit.Builder()
                .baseUrl("https://translation.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build()
                .create(TranslationApi::class.java)

        private val apiKey = <your_api_key_here>

        override fun onBindViewHolder(holder: HotelReviewsViewHolder, position: Int) {
            when (holder.itemView) {
                is HotelReviewsSummaryWidget -> {
                    val hotelReviewsSummaryViewModel = HotelReviewsSummaryViewModel(holder.itemView.context)
                    hotelReviewsSummaryViewModel.reviewsSummaryObserver.onNext(reviewsSummary)
                    holder.itemView.bindData(hotelReviewsSummaryViewModel)
                }
                is HotelReviewRowView -> {
                    val hotelReviewRowViewModel = HotelReviewRowViewModel(holder.itemView.context)
                    hotelReviewRowViewModel.reviewObserver.onNext(reviews[position - 1])
                    val deviceLocale = Locale.getDefault()
                    val reviewLocale = Locale(reviews[position - 1].contentLocale)
                    if (reviewLocale.language != deviceLocale.language) {
                        val review = reviews[position - 1]
                        translateApi.translateToTargetLanguage(deviceLocale.language, review.reviewText, apiKey)
                                .subscribeOn(Schedulers.io())
                                .map { it.data.translations[0].translatedText }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeObserver(hotelReviewRowViewModel.translationObservable)
                    }
                    holder.itemView.bindData(hotelReviewRowViewModel)
                }
                is HotelReviewsLoadingWidget -> loadMoreReviews()
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) return VIEW_TYPE_HEADER
            if (position >= reviews.size + 1) return VIEW_TYPE_LOADING
            return VIEW_TYPE_REVIEW
        }

        fun addReviews(addedReviews: List<HotelReviewsResponse.Review>) {
            reviews.addAll(addedReviews)
            notifyDataSetChanged()
        }

        fun updateSummary(reviewsSummary: HotelReviewsResponse.ReviewSummary) {
            this.reviewsSummary = reviewsSummary
            notifyDataSetChanged()
        }

        private fun loadMoreReviews() {
            loadMoreObservable.onNext(Unit)
        }
    }

    class HotelReviewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
