package com.expedia.bookings.widget.hotel

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FavoriteButton
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.TextView
import com.expedia.util.getABTestGuestRatingBackground
import com.expedia.util.getABTestGuestRatingText
import com.expedia.util.subscribeBackgroundColor
import com.expedia.util.subscribeColorFilter
import com.expedia.util.subscribeImageDrawable
import com.expedia.util.subscribeStarColor
import com.expedia.util.subscribeStarRating
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelViewModel
import com.larvalabs.svgandroid.widget.SVGView
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

open class HotelCellViewHolder(val root: ViewGroup, val width: Int) :
        RecyclerView.ViewHolder(root), View.OnClickListener {

    val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    val resources = root.resources

    val cardView: CardView by root.bindView(R.id.card_view)
    var hotelId: String by Delegates.notNull()
    val imageView: ImageView by root.bindView(R.id.background)
    val heartView: FavoriteButton by root.bindView(R.id.heart_image_view)
    val gradient: View by root.bindView(R.id.foreground)
    val hotelName: TextView by root.bindView(R.id.hotel_name_text_view)
    val pricePerNight: TextView by root.bindView(R.id.price_per_night)
    val strikeThroughPricePerNight: TextView by root.bindView(R.id.strike_through_price)
    val guestRating: TextView by root.bindView(R.id.guest_rating)
    val guestRatingRecommendedText: TextView by root.bindView(R.id.guest_rating_recommended_text)
    val noGuestRating: TextView by root.bindView(R.id.no_guest_rating)
    val topAmenityTitle: TextView by root.bindView(R.id.top_amenity_title)
    var ratingBar: StarRatingBar by Delegates.notNull()
    val discountPercentage: TextView by root.bindView(R.id.discount_percentage)
    val hotelAmenityOrDistanceFromLocation: TextView by root.bindView(R.id.hotel_amenity_or_distance_from_location)

    val urgencyMessageContainer: LinearLayout by root.bindView (R.id.urgency_message_layout)
    val urgencyIcon: ImageView by root.bindView(R.id.urgency_icon)
    val urgencyMessageBox: TextView by root.bindView(R.id.urgency_message)
    val vipMessage: TextView by root.bindView(R.id.vip_message)
    val vipLoyaltyMessage: TextView by root.bindView(R.id.vip_loyalty_message)
    val airAttachDiscount: TextView by root.bindView(R.id.air_attach_discount)
    val airAttachSVG: SVGView by root.bindView(R.id.air_attach_curve)
    val airAttachContainer: LinearLayout by root.bindView(R.id.air_attach_layout)
    val airAttachSWPImage: ImageView by root.bindView(R.id.air_attach_swp_image)
    val ratingAmenityContainer: View by root.bindView(R.id.rating_amenity_container)
    val earnMessagingText: TextView by root.bindView(R.id.earn_messaging)

    val hotelClickedSubject = PublishSubject.create<Int>()

    init {
        itemView.setOnClickListener(this)

        LayoutUtils.setSVG(airAttachSVG, R.raw.air_attach_curve);

        if (shouldShowCircleForRatings()) {
            ratingBar = root.findViewById(R.id.circle_rating_bar) as StarRatingBar
        } else {
            ratingBar = root.findViewById(R.id.star_rating_bar) as StarRatingBar
        }
        ratingBar.visibility = View.VISIBLE
    }

    open fun bind(viewModel: HotelViewModel) {
        hotelId = viewModel.hotelId

        viewModel.hotelNameObservable.subscribeText(hotelName)
        viewModel.pricePerNightObservable.subscribeText(pricePerNight)
        viewModel.pricePerNightColorObservable.subscribeTextColor(pricePerNight)
        viewModel.hotelGuestRatingObservable.subscribe { rating ->
            guestRating.text = rating.toString()
            guestRating.background = getGuestRatingBackground(rating, itemView.context)
            guestRatingRecommendedText.text = getGuestRatingRecommendedText(rating, itemView.resources)
        }
        viewModel.topAmenityTitleObservable.subscribeText(topAmenityTitle)
        viewModel.hotelDiscountPercentageObservable.subscribeText(discountPercentage)
        viewModel.hotelStrikeThroughPriceFormatted.subscribeText(strikeThroughPricePerNight)
        viewModel.hotelStrikeThroughPriceVisibility.subscribeVisibility(strikeThroughPricePerNight)
        viewModel.isHotelGuestRatingAvailableObservable.subscribeVisibility(guestRating)
        viewModel.isHotelGuestRatingAvailableObservable.subscribeVisibility(guestRatingRecommendedText)
        viewModel.isHotelGuestRatingAvailableObservable.map { !it }.subscribeVisibility(noGuestRating)
        viewModel.showDiscountObservable.subscribeVisibility(discountPercentage)
        viewModel.distanceFromCurrentLocation.subscribeText(hotelAmenityOrDistanceFromLocation)
        viewModel.topAmenityVisibilityObservable.subscribeVisibility(topAmenityTitle)
        viewModel.topAmenityTitleObservable.subscribeText(topAmenityTitle)
        viewModel.urgencyIconObservable.subscribeImageDrawable(urgencyIcon)
        viewModel.urgencyIconVisibilityObservable.subscribeVisibility (urgencyIcon)
        viewModel.urgencyMessageVisibilityObservable.subscribeVisibility(urgencyMessageContainer)
        viewModel.urgencyMessageBackgroundObservable.subscribeBackgroundColor(urgencyMessageContainer)
        viewModel.urgencyMessageBoxObservable.subscribeText(urgencyMessageBox)
        viewModel.vipMessageVisibilityObservable.subscribeVisibility(vipMessage)
        viewModel.vipLoyaltyMessageVisibilityObservable.subscribeVisibility(vipLoyaltyMessage)
        viewModel.airAttachWithDiscountLabelVisibilityObservable.subscribeVisibility(airAttachContainer)
        viewModel.airAttachIconWithoutDiscountLabelVisibility.subscribeVisibility(airAttachSWPImage)
        viewModel.hotelDiscountPercentageObservable.subscribeText(airAttachDiscount)
        viewModel.ratingAmenityContainerVisibilityObservable.subscribeVisibility(ratingAmenityContainer)
        viewModel.earnMessagingObservable.subscribeText(earnMessagingText)
        viewModel.earnMessagingVisibilityObservable.subscribeVisibility(earnMessagingText)

        viewModel.toolBarRatingColor.subscribeStarColor(ratingBar)
        viewModel.imageColorFilter.subscribeColorFilter(imageView)
        viewModel.hotelStarRatingObservable.subscribeStarRating(ratingBar)

        viewModel.adImpressionObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(itemView.context, it, null)
            viewModel.setImpressionTracked(true)
        }

        viewModel.hotelLargeThumbnailUrlObservable.subscribe { url ->
            PicassoHelper.Builder(itemView.context)
                    .setPlaceholder(R.drawable.results_list_placeholder)
                    .setError(R.drawable.room_fallback)
                    .setCacheEnabled(false)
                    .setTarget(target).setTag(PICASSO_TAG)
                    .build()
                    .load(HotelMedia(url).getBestUrls(width / 2))
        }

        if (HotelFavoriteHelper.showHotelFavoriteTest(root.context)) {
            heartView.hotelId = hotelId
            heartView.updateImageState()
        }
        cardView.contentDescription = viewModel.getHotelContentDesc()
    }

    override fun onClick(view: View) {
        hotelClickedSubject.onNext(adapterPosition)
    }

    open fun getGuestRatingRecommendedText(rating: Float, resources: Resources): String {
        return getABTestGuestRatingText(rating, resources)
    }

    open fun getGuestRatingBackground(rating: Float, context: Context): Drawable {
        return getABTestGuestRatingBackground(rating, context)
    }

    private val target = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)
            imageView.setImageBitmap(bitmap)

            val palette = Palette.Builder(bitmap).generate()
            val color = palette.getDarkVibrantColor(R.color.transparent_dark)

            val fullColorBuilder = ColorBuilder(color).darkenBy(.6f).setSaturation(if (!mIsFallbackImage) .8f else 0f);
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

            if (vipMessage.visibility == View.VISIBLE || HotelFavoriteHelper.showHotelFavoriteTest(root.context)) {
                drawable.shaderFactory = getShader(colorArrayFull)
            } else {
                drawable.shaderFactory = getShader(colorArrayBottom)
            }

            gradient.background = drawable

            if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(root.context, AbacusUtils.EBAndroidAppHotelFavoriteTest,
                    R.string.preference_enable_hotel_favorite)) {
                heartView.bringToFront()
            }
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
