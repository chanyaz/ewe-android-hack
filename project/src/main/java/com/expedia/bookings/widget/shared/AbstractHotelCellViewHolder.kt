package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.hotel.hotelCellModules.HotelCellNameStarAmenityDistance
import com.expedia.bookings.widget.hotel.hotelCellModules.HotelCellPriceTopAmenity
import com.expedia.bookings.widget.hotel.hotelCellModules.HotelCellUrgencyMessage
import com.expedia.bookings.widget.hotel.hotelCellModules.HotelCellVipMessage
import com.expedia.util.getGuestRatingText
import com.expedia.util.setInverseVisibility
import com.expedia.util.updateVisibility
import com.expedia.vm.hotel.HotelViewModel
import com.larvalabs.svgandroid.widget.SVGView
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

abstract class AbstractHotelCellViewHolder(val root: ViewGroup) :
        RecyclerView.ViewHolder(root), View.OnClickListener {

    abstract fun createHotelViewModel(context: Context): HotelViewModel

    val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    val hotelClickedSubject = PublishSubject.create<Int>()

    val resources = root.resources
    var hotelId: String by Delegates.notNull()
    val viewModel = createHotelViewModel(itemView.context)

    val pinnedHotelTextView: TextView by bindView(R.id.pinned_hotel_view)
    val imageView: ImageView by bindView(R.id.background)
    val gradient: View by bindView(R.id.foreground)
    val hotelNameStarAmenityDistance: HotelCellNameStarAmenityDistance by bindView(R.id.hotel_name_star_amenity_distance)
    val hotelPriceTopAmenity: HotelCellPriceTopAmenity by bindView(R.id.hotel_price_top_amenity)
    val guestRating: TextView by bindView(R.id.guest_rating)
    val guestRatingRecommendedText: TextView by bindView(R.id.guest_rating_recommended_text)
    val noGuestRating: TextView by bindView(R.id.no_guest_rating)
    val discountPercentage: TextView by bindView(R.id.discount_percentage)
    val ratingPointsContainer: LinearLayout by bindView(R.id.rating_earn_container)
    val urgencyMessageContainer: HotelCellUrgencyMessage by bindView(R.id.urgency_message_layout)
    val vipMessageContainer: HotelCellVipMessage by bindView(R.id.vip_message_container)
    val airAttachDiscount: TextView by bindView(R.id.air_attach_discount)
    val airAttachSVG: SVGView by bindView(R.id.air_attach_curve)
    val airAttachContainer: LinearLayout by bindView(R.id.air_attach_layout)
    val airAttachSWPImage: ImageView by bindView(R.id.air_attach_swp_image)
    val earnMessagingText: TextView by bindView(R.id.earn_messaging)
    val cardView: CardView by bindView(R.id.card_view)

    init {
        itemView.setOnClickListener(this)

        LayoutUtils.setSVG(airAttachSVG, R.raw.air_attach_curve)
    }

    @CallSuper
    open fun bindHotelData(hotel: Hotel) {
        viewModel.bindHotelData(hotel)

        this.hotelId = hotel.hotelId

        hotelNameStarAmenityDistance.update(viewModel)
        hotelPriceTopAmenity.update(viewModel)
        vipMessageContainer.update(viewModel)
        urgencyMessageContainer.update(viewModel)

        imageView.colorFilter = viewModel.getImageColorFilter()

        updateDiscountPercentage()
        updateHotelGuestRating()

        updateAirAttach()

        earnMessagingText.text = viewModel.earnMessage
        earnMessagingText.updateVisibility(viewModel.showEarnMessage)

        ratingPointsContainer.updateVisibility(viewModel.showRatingPointsContainer())

        loadHotelImage()

        cardView.contentDescription = viewModel.getHotelContentDesc()
    }

    override fun onClick(view: View) {
        hotelClickedSubject.onNext(adapterPosition)
    }

    fun markPinned(pin: Boolean) {
        pinnedHotelTextView.updateVisibility(pin)
    }

    private fun loadHotelImage() {
        val url = viewModel.getHotelLargeThumbnailUrl()
        if (url.isNotBlank()) {

            if (imageView.width == 0) {
                // Because of prefetch search results get bound before they are laid out.
                var onLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
                onLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                    PicassoHelper.Builder(itemView.context)
                            .setPlaceholder(R.drawable.results_list_placeholder)
                            .setError(R.drawable.room_fallback)
                            .setCacheEnabled(false)
                            .setTarget(target).setTag(PICASSO_TAG)
                            .build()
                            .load(HotelMedia(url).getBestUrls(imageView.width / 2))
                    imageView.viewTreeObserver.removeOnGlobalLayoutListener(onLayoutListener)
                }

                imageView.viewTreeObserver.addOnGlobalLayoutListener(onLayoutListener)
            } else {
                PicassoHelper.Builder(itemView.context)
                        .setPlaceholder(R.drawable.results_list_placeholder)
                        .setError(R.drawable.room_fallback)
                        .setCacheEnabled(false)
                        .setTarget(target).setTag(PICASSO_TAG)
                        .build()
                        .load(HotelMedia(url).getBestUrls(imageView.width / 2))
            }
        }
    }

    private fun updateAirAttach() {
        airAttachContainer.updateVisibility(viewModel.showAirAttachWithDiscountLabel)
        airAttachSWPImage.updateVisibility(viewModel.showAirAttachIconWithoutDiscountLabel)
        airAttachDiscount.text = viewModel.hotelDiscountPercentage
    }

    private fun updateDiscountPercentage() {
        discountPercentage.text = viewModel.hotelDiscountPercentage
        if(viewModel.hasMemberDeal()) {
            discountPercentage.setBackgroundResource(R.drawable.member_only_discount_percentage_background)
            discountPercentage.setTextColor(ContextCompat.getColor(itemView.context, R.color.member_pricing_text_color))
        } else {
            discountPercentage.setBackgroundResource(R.drawable.discount_percentage_background)
            discountPercentage.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
        }
        discountPercentage.updateVisibility(viewModel.showDiscount)
    }

    private fun updateHotelGuestRating() {
        if (viewModel.isHotelGuestRatingAvailable) {
            val rating = viewModel.hotelGuestRating
            guestRating.text = rating.toString()
            guestRatingRecommendedText.text = getGuestRatingText(rating, itemView.resources)
        }

        guestRating.updateVisibility(viewModel.isHotelGuestRatingAvailable)
        guestRatingRecommendedText.updateVisibility(viewModel.isHotelGuestRatingAvailable)
        noGuestRating.setInverseVisibility(viewModel.isHotelGuestRatingAvailable)
    }

    private val target = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)
            imageView.setImageBitmap(bitmap)
            val listener = Palette.PaletteAsyncListener { palette ->
                mixColor(palette)
            }
            Palette.Builder(bitmap).generate(listener)

        }

        private fun mixColor(palette: Palette) {
            val color = palette.getDarkVibrantColor(R.color.transparent_dark)
            val fullColorBuilder = ColorBuilder(color).darkenBy(.6f).setSaturation(if (!mIsFallbackImage) .8f else 0f)
            val topColor = fullColorBuilder.setAlpha(80).build() // 30
            val midColor1 = fullColorBuilder.setAlpha(15).build() // 5
            val midColor2 = fullColorBuilder.setAlpha(25).build() // 10
            val bottomColor = fullColorBuilder.setAlpha(125).build() // 50
            val startColor = fullColorBuilder.setAlpha(154).build()
            val endColor = fullColorBuilder.setAlpha(0).build()
            val colorArrayBottom = intArrayOf(0, 0, endColor, startColor)
            val colorArrayFull = intArrayOf(topColor, midColor1, midColor2, bottomColor)

            val drawable = PaintDrawable()
            drawable.shape = RectShape()

            if (vipMessageContainer.visibility == View.VISIBLE) {
                drawable.shaderFactory = getShader(colorArrayFull)
            } else {
                drawable.shaderFactory = getShader(colorArrayBottom)
            }

            gradient.background = drawable
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            super.onBitmapFailed(errorDrawable)
            if (errorDrawable != null) {
                imageView.setImageDrawable(errorDrawable)
            }
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)
            imageView.setImageDrawable(placeHolderDrawable)
            gradient.background = null
        }
    }

    private fun getShader(array: IntArray): ShapeDrawable.ShaderFactory {
        return object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                val lg = LinearGradient(0f, 0f, 0f, height.toFloat(),
                        array, DEFAULT_GRADIENT_POSITIONS, Shader.TileMode.REPEAT)
                return lg
            }
        }
    }
}
