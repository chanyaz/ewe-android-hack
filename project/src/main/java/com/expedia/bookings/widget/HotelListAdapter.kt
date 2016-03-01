package com.expedia.bookings.widget

import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.getGuestRatingBackgroundDrawable
import com.expedia.util.getGuestRatingRecommendedText
import com.expedia.util.subscribeBackgroundColor
import com.expedia.util.subscribeColorFilter
import com.expedia.util.subscribeImageDrawable
import com.expedia.util.subscribeStarColor
import com.expedia.util.subscribeStarRating
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelResultsPricingStructureHeaderViewModel
import com.larvalabs.svgandroid.widget.SVGView
import com.squareup.picasso.Picasso
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.collections.emptyList
import kotlin.collections.firstOrNull
import kotlin.collections.indexOfFirst
import kotlin.collections.listOf
import kotlin.properties.Delegates

class HotelListAdapter(val hotelSelectedSubject: PublishSubject<Hotel>, val headerSubject: PublishSubject<Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW = 0
    val PRICING_STRUCTURE_HEADER_VIEW = 1
    val HOTEL_VIEW = 2
    val LOADING_VIEW = 3

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val resultsSubject = BehaviorSubject.create<HotelSearchResponse>()
    val hotelSoldOut = endlessObserver<String> { soldOutHotelId ->
        hotels.firstOrNull { it.hotelId == soldOutHotelId }?.isSoldOut = true
        hotelListItemsMetadata.firstOrNull { it.hotelId == soldOutHotelId }?.hotelSoldOut?.onNext(true)
    }

    val isBucketedForResultMap = Db.getAbacusResponse().isUserBucketedForTest(
            AbacusUtils.EBAndroidAppHotelResultMapTest)

    private data class HotelListItemMetadata(val hotelId: String, val hotelSoldOut: BehaviorSubject<Boolean>)

    private val hotelListItemsMetadata: MutableList<HotelListItemMetadata> = ArrayList()

    private var hotels: List<Hotel> = emptyList()

    private fun getHotel(rawAdapterPosition: Int): Hotel {
        return hotels.get(rawAdapterPosition - numHeaderItemsInHotelsList())
    }

    init {
        resultsSubject.subscribe { response ->
            loading = false
            hotels = ArrayList(response.hotelList)
            hotelListItemsMetadata.clear()
            notifyDataSetChanged()
        }
        loadingSubject.subscribe {
            loading = true
        }
    }

    fun isLoading(): Boolean {
        return loading
    }

    fun showLoading() {
        loadingSubject.onNext(Unit)
        // show 3 tiles during loading if map is hidden to user
        if (isBucketedForResultMap)
            hotels = listOf(Hotel(), Hotel(), Hotel())
        else
            hotels = listOf(Hotel(), Hotel())
        notifyDataSetChanged()
    }

    fun numHeaderItemsInHotelsList(): Int {
        return 2
    }

    override fun getItemCount(): Int {
        return hotels.size + numHeaderItemsInHotelsList()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW
        } else if (position == 1) {
            return PRICING_STRUCTURE_HEADER_VIEW
        } else if (loading) {
            return LOADING_VIEW
        } else {
            return HOTEL_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fixedPosition = position - numHeaderItemsInHotelsList()
        when (holder) {
            is HotelViewHolder -> holder.bind(HotelViewModel(holder.itemView.context, hotels.get(fixedPosition)))
            is LoadingViewHolder -> holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView, fixedPosition % 2 == 0))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW) {
            val header = View(parent.context)
            var lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.height = if (isBucketedForResultMap || (ExpediaBookingApp.isAutomation() || ExpediaBookingApp.isDeviceShitty())) 0 else parent.height
            header.layoutParams = lp

            return MapSwitchClickInterceptorTransparentHeaderViewHolder(header)
        } else if (viewType == LOADING_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_loading_cell, parent, false)
            return LoadingViewHolder(view)
        } else if (viewType == PRICING_STRUCTURE_HEADER_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_results_pricing_structure_header_cell, parent, false)
            val vm = HotelResultsPricingStructureHeaderViewModel(view.resources)
            loadingSubject.subscribe(vm.loadingStartedObserver)
            resultsSubject.subscribe(vm.resultsDeliveredObserver)
            val holder = HotelResultsPricingStructureHeaderViewHolder(view as ViewGroup, vm)
            return holder
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
            return HotelViewHolder(view as ViewGroup, parent.width)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == LOADING_VIEW) {
            (holder as LoadingViewHolder).cancelAnimation()
        } else if (holder.itemViewType == HOTEL_VIEW) {
            val hotelItemIndex = hotelListItemsMetadata.indexOfFirst { it.hotelId == (holder as HotelViewHolder).hotelId }
            if (hotelItemIndex != -1) {
                hotelListItemsMetadata.removeAt(hotelItemIndex)
            }
        }
        super.onViewRecycled(holder)
    }

    public inner class HotelViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val PICASSO_TAG = "HOTEL_RESULTS_LIST"
        val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .25f, .3f, 1f)

        val resources = root.resources

        var hotelId: String by Delegates.notNull()
        val imageView: ImageView by root.bindView(R.id.background)
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
        val airAttachDiscount: TextView by root.bindView(R.id.air_attach_discount)
        val airAttachSVG: SVGView by root.bindView(R.id.air_attach_curve)
        val airAttachContainer: LinearLayout by root.bindView(R.id.air_attach_layout)
        val ratingAmenityContainer: View by root.bindView(R.id.rating_amenity_container)
        val priceIncludesFlightsView: LinearLayout by root.bindView(R.id.price_includes_flights)

        init {
            itemView.setOnClickListener(this)

            if (!ExpediaBookingApp.isAutomation()) {
                airAttachSVG.setSVG(R.raw.air_attach_curve)
            }

            if (shouldShowCircleForRatings()) {
                ratingBar = root.findViewById(R.id.circle_rating_bar) as StarRatingBar
            } else {
                ratingBar = root.findViewById(R.id.star_rating_bar) as StarRatingBar
            }
            ratingBar.visibility = View.VISIBLE
        }

        fun bind(viewModel: HotelViewModel) {
            hotelId = viewModel.hotelId
            hotelListItemsMetadata.add(HotelListItemMetadata(viewModel.hotelId, viewModel.soldOut))

            viewModel.hotelNameObservable.subscribeText(hotelName)
            viewModel.pricePerNightObservable.subscribeText(pricePerNight)
            viewModel.pricePerNightColorObservable.subscribeTextColor(pricePerNight)
            viewModel.hotelGuestRatingObservable.subscribe { rating ->
                guestRating.text = rating.toString()
                guestRating.background = getGuestRatingBackgroundDrawable(rating, itemView.context)
                guestRatingRecommendedText.text = getGuestRatingRecommendedText(rating, itemView.resources)
            }
            viewModel.topAmenityTitleObservable.subscribeText(topAmenityTitle)
            viewModel.hotelDiscountPercentageObservable.subscribeText(discountPercentage)
            viewModel.hotelStrikeThroughPriceFormatted.subscribeText(strikeThroughPricePerNight)
            viewModel.hotelStrikeThroughPriceVisibility.subscribeVisibility(strikeThroughPricePerNight)
            viewModel.isHotelGuestRatingAvailableObservable.subscribeVisibility(guestRating)
            viewModel.isHotelGuestRatingAvailableObservable.subscribeVisibility(guestRatingRecommendedText)
            viewModel.isHotelGuestRatingAvailableObservable.map { !it }.subscribeVisibility(noGuestRating)
            viewModel.hasDiscountObservable.subscribeVisibility(discountPercentage)
            viewModel.distanceFromCurrentLocation.subscribeText(hotelAmenityOrDistanceFromLocation)
            viewModel.topAmenityVisibilityObservable.subscribeVisibility(topAmenityTitle)
            viewModel.topAmenityTitleObservable.subscribeText(topAmenityTitle)
            viewModel.urgencyIconObservable.subscribeImageDrawable(urgencyIcon)
            viewModel.urgencyIconVisibilityObservable.subscribeVisibility(urgencyIcon)
            viewModel.urgencyMessageVisibilityObservable.subscribeVisibility(urgencyMessageContainer)
            viewModel.urgencyMessageBackgroundObservable.subscribeBackgroundColor(urgencyMessageContainer)
            viewModel.urgencyMessageBoxObservable.subscribeText(urgencyMessageBox)
            viewModel.vipMessageVisibilityObservable.subscribeVisibility(vipMessage)
            viewModel.airAttachVisibilityObservable.subscribeVisibility(airAttachContainer)
            viewModel.hotelDiscountPercentageObservable.subscribeText(airAttachDiscount)
            viewModel.ratingAmenityContainerVisibilityObservable.subscribeVisibility(ratingAmenityContainer)

            viewModel.toolBarRatingColor.subscribeStarColor(ratingBar)
            viewModel.imageColorFilter.subscribeColorFilter(imageView)
            viewModel.hotelStarRatingObservable.subscribeStarRating(ratingBar)
            viewModel.priceIncludesFlightsObservable.subscribeVisibility(priceIncludesFlightsView)

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
        }

        override fun onClick(view: View) {
            val hotel: Hotel = getHotel(adapterPosition)
            hotelSelectedSubject.onNext(hotel)
            if (hotel.isSponsoredListing) {
                AdImpressionTracking.trackAdClickOrImpression(itemView.context, hotel.clickTrackingUrl, null)
                HotelV2Tracking().trackHotelV2SponsoredListingClick()
            }
        }

        private val target = object : PicassoTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                super.onBitmapLoaded(bitmap, from)
                imageView.setImageBitmap(bitmap)

                val palette = Palette.Builder(bitmap).generate()
                val color = palette.getDarkVibrantColor(R.color.transparent_dark)

                val fullColorBuilder = ColorBuilder(color).darkenBy(.6f).setSaturation(if (!mIsFallbackImage) .8f else 0f);
                val startColor = fullColorBuilder.setAlpha(154).build()
                val endColor = fullColorBuilder.setAlpha(0).build()

                val colorArrayBottom = intArrayOf(0, 0, endColor, startColor)
                val colorArrayFull = intArrayOf(startColor, 0, endColor, startColor)

                val drawable = PaintDrawable()
                drawable.shape = RectShape()

                if (vipMessage.visibility == View.VISIBLE ) {
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

        private fun getShader(array: IntArray) : ShapeDrawable.ShaderFactory {
            return object : ShapeDrawable.ShaderFactory() {
                override fun resize(width: Int , height: Int) : Shader {
                    val lg = LinearGradient(0f, 0f, 0f, height.toFloat(),
                            array, DEFAULT_GRADIENT_POSITIONS, Shader.TileMode.REPEAT)
                    return lg
                }
            }
        }

    }

    public inner class HotelResultsPricingStructureHeaderViewHolder(val root: ViewGroup, val vm: HotelResultsPricingStructureHeaderViewModel) : RecyclerView.ViewHolder(root) {
        val pricingStructureHeader: TextView by root.bindView(R.id.pricing_structure_header)
        val loyaltyPointsAppliedHeader: TextView by root.bindView(R.id.loyalty_points_applied_message)
        val shadow: View by root.bindView(R.id.drop_shadow)

        init {
            if (isBucketedForResultMap || ExpediaBookingApp.isDeviceShitty()) {
                shadow.visibility = View.GONE
            }
            vm.pricingStructureHeaderObservable.subscribeText(pricingStructureHeader)
            vm.loyaltyAvailableObservable.subscribeVisibility(loyaltyPointsAppliedHeader)
        }
    }

    public inner class MapSwitchClickInterceptorTransparentHeaderViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        init {
            itemView.setOnClickListener {
                headerSubject.onNext(Unit)
            }
        }
    }
}
