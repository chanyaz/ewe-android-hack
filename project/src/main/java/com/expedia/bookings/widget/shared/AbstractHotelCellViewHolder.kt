package com.expedia.bookings.widget.shared

import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.hotel.widget.HotelCellNameStarAmenityDistance
import com.expedia.bookings.hotel.widget.HotelCellPriceTopAmenity
import com.expedia.bookings.hotel.widget.HotelCellUrgencyMessage
import com.expedia.bookings.hotel.widget.HotelCellVipMessage
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.larvalabs.svgandroid.widget.SVGView
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject

abstract class AbstractHotelCellViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

    val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    val hotelClickedSubject = PublishSubject.create<Int>()

    val resources = root.resources

    val pinnedHotelTextView: TextView by bindView(R.id.pinned_hotel_view)
    val imageView: ImageView by bindView(R.id.background)
    val soldOutOverlay: View by bindView(R.id.hotel_sold_out_overlay_container)
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

    override fun onClick(view: View) {
        hotelClickedSubject.onNext(adapterPosition)
    }

    fun markPinned(pin: Boolean) {
        pinnedHotelTextView.setVisibility(pin)
    }

    protected fun loadHotelImage(url: String) {
        if (url.isNotBlank()) {

            if (imageView.width == 0) {
                // Because of prefetch search results get bound before they are laid out.
                var layoutChangeListener: View.OnLayoutChangeListener? = null
                layoutChangeListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    PicassoHelper.Builder(itemView.context)
                            .setPlaceholder(R.drawable.results_list_placeholder)
                            .setError(R.drawable.room_fallback)
                            .setCacheEnabled(false)
                            .setTarget(target).setTag(PICASSO_TAG)
                            .build()
                            .load(HotelMedia(url).getBestUrls(imageView.width / 2))
                    imageView.removeOnLayoutChangeListener(layoutChangeListener)
                }
                imageView.addOnLayoutChangeListener(layoutChangeListener)
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

    private val target = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)
            imageView.setImageBitmap(bitmap)
            addGradient()
        }

        private fun addGradient() {
            val colorBuilder = ColorBuilder(ContextCompat.getColor(gradient.context, R.color.gray900))
            val topColor = colorBuilder.setAlpha(60).build()
            val midColor1 = colorBuilder.setAlpha(15).build()
            val midColor2 = colorBuilder.setAlpha(40).build()
            val bottomColor = colorBuilder.setAlpha(154).build()
            val colorArrayBottom = intArrayOf(0, 0, midColor2, bottomColor)
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
                val textHeightAsPercent = (hotelNameStarAmenityDistance.parent as View).top.toFloat() / height
                val lg = LinearGradient(0f, 0f, 0f, height.toFloat(),
                        array, getGradientPositions(textHeightAsPercent), Shader.TileMode.REPEAT)
                return lg
            }

            private fun getGradientPositions(textHeightPercent: Float): FloatArray {
                val gradientPositions = DEFAULT_GRADIENT_POSITIONS.copyOf()
                gradientPositions[1] = Math.min(DEFAULT_GRADIENT_POSITIONS[1], textHeightPercent)
                gradientPositions[2] = textHeightPercent
                return gradientPositions
            }
        }
    }
}
