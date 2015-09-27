package com.expedia.bookings.widget

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribe
import com.expedia.util.subscribeBackgroundColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelResultsPricingStructureHeaderViewModel
import com.squareup.picasso.Picasso
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

public class HotelListAdapter(val hotelSelectedSubject: PublishSubject<Hotel>, val headerSubject: PublishSubject<Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW = 0
    val PRICING_STRUCTURE_HEADER_VIEW = 1
    val HOTEL_VIEW = 2
    val LOADING_VIEW = 3

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val resultsSubject = PublishSubject.create<HotelSearchResponse>()

    private var hotels: List<Hotel> = emptyList()

    private fun getHotel(rawAdapterPosition: Int): Hotel {
        return hotels.get(rawAdapterPosition - numHeaderItemsInHotelsList())
    }

    init {
        resultsSubject.subscribe { response ->
            loading = false
            hotels = ArrayList(response.hotelList)
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
        hotels = listOf(Hotel(), Hotel())
        notifyDataSetChanged()
    }

    fun numHeaderItemsInHotelsList(): Int {
        return 2
    }

    override fun getItemCount(): Int {
        return hotels.size() + numHeaderItemsInHotelsList()
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
            lp.height = if (ExpediaBookingApp.isAutomation()) 0 else parent.height
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
        }
        super.onViewRecycled(holder)
    }

    public inner class HotelViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), HeaderBitmapDrawable.CallbackListener, View.OnClickListener {

        val PICASSO_TAG = "HOTEL_RESULTS_LIST"
        val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .5f, 1f)

        val resources = root.resources

        val imageView: ImageView by root.bindView(R.id.background)
        val hotelName: TextView by root.bindView(R.id.hotel_name_text_view)
        val pricePerNight: TextView by root.bindView(R.id.price_per_night)
        val strikeThroughPricePerNight: TextView by root.bindView(R.id.strike_through_price)
        val guestRating: TextView by root.bindView(R.id.guest_rating)
        val topAmenityTitle: TextView by root.bindView(R.id.top_amenity_title)
        val starRating: RatingBar by root.bindView(R.id.hotel_rating_bar)
        val discountPercentage: TextView by root.bindView(R.id.discount_percentage)
        val hotelAmenityOrDistanceFromLocation: TextView by root.bindView(R.id.hotel_amenity_or_distance_from_location)

        val urgencyMessageContainer: LinearLayout by root.bindView (R.id.urgency_message_layout)
        val urgencyIcon: ImageView by root.bindView(R.id.urgency_icon)
        val urgencyMessageBox: TextView by root.bindView(R.id.urgency_message)
        val vipMessage: TextView by root.bindView(R.id.vip_message)
        val airAttachDiscount: TextView by root.bindView(R.id.air_attach_discount)
        val airAttachContainer: LinearLayout by root.bindView(R.id.air_attach_layout)
        val gradientTop: View by root.bindView(R.id.gradient_top)
        val gradientBottom: View by root.bindView(R.id.gradient_bottom)

        init {
            itemView.setOnClickListener(this)

            strikeThroughPricePerNight.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        public fun bind(viewModel: HotelViewModel) {
            viewModel.hotelLargeThumbnailUrlObservable.subscribe { url ->
                PicassoHelper.Builder(itemView.context)
                        .setPlaceholder(R.drawable.results_list_placeholder)
                        .setError(R.drawable.results_list_placeholder)
                        .setTarget(target).setTag(PICASSO_TAG)
                        .build()
                        .load(HotelMedia(url).getBestUrls(width))
            }

            viewModel.hotelNameObservable.subscribe(hotelName)
            viewModel.pricePerNightObservable.subscribe(pricePerNight)
            viewModel.hotelGuestRatingObservable.subscribe(guestRating)
            viewModel.topAmenityTitleObservable.subscribe(topAmenityTitle)
            viewModel.hotelDiscountPercentageObservable.subscribe(discountPercentage)
            viewModel.hotelStrikeThroughPriceObservable.subscribe(strikeThroughPricePerNight)
            viewModel.hasDiscountObservable.subscribeVisibility(strikeThroughPricePerNight)
            viewModel.hasDiscountObservable.subscribeVisibility(discountPercentage)
            viewModel.distanceFromCurrentLocationObservable.subscribe(hotelAmenityOrDistanceFromLocation)
            viewModel.topAmenityVisibilityObservable.subscribeVisibility(topAmenityTitle)
            viewModel.topAmenityTitleObservable.subscribe(topAmenityTitle)
            viewModel.urgencyIconObservable.subscribe(urgencyIcon)
            viewModel.urgencyMessageVisibilityObservable.subscribeVisibility(urgencyMessageContainer)
            viewModel.urgencyMessageBackgroundObservable.subscribeBackgroundColor(urgencyMessageContainer)
            viewModel.urgencyMessageBoxObservable.subscribe(urgencyMessageBox)
            viewModel.vipMessageVisibilityObservable.subscribeVisibility(vipMessage)
            viewModel.vipMessageVisibilityObservable.subscribeVisibility(gradientTop)
            viewModel.airAttachVisibilityObservable.subscribeVisibility(airAttachContainer)
            viewModel.hotelDiscountPercentageObservable.subscribe(airAttachDiscount)

            viewModel.hotelStarRatingObservable.subscribe {
                starRating.rating = it
            }
            viewModel.adImpressionObservable.subscribe {
                AdImpressionTracking.trackAdClickOrImpression(itemView.context, it, null)
            }
        }

        override fun onClick(view: View) {
            val hotel: Hotel = getHotel(adapterPosition)
            hotelSelectedSubject.onNext(hotel)
            if (hotel.isSponsoredListing) AdImpressionTracking.trackAdClickOrImpression(itemView.context, hotel.clickTrackingUrl, null)
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

        private val target = object : PicassoTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                super.onBitmapLoaded(bitmap, from)

                val palette = Palette.generate(bitmap)
                val color = palette.getVibrantColor(R.color.transparent_dark)

                val fullColorBuilder = ColorBuilder(color).darkenBy(0.3f);
                val startColor = fullColorBuilder.setAlpha(154).build()
                val endColor = fullColorBuilder.setAlpha(0).build()

                val drawable = HeaderBitmapDrawable()
                drawable.setBitmap(bitmap)
                val colorArrayBottom = intArrayOf(0, endColor,
                        startColor)
                val colorArrayFull = intArrayOf(startColor, endColor,
                        startColor)

                if( vipMessage.getVisibility() == View.VISIBLE ) {
                    drawable.setGradient(colorArrayFull, null)
                }
                else {
                    drawable.setGradient(colorArrayBottom, DEFAULT_GRADIENT_POSITIONS)
                }
                imageView.setImageDrawable(drawable)
                gradientTop.setVisibility(View.VISIBLE)
                gradientBottom.setVisibility(View.VISIBLE)
            }

            override fun onBitmapFailed(errorDrawable: Drawable) {
                super.onBitmapFailed(errorDrawable)
                imageView.setImageDrawable(errorDrawable)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable) {
                super.onPrepareLoad(placeHolderDrawable)
                imageView.setImageDrawable(placeHolderDrawable)
            }
        }

    }

    public inner class HotelResultsPricingStructureHeaderViewHolder(val root: ViewGroup, val vm: HotelResultsPricingStructureHeaderViewModel) : RecyclerView.ViewHolder(root) {
        val pricingStructureHeader: TextView by root.bindView(R.id.pricing_structure_header)

        init {
            vm.pricingStructureHeaderObservable.subscribe(pricingStructureHeader)
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
