package com.expedia.bookings.widget

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeColorFilter
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeRating
import com.expedia.util.subscribeStarColor
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.mobiata.android.text.StrikethroughTagHandler
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.collections.firstOrNull
import kotlin.collections.indexOfFirst
import kotlin.properties.Delegates

class HotelMapCarouselAdapter(var hotels: List<Hotel>, val hotelSubject: PublishSubject<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSoldOut = endlessObserver<String> { soldOutHotelId ->
        hotels.firstOrNull { it.hotelId == soldOutHotelId }?.isSoldOut = true
        hotelListItemsMetadata.firstOrNull { it.hotelId == soldOutHotelId }?.hotelSoldOut?.onNext(true)
    }

    private data class HotelListItemMetadata(val hotelId: String, val hotelSoldOut: BehaviorSubject<Boolean>)
    private val hotelListItemsMetadata: MutableList<HotelListItemMetadata> = ArrayList()

    override fun getItemCount(): Int {
        return hotels.size
    }

    fun setItems(newHotels: List<Hotel>) {
        hotels = newHotels
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        val holder: HotelViewHolder = given as HotelViewHolder
        val viewModel = HotelViewModel(holder.itemView.context, hotels[position])
        holder.bind(viewModel)
        holder.itemView.setOnClickListener(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_marker_preview_cell, parent, false)
        val screen = Ui.getScreenSize(parent.context)
        var lp = view.findViewById(R.id.root).layoutParams
        lp.width = screen.x
        return HotelViewHolder(view as ViewGroup)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        val hotelItemIndex = hotelListItemsMetadata.indexOfFirst { it.hotelId == (holder as HotelViewHolder).hotelId }
        if (hotelItemIndex != -1) {
            hotelListItemsMetadata.removeAt(hotelItemIndex)
        }
        super.onViewRecycled(holder)
    }

    public inner class HotelViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val resources: Resources by lazy {
            itemView.resources
        }

        override fun onClick(view: View) {
            val hotel: Hotel = hotels[adapterPosition]
            hotelSubject.onNext(hotel)
            HotelV2Tracking().trackHotelV2CarouselClick()
        }

        var hotelId: String by Delegates.notNull()
        val hotelPreviewImage: ImageView by bindView(R.id.hotel_preview_image)
        val hotelPreviewText: TextView by bindView(R.id.hotel_preview_text)
        val hotelPricePerNight: TextView by bindView(R.id.hotel_price_per_night)
        val hotelSoldOut: TextView by bindView(R.id.hotel_sold_out)
        val hotelStrikeThroughPrice: TextView by bindView(R.id.hotel_strike_through_price)
        val hotelGuestRating: TextView by bindView(R.id.hotel_guest_rating)
        val hotelGuestRecommend: TextView by bindView(R.id.hotel_guest_recommend)
        val hotelNoGuestRating: TextView by bindView(R.id.no_guest_rating)
        var hotelPreviewRating: StarRatingBar by Delegates.notNull()

        init {
            hotelPreviewRating = root.findViewById(if (shouldShowCircleForRatings()) R.id.hotel_preview_circle_rating else R.id.hotel_preview_star_rating) as StarRatingBar
            hotelPreviewRating.visibility = View.VISIBLE
        }

        fun bind(viewModel: HotelViewModel) {
            hotelId = viewModel.hotelId
            hotelListItemsMetadata.add(HotelListItemMetadata(viewModel.hotelId, viewModel.soldOut))

            viewModel.hotelLargeThumbnailUrlObservable.subscribe {
                PicassoHelper.Builder(hotelPreviewImage)
                        .setError(R.drawable.room_fallback)
                        .build()
                        .load(it)
            }

            viewModel.hotelNameObservable.subscribeText(hotelPreviewText)
            viewModel.hotelPreviewRatingVisibility.subscribeVisibility(hotelPreviewRating)
            viewModel.hotelPreviewRating.subscribeRating(hotelPreviewRating)
            viewModel.toolBarRatingColor.subscribeStarColor(hotelPreviewRating)
            viewModel.imageColorFilter.subscribeColorFilter(hotelPreviewImage)
            viewModel.hotelStrikeThroughPriceVisibility.subscribeVisibility(hotelStrikeThroughPrice)
            viewModel.hotelPriceFormatted.subscribeText(hotelPricePerNight)
            viewModel.hotelStrikeThroughPriceFormatted.subscribeText(hotelStrikeThroughPrice)
            viewModel.hotelGuestRatingObservable.subscribe { hotelGuestRating.text = it.toString() }
            viewModel.soldOut.subscribeVisibility(hotelSoldOut)
            viewModel.soldOut.subscribeInverseVisibility(hotelPricePerNight)

            viewModel.isHotelGuestRatingAvailableObservable.subscribeVisibility(hotelGuestRating)
            viewModel.isHotelGuestRatingAvailableObservable.subscribeVisibility(hotelGuestRecommend)
            viewModel.isHotelGuestRatingAvailableObservable.map { !it }.subscribeVisibility(hotelNoGuestRating)

            hotelPreviewText.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
            hotelPricePerNight.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD)
            hotelStrikeThroughPrice.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
            hotelGuestRating.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
            hotelGuestRecommend.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        }
    }
}

fun priceFormatter(resources: Resources, rate: HotelRate?, strikeThrough: Boolean): CharSequence {
    if (rate == null) return ""
    var hotelPrice = HotelRate.getDisplayMoney(rate, strikeThrough).getFormattedMoney(Money.F_NO_DECIMAL)

    return if (strikeThrough) Html.fromHtml(resources.getString(R.string.strike_template, hotelPrice), null, StrikethroughTagHandler()) else hotelPrice
}
