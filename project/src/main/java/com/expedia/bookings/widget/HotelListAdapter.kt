package com.expedia.bookings.widget

import android.content.res.Resources
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribe
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

public class HotelListAdapter(var hotels: List<Hotel>, val hotelSubject: PublishSubject<Hotel>, val headerSubject: PublishSubject<Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val HEADER_VIEW = 0
    val HOTEL_VIEW = 1
    val LOADING_VIEW = 2

    var loadingState: Boolean = false

    fun setData(data: List<Hotel>) {
        hotels = data
    }

    override fun getItemCount(): Int {
        return hotels.size()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return HEADER_VIEW
        } else if (loadingState) {
            return LOADING_VIEW
        } else {
            return HOTEL_VIEW
        }
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder, position: Int) {
        if (given.getItemViewType() == HEADER_VIEW) {
            val holder: HeaderViewHolder = given as HeaderViewHolder
            holder.itemView.setOnClickListener(holder)
        } else if (given.getItemViewType() == HOTEL_VIEW) {
            val holder: HotelViewHolder = given as HotelViewHolder
            val viewModel = HotelViewModel(hotels.get(position), holder.resources)
            holder.bind(viewModel)
            holder.itemView.setOnClickListener(holder)
        } else if (given.getItemViewType() == LOADING_VIEW) {
            val holder: LoadingViewHolder = given as LoadingViewHolder
            val animation = AnimUtils.setupLoadingAnimation(holder.backgroundImageView, position % 2 == 0)
            holder.setAnimator(animation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == HEADER_VIEW) {
            val header = View(parent.getContext())

            var lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.height = if (ExpediaBookingApp.isAutomation()) 0 else parent.getHeight()
            header.setLayoutParams(lp)

            return HeaderViewHolder(header)
        } else if (viewType == LOADING_VIEW) {
            val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_loading_cell, parent, false)
            return LoadingViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_cell, parent, false)
            return HotelViewHolder(view as ViewGroup, parent.getWidth())
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.getItemViewType() == LOADING_VIEW) {
            (holder as LoadingViewHolder).cancelAnimation()
        }
        super.onViewRecycled(holder)
    }

    public inner class HotelViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), HeaderBitmapDrawable.CallbackListener, View.OnClickListener {

        val PICASSO_TAG = "HOTEL_RESULTS_LIST"

        val resources: Resources by Delegates.lazy {
            itemView.getResources()
        }

        val imageView: ImageView by root.bindView(R.id.background)
        val hotelName: TextView by root.bindView(R.id.hotel_name_text_view)
        val pricePerNight: TextView by root.bindView(R.id.price_per_night)
        val strikeThroughPricePerNight: TextView by root.bindView(R.id.strike_through_price)
        val guestRatingPercentage: TextView by root.bindView(R.id.guest_rating_percentage)
        val topAmenityTitle: TextView by root.bindView(R.id.top_amenity_title)
        val starRating: RatingBar by root.bindView(R.id.hotel_rating_bar)

        public fun bind(viewModel: HotelViewModel) {
            viewModel.hotelLargeThumbnailUrlObservable.subscribe { url ->
                val drawable = Images.makeHotelBitmapDrawable(itemView.getContext(), this, width, url, PICASSO_TAG)
                imageView.setImageDrawable(drawable)
            }

            viewModel.hotelNameObservable.subscribe(hotelName)
            viewModel.pricePerNightObservable.subscribe(pricePerNight)
            viewModel.guestRatingPercentageObservable.subscribe(guestRatingPercentage)
            viewModel.topAmenityTitleObservable.subscribe(topAmenityTitle)

            strikeThroughPricePerNight.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)

            viewModel.hotelStarRatingObservable.subscribe {
                starRating.setRating(it)
            }
        }

        override fun onClick(view: View) {
            val hotel: Hotel = hotels.get(getAdapterPosition())
            hotelSubject.onNext(hotel)
        }

        override fun onBitmapLoaded() {
            // ignore
        }

        override fun onBitmapFailed() {
            // ignore
        }

        override fun onPrepareLoad() {
            // ignore
        }
    }

    public inner class HeaderViewHolder(root: View) : RecyclerView.ViewHolder(root), View.OnClickListener {

        override fun onClick(view: View) {
            if (getItemViewType() == HEADER_VIEW) {
                headerSubject.onNext(Unit)
            }
        }
    }
}
