package com.expedia.bookings.widget

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setTextAndVisibility
import com.expedia.bookings.hotel.util.shouldShowCircleForRatings
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.HotelViewModel
import com.mobiata.android.text.StrikethroughTagHandler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import kotlin.properties.Delegates

class HotelMapCarouselAdapter(var hotels: List<Hotel>, val hotelSubject: PublishSubject<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSoldOut = endlessObserver<String> { soldOutHotelId ->
        hotels.firstOrNull { it.hotelId == soldOutHotelId }?.isSoldOut = true
        hotelListItemsMetadata.firstOrNull { it.hotelId == soldOutHotelId }?.hotelSoldOut?.onNext(true)
    }

    var shopWithPoints: Boolean = false

    var lineOfBusiness = LineOfBusiness.HOTELS

    private data class HotelListItemMetadata(val hotelId: String, val hotelSoldOut: BehaviorSubject<Boolean>)

    private val hotelListItemsMetadata: MutableList<HotelListItemMetadata> = ArrayList()

    override fun getItemCount(): Int {
        return hotels.size
    }

    fun setItems(newHotels: List<Hotel>) {
        hotels = newHotels
        notifyDataSetChanged()
    }

    fun setLob(lob: LineOfBusiness) {
        lineOfBusiness = lob
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        val holder: HotelViewHolder = given as HotelViewHolder
        val viewModel = HotelViewModel(holder.itemView.context)
        viewModel.bindHotelData(hotels[position])
        holder.bind(viewModel)
        holder.itemView.setOnClickListener(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_marker_preview_cell, parent, false)
        val screen = Ui.getScreenSize(parent.context)
        val lp = view.findViewById<View>(R.id.root).layoutParams
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

    inner class HotelViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val resources: Resources by lazy {
            itemView.resources
        }

        override fun onClick(view: View) {
            val hotel: Hotel = hotels[adapterPosition]
            hotelSubject.onNext(hotel)
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
        val loyaltyAppliedMessageContainer: LinearLayout by bindView(R.id.map_loyalty_message_container)
        val loyaltyAppliedMessage: TextView by bindView(R.id.map_loyalty_applied_message)
        val shadowOnLoyaltyMessageContainer: View by bindView(R.id.shadow_on_loyalty_message_container)
        val shadowOnHotelCell: View by bindView(R.id.shadow_on_hotel_preview_cell)
        val loyaltyEarnMessage: TextView by bindView(R.id.hotel_loyalty_earn_message)

        var hotelPreviewRating: StarRatingBar by Delegates.notNull()

        init {
            hotelPreviewRating = root.findViewById<StarRatingBar>(if (shouldShowCircleForRatings()) R.id.hotel_preview_circle_rating else R.id.hotel_preview_star_rating)
            hotelPreviewRating.visibility = View.VISIBLE
        }

        fun bind(viewModel: HotelViewModel) {
            hotelId = viewModel.hotelId
            hotelListItemsMetadata.add(HotelListItemMetadata(viewModel.hotelId, viewModel.soldOut))

            val url = viewModel.getHotelLargeThumbnailUrl()
            if (url.isNotBlank()) {
                PicassoHelper.Builder(hotelPreviewImage)
                        .setError(R.drawable.room_fallback)
                        .build()
                        .load(url)
            }

            hotelPreviewText.text = viewModel.hotelName
            hotelPreviewImage.colorFilter = viewModel.getImageColorFilter()

            updateHotelRating(viewModel)
            updatePricing(viewModel)

            loyaltyAppliedMessageContainer.visibility =
                    if (viewModel.loyaltyAvailable) View.VISIBLE
                    else if (shopWithPoints) View.INVISIBLE
                    else View.GONE

            shadowOnLoyaltyMessageContainer.setVisibility(viewModel.loyaltyAvailable)
            shadowOnHotelCell.setVisibility(!viewModel.loyaltyAvailable && lineOfBusiness == LineOfBusiness.HOTELS)
            loyaltyAppliedMessage.text = viewModel.getMapLoyaltyMessageText()
            loyaltyEarnMessage.text = viewModel.earnMessage
            loyaltyEarnMessage.setVisibility(viewModel.showEarnMessage)

            updateFonts()
        }

        private fun updatePricing(viewModel: HotelViewModel) {
            hotelStrikeThroughPrice.setTextAndVisibility(viewModel.hotelStrikeThroughPriceFormatted)
            hotelPricePerNight.text = viewModel.hotelPriceFormatted
            hotelPricePerNight.setTextColor(viewModel.pricePerNightColor)
            hotelPricePerNight.setInverseVisibility(viewModel.isHotelSoldOut)
            hotelSoldOut.setVisibility(viewModel.isHotelSoldOut)
        }

        private fun updateFonts() {
            hotelPreviewText.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
            hotelPricePerNight.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD)
            hotelStrikeThroughPrice.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
            hotelGuestRating.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
            hotelGuestRecommend.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        }

        private fun updateHotelRating(viewModel: HotelViewModel) {
            hotelPreviewRating.setStarColor(viewModel.getStarRatingColor())
            hotelPreviewRating.setRating(viewModel.hotelStarRating)
            hotelPreviewRating.setVisibility(viewModel.showHotelPreviewRating)

            hotelGuestRating.text = viewModel.hotelGuestRating.toString()
            hotelGuestRating.setVisibility(viewModel.isHotelGuestRatingAvailable)
            hotelGuestRecommend.setVisibility(viewModel.isHotelGuestRatingAvailable)
            hotelNoGuestRating.setVisibility(viewModel.showNoGuestRating)
        }
    }
}

fun priceFormatter(resources: Resources, rate: HotelRate?, strikeThrough: Boolean, shouldFallbackToZeroIfNegative: Boolean): CharSequence {

    if (rate == null) {
        return ""
    } else if (strikeThrough && !rate.isStrikeThroughPriceValid) { // #6801 - strikethrough price now optional from API
        return ""
    } else {
        val hotelPrice = rate.getDisplayMoney(strikeThrough, shouldFallbackToZeroIfNegative).getFormattedMoney(Money.F_NO_DECIMAL)
        return if (strikeThrough) HtmlCompat.fromHtml(resources.getString(R.string.strike_template, hotelPrice), null, StrikethroughTagHandler()) else hotelPrice
    }
}
