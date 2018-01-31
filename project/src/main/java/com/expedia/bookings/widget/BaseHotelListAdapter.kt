package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelResultsPricingStructureHeaderViewModel
import com.mobiata.android.util.AndroidUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import android.text.SpannableStringBuilder
import android.text.Spannable
import android.text.style.ImageSpan
import com.expedia.bookings.data.pos.PointOfSale

abstract class BaseHotelListAdapter(val hotelSelectedSubject: PublishSubject<Hotel>,
                                    val headerSubject: PublishSubject<Unit>,
                                    val pricingHeaderSelectedSubject: PublishSubject<Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun getHotelCellHolder(parent: ViewGroup): AbstractHotelCellViewHolder
    abstract fun getPriceDescriptorMessageIdForHSR(context: Context): Int?

    val MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW = 0
    val PRICING_STRUCTURE_HEADER_VIEW = 1
    val HOTEL_VIEW = 2
    val LOADING_VIEW = 3
    val END_OF_LIST = 4
    val FILTER_PROMPT = 15

    val allViewsLoadedTimeObservable = PublishSubject.create<Unit>()

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val resultsSubject = BehaviorSubject.create<HotelSearchResponse>()
    val filterPromptSubject = PublishSubject.create<Unit>()

    val hotelSoldOut = endlessObserver<String> { soldOutHotelId ->
        hotels.firstOrNull { it.hotelId == soldOutHotelId }?.isSoldOut = true
        hotelListItemsMetadata.firstOrNull { it.hotelId == soldOutHotelId }?.hotelSoldOut?.onNext(true)
    }

    private data class HotelListItemMetadata(val hotelId: String, val hotelSoldOut: BehaviorSubject<Boolean>)

    private val hotelListItemsMetadata: MutableList<HotelListItemMetadata> = ArrayList()

    private var hotels: ArrayList<Hotel> = ArrayList()

    private fun getHotel(rawAdapterPosition: Int): Hotel {
        return hotels[rawAdapterPosition - numHeaderItemsInHotelsList()]
    }

    private var newResultsConsumed = false
    private var pinnedSearch = false

    init {
        resultsSubject.subscribe { response ->
            pinnedSearch = response.hasPinnedHotel()
            loading = false
            hotels = ArrayList(response.hotelList)
            hotelListItemsMetadata.clear()
            newResultsConsumed = false
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
        if (ExpediaBookingApp.isDeviceShitty())
            hotels = arrayListOf(Hotel(), Hotel(), Hotel())
        else hotels = arrayListOf(Hotel(), Hotel())
        notifyDataSetChanged()
    }

    fun numHeaderItemsInHotelsList(): Int {
        return 2
    }

    override fun getItemCount(): Int {
        if (hotels.size != 1) {
            return hotels.size + numHeaderItemsInHotelsList() + 1
        }
        return hotels.size + numHeaderItemsInHotelsList()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW
        } else if (position == 1) {
            return PRICING_STRUCTURE_HEADER_VIEW
        } else if (loading) {
            return LOADING_VIEW
        } else if (position == hotels.size + numHeaderItemsInHotelsList()) {
            return END_OF_LIST
        } else {
            return HOTEL_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fixedPosition = position - numHeaderItemsInHotelsList()
        when (holder) {
            is AbstractHotelCellViewHolder -> {
                val hotel = hotels[fixedPosition]
                holder.bindHotelData(hotel)
                hotelListItemsMetadata.add(HotelListItemMetadata(holder.hotelId, holder.viewModel.soldOut))
                if (!newResultsConsumed) {
                    newResultsConsumed = true
                    allViewsLoadedTimeObservable.onNext(Unit)
                }
                holder.markPinned(pinnedSearch && fixedPosition == 0)
                if (fixedPosition == FILTER_PROMPT) {
                    filterPromptSubject.onNext(Unit)
                }
            }
            is LoadingViewHolder -> holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView, fixedPosition % 2 == 0))
        }
    }

    private fun hotelSelected(context: Context, adapterPosition: Int) {
        val hotel: Hotel = getHotel(adapterPosition)
        hotelSelectedSubject.onNext(hotel)
        if (hotel.isSponsoredListing) {
            AdImpressionTracking.trackAdClickOrImpression(context, hotel.clickTrackingUrl, null)
            HotelTracking.trackHotelSponsoredListingClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW) {
            val header = View(parent.context)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (isHideMiniMapOnResultBucketed(parent.context) || ExpediaBookingApp.isAutomation() || ExpediaBookingApp.isDeviceShitty()) {
                lp.height = 0
            } else {
                lp.height = AndroidUtils.getScreenSize(parent.context).y
            }
            header.layoutParams = lp
            return MapSwitchClickInterceptorTransparentHeaderViewHolder(header)
        } else if (viewType == LOADING_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_loading_cell, parent, false)
            val loadingViewHolder = HotelLoadingViewHolder(view)
            return loadingViewHolder
        } else if (viewType == PRICING_STRUCTURE_HEADER_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_results_header_cell, parent, false)
            val vm = HotelResultsPricingStructureHeaderViewModel(parent.context, getPriceDescriptorMessageIdForHSR(parent.context))
            loadingSubject.subscribe(vm.loadingStartedObserver)
            resultsSubject.subscribe(vm.resultsDeliveredObserver)
            val holder = HotelResultsPricingStructureHeaderViewHolder(view as ViewGroup, vm)
            return holder
        } else if (viewType == END_OF_LIST) {
            val header = View(parent.context)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.height = parent.context.resources.getDimensionPixelSize(R.dimen.hotel_results_last_price_buffer)
            header.layoutParams = lp
            return EndOfListViewHolder(header)
        } else {
            val holder: AbstractHotelCellViewHolder = getHotelCellHolder(parent)
            holder.hotelClickedSubject.subscribe { position ->
                hotelSelected(holder.itemView.context, position)
            }
            return holder
        }
    }

    private fun isHideMiniMapOnResultBucketed(context: Context): Boolean =
            AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.HotelHideMiniMapOnResult)

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == LOADING_VIEW) {
            (holder as LoadingViewHolder).cancelAnimation()
        } else if (holder.itemViewType == HOTEL_VIEW) {
            val hotelItemIndex = hotelListItemsMetadata.indexOfFirst {
                it.hotelId == (holder as AbstractHotelCellViewHolder).hotelId
            }
            if (hotelItemIndex != -1) {
                hotelListItemsMetadata.removeAt(hotelItemIndex)
            }
        }
        super.onViewRecycled(holder)
    }

    inner class HotelResultsPricingStructureHeaderViewHolder(val root: ViewGroup, val vm: HotelResultsPricingStructureHeaderViewModel) : RecyclerView.ViewHolder(root) {
        val resultsDescriptionHeader: TextView by bindView(R.id.results_description_header)
        val loyaltyPointsAppliedHeader: TextView by bindView(R.id.loyalty_points_applied_message)
        val shadow: View by bindView(R.id.drop_shadow)

        init {
            if (isHideMiniMapOnResultBucketed(root.context) || ExpediaBookingApp.isDeviceShitty()) {
                shadow.visibility = View.GONE
            }

            val faqUrl = PointOfSale.getPointOfSale().hotelsResultsSortFaqUrl
            vm.resultsDescriptionHeaderObservable.subscribe { resultsDescription ->
                val resultDescriptionSpannable = SpannableStringBuilder(HtmlCompat.fromHtml(resultsDescription))
                if (faqUrl.isNotEmpty() && !resultDescriptionSpannable.toString().equals(root.context.resources.getString(R.string.progress_searching_hotels_hundreds))) {
                    resultDescriptionSpannable.append("  ")
                    resultDescriptionSpannable.setSpan(ImageSpan(root.context, R.drawable.details_info), resultDescriptionSpannable.length - 1, resultDescriptionSpannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                resultsDescriptionHeader.setText(resultDescriptionSpannable, android.widget.TextView.BufferType.SPANNABLE)
            }
            vm.loyaltyAvailableObservable.subscribeVisibility(loyaltyPointsAppliedHeader)

            vm.sortFaqLinkAvailableObservable.subscribe { faqLinkAvailable ->
                if (faqLinkAvailable) {
                    resultsDescriptionHeader.setOnClickListener {
                        pricingHeaderSelectedSubject.onNext(Unit)
                    }
                } else {
                    resultsDescriptionHeader.setOnClickListener(null)
                }
            }
        }
    }

    inner class MapSwitchClickInterceptorTransparentHeaderViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        init {
            itemView.setOnClickListener {
                headerSubject.onNext(Unit)
            }
            itemView.contentDescription = root.context.getString(R.string.hotel_results_map_view_cont_desc)
        }
    }

    inner class EndOfListViewHolder(root: View) : RecyclerView.ViewHolder(root)
}
