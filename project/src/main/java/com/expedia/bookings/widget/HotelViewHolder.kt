package com.expedia.bookings.widget

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.expedia.vm.launch.RecommendedHotelViewModel
import io.reactivex.subjects.PublishSubject

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
    private var discountTenPercentOrBetter: Boolean = true
    private var isDiscountRestrictedToCurrentSourceType = false
    private var isSameDayDRR = false
    private var isAirAttached = false

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

    fun bindListData(data: Any, fullWidthTile: Boolean, hotelSelectedSubject: PublishSubject<Hotel>, vm: RecommendedHotelViewModel) {
        this.hotelSelectedSubject = hotelSelectedSubject

        bindViewModel(vm)

        itemView.tag = data
        cardView.preventCornerOverlap = false
        subtitle.visibility = View.GONE
        cardView.contentDescription = vm.hotelContentDesc

        createDiscountBanner(fullWidthTile)
        checkForHideStrikethrough()

        if (fullWidthTile) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE.toFloat())
            fullTilePriceContainer.visibility = View.VISIBLE
            halfTilePriceContainer.visibility = View.GONE
            createFullWidthRating()
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE.toFloat())
            fullTilePriceContainer.visibility = View.GONE
            halfTilePriceContainer.visibility = View.VISIBLE
            createHalfWidthRating()
        }
    }

    private fun bindViewModel(vm: RecommendedHotelViewModel) {
        title.text = vm.title
        rating.text = vm.rating
        fullTilePrice.text = vm.price
        halfTilePrice.text = vm.price
        saleTextView.text = vm.saleText
        fullTileStrikethroughPrice.text = vm.strikeThroughPrice
        halfTileStrikethroughPrice.text = vm.strikeThroughPrice
        discountTenPercentOrBetter = vm.discountTenPercentOrBetter
        isDiscountRestrictedToCurrentSourceType = vm.isDiscountRestrictedToCurrentSourceType
        isSameDayDRR = vm.isSameDayDRR
        isAirAttached = vm.isAirAttached
    }

    private fun createHalfWidthRating() {
        ratingText.visibility = View.GONE
        if (rating.text.equals("0.0")) {
            ratingInfo.visibility = View.INVISIBLE
        }
    }

    private fun createFullWidthRating() {
        if (rating.text.equals("0.0")) {
            ratingInfo.visibility = View.GONE
            noRatingText.visibility = View.VISIBLE
        } else {
            ratingText.visibility = View.VISIBLE
        }
    }

    private fun checkForHideStrikethrough() {
        if (!PointOfSale.getPointOfSale().supportsStrikethroughPrice() || !discountTenPercentOrBetter) {
            fullTileStrikethroughPrice.visibility = View.GONE
            halfTileStrikethroughPrice.visibility = View.GONE
        } else {
            fullTileStrikethroughPrice.visibility = View.VISIBLE
            halfTileStrikethroughPrice.visibility = View.VISIBLE
        }
    }

    private fun createDiscountBanner(fullWidth: Boolean) {
        if (discountTenPercentOrBetter) {
            saleTextView.visibility = View.VISIBLE
            showDiscountMessage(fullWidth)
        } else {
            saleTextView.visibility = View.GONE
        }
    }

    private fun showDiscountMessage(fullWidth: Boolean) {
        if (isDiscountRestrictedToCurrentSourceType) {
            saleTextView.setBackgroundColor(purple)
            saleTextView.setCompoundDrawablesWithIntrinsicBounds(mobileOnly, null, null, null)
            if (fullWidth) {
                saleTextView.setText(R.string.launch_mobile_exclusive)
            }
        } else if (isSameDayDRR) {
            saleTextView.setBackgroundColor(blue)
            saleTextView.setCompoundDrawablesWithIntrinsicBounds(tonightOnly, null, null, null)
            if (fullWidth) {
                saleTextView.setText(R.string.launch_tonight_only)
            }
        } else if (isAirAttached) {
            saleTextView.setBackgroundColor(orange)
        } else {
            saleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            saleTextView.setBackgroundColor(green)
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
