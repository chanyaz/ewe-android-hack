package com.expedia.bookings.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.otto.Events
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.bindView
import com.mobiata.android.text.StrikethroughTagHandler
import rx.subjects.PublishSubject

/**
 * A Viewholder for the case where our data are hotels.
 */
class HotelViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, HeaderBitmapDrawable.PicassoTargetListener {
    private val green: Int
    private val orange: Int
    private val purple: Int
    private val blue: Int
    private val mobileOnly: Drawable
    private val tonightOnly: Drawable
    private var hotelSelectedSubject: PublishSubject<Hotel>? = null
    private val FULL_TILE_TEXT_SIZE = 18
    private val HALF_TILE_TEXT_SIZE = 15

    val gradient: View by bindView(R.id.gradient)

    val cardView: CardView by bindView(R.id.card_view)
    val title: TextView by bindView(R.id.title)
    val subtitle: TextView by bindView(R.id.subtitle)
    val ratingInfo: View by bindView(R.id.rating_info)
    val rating: TextView by bindView(R.id.rating)
    val ratingText: TextView by bindView(R.id.rating_text)
    val fullTilePriceContainer: View by bindView(R.id.full_tile_price_container)
    val fullTileStrikethroughPrice: TextView by bindView(R.id.full_tile_strikethrough_price)
    val fullTilePrice: TextView by bindView(R.id.full_tile_price)
    val halfTilePriceContainer: View by bindView(R.id.half_tile_price_container)
    val halfTileStrikethroughPrice: TextView by bindView(R.id.half_tile_strikethrough_price)
    val halfTilePrice: TextView by bindView(R.id.half_tile_price)
    val backgroundImage: ImageView by bindView(R.id.background_image)
    val saleTextView: TextView by bindView(R.id.launch_tile_upsell_text)
    val noRatingText: TextView by bindView(R.id.no_rating_text)

    init {
        green = ContextCompat.getColor(view.context, R.color.launch_discount)
        orange = ContextCompat.getColor(view.context, R.color.launch_air_attach)
        purple = ContextCompat.getColor(view.context, R.color.launch_mobile_exclusive)
        blue = ContextCompat.getColor(view.context, R.color.launch_tonight_only)
        mobileOnly = ContextCompat.getDrawable(view.context, R.drawable.ic_mobile_only)
        tonightOnly = ContextCompat.getDrawable(view.context, R.drawable.ic_tonight_only)

        itemView.setOnClickListener(this)
    }

    fun bindListData(data: Any, fullWidthTile: Boolean, hotelSelectedSubject: PublishSubject<Hotel>) {
        val context = itemView.context
        itemView.tag = data
        cardView.preventCornerOverlap = false

        if (fullWidthTile) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE.toFloat())
            fullTilePriceContainer.visibility = View.VISIBLE
            halfTilePriceContainer.visibility = View.GONE
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE.toFloat())
            fullTilePriceContainer.visibility = View.GONE
            halfTilePriceContainer.visibility = View.VISIBLE
        }

        val hotel = data as Hotel
        this.hotelSelectedSubject = hotelSelectedSubject
        bindHotelData(hotel, context, fullWidthTile)

    }

    private fun bindHotelData(hotel: Hotel, context: Context, fullWidth: Boolean) {
        title.text = hotel.localizedName
        subtitle.visibility = View.GONE
        ratingInfo.visibility = View.VISIBLE
        noRatingText.visibility = View.GONE

        if (fullWidth) {
            if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
                fullTileStrikethroughPrice.visibility = View.VISIBLE
                fullTileStrikethroughPrice.text = HtmlCompat.fromHtml(context.getString(R.string.strike_template,
                        StrUtils.formatHotelPrice(Money(Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers).toString(), hotel.lowRateInfo.currencyCode))),
                        null,
                        StrikethroughTagHandler())
            } else {
                fullTileStrikethroughPrice.visibility = View.GONE
            }
            fullTilePrice.text = StrUtils.formatHotelPrice(Money(Math.round(hotel.lowRateInfo.priceToShowUsers).toString(), hotel.lowRateInfo.currencyCode))
            if (hotel.hotelGuestRating == 0f) {
                ratingInfo.visibility = View.GONE
                noRatingText.visibility = View.VISIBLE
            } else {
                rating.text = java.lang.Float.toString(hotel.hotelGuestRating)
                ratingText.visibility = View.VISIBLE
            }
        } else {
            if (PointOfSale.getPointOfSale().supportsStrikethroughPrice() && HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
                halfTileStrikethroughPrice.visibility = View.VISIBLE
                halfTileStrikethroughPrice.text = HtmlCompat.fromHtml(context.getString(R.string.strike_template,
                        StrUtils.formatHotelPrice(Money(Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers).toString(), hotel.rateCurrencyCode))),
                        null,
                        StrikethroughTagHandler())
            } else {
                halfTileStrikethroughPrice.visibility = View.GONE
            }
            halfTilePrice.text = StrUtils.formatHotelPrice(Money(Math.round(hotel.lowRateInfo.priceToShowUsers).toString(), hotel.lowRateInfo.currencyCode))
            if (hotel.hotelGuestRating == 0f) {
                ratingInfo.visibility = View.INVISIBLE
            } else {
                rating.text = java.lang.Float.toString(hotel.hotelGuestRating)
            }
            ratingText.visibility = View.GONE
        }
        setHotelDiscountBanner(hotel, context, fullWidth)
    }

    // Set appropriate discount and / or DRR message
    private fun setHotelDiscountBanner(hotel: Hotel, context: Context, fullWidth: Boolean) {
        if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
            saleTextView.visibility = View.VISIBLE
            // Mobile exclusive case
            if (hotel.isDiscountRestrictedToCurrentSourceType) {
                saleTextView.setBackgroundColor(purple)
                saleTextView.setCompoundDrawablesWithIntrinsicBounds(mobileOnly, null, null, null)
                if (fullWidth) {
                    saleTextView.setText(R.string.launch_mobile_exclusive)
                } else {
                    saleTextView.text = context.getString(R.string.percent_off_TEMPLATE,
                            HotelUtils.getDiscountPercent(hotel.lowRateInfo))
                }
            } else if (hotel.isSameDayDRR) {
                saleTextView.setBackgroundColor(blue)
                saleTextView.setCompoundDrawablesWithIntrinsicBounds(tonightOnly, null, null, null)
                if (fullWidth) {
                    saleTextView.setText(R.string.launch_tonight_only)
                } else {
                    saleTextView.text = context.getString(R.string.percent_off_TEMPLATE,
                            HotelUtils.getDiscountPercent(hotel.lowRateInfo))
                }
            } else {
                saleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                saleTextView.text = context.getString(R.string.percent_off_TEMPLATE,
                        HotelUtils.getDiscountPercent(hotel.lowRateInfo))
                if (hotel.lowRateInfo.airAttached) {
                    saleTextView.setBackgroundColor(orange)
                } else {
                    saleTextView.setBackgroundColor(green)
                }
            }// Default discount case
            // Tonight only case
        } else {
            saleTextView.visibility = View.GONE
        }
    }

    override fun onClick(view: View) {
        val selectedHotel = view.tag as Hotel
        if (hotelSelectedSubject != null) {
            hotelSelectedSubject!!.onNext(selectedHotel)
        }
        Events.post(Events.LaunchListItemSelected(selectedHotel))
        OmnitureTracking.trackNewLaunchScreenTileClick(false)
    }

    override fun onBitmapLoaded() {
        gradient.visibility = View.VISIBLE
    }

    override fun onBitmapFailed() {
        gradient.visibility = View.GONE
    }

    override fun onPrepareLoad() {
        gradient.visibility = View.GONE
    }

}
