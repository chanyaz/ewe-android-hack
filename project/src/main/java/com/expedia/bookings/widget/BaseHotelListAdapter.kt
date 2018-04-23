package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.data.HotelAdapterItem
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.HotelResultsPricingStructureHeaderViewModel
import com.mobiata.android.util.AndroidUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import android.text.SpannableStringBuilder
import android.text.Spannable
import android.text.style.ImageSpan
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.features.Features
import com.expedia.bookings.hotel.widget.Earn2xCardViewHolder
import com.expedia.bookings.hotel.widget.HotelUrgencyViewHolder
import com.expedia.bookings.hotel.widget.GenericAttachViewHolder
import com.expedia.bookings.utils.isHideMiniMapOnResultBucketed
import com.squareup.phrase.Phrase

abstract class BaseHotelListAdapter(val hotelSelectedSubject: PublishSubject<Hotel>,
                                    val headerSubject: PublishSubject<Unit>,
                                    val pricingHeaderSelectedSubject: PublishSubject<Unit>,
                                    var canShow2xMessaging: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun getHotelCellHolder(parent: ViewGroup): AbstractHotelCellViewHolder
    abstract fun getPriceDescriptorMessageIdForHSR(context: Context): Int?
    abstract fun getHeaderTopPadding(context: Context, currentPadding: Int): Int

    var firstHotelIndex = 0
    val FILTER_PROMPT_POSITION = 15
    val URGENCY_POSITION = 4
    val GENERIC_ATTACH_POSITION = 1

    val allViewsLoadedTimeObservable = PublishSubject.create<Unit>()

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val resultsSubject = BehaviorSubject.create<HotelSearchResponse>()
    val filterPromptSubject = PublishSubject.create<Unit>()

    val hotelSoldOut = endlessObserver<String> { soldOutHotelId ->
        data.forEach { item ->
            if (item is HotelAdapterItem.Hotel && item.hotel.hotelId == soldOutHotelId) {
                item.hotel.isSoldOut = true
            }
        }
        hotelListItemsMetadata.firstOrNull { it.hotelId == soldOutHotelId }?.hotelSoldOut?.onNext(true)
    }

    private var data = ArrayList<HotelAdapterItem>()
    private var loadingList = listOf<HotelAdapterItem>(HotelAdapterItem.TransparentMapView(),
            HotelAdapterItem.Header(), HotelAdapterItem.Loading(),
            HotelAdapterItem.Loading(), HotelAdapterItem.Loading())

    private data class HotelListItemMetadata(val hotelId: String, val hotelSoldOut: BehaviorSubject<Boolean>)

    private val hotelListItemsMetadata: MutableList<HotelListItemMetadata> = ArrayList()

    private var compressionMessage: String? = null
    private var newResultsConsumed = false
    private var pinnedSearch = false
    private var isGenericAttachedEnabled = Features.all.genericAttach.enabled()
    private var isAirAttached = false

    init {
        resultsSubject.subscribe { response ->
            pinnedSearch = response.hasPinnedHotel()
            loading = false
            resetData()
            firstHotelIndex = data.size
            response.hotelList.forEach { hotel ->
                data.add(HotelAdapterItem.Hotel(hotel))
                if (hotel.lowRateInfo?.airAttached ?: false) {
                    isAirAttached = true
                }
            }
            data.add(HotelAdapterItem.Spacer())
            compressionMessage?.let { message -> insertUrgency(HotelAdapterItem.Urgency(message)) }
            if (isAirAttached && isGenericAttachedEnabled) insertGenericAttach()
            hotelListItemsMetadata.clear()
            newResultsConsumed = false
            notifyDataSetChanged()
        }
        loadingSubject.subscribe {
            loading = true
        }
    }

    fun addUrgency(compressionMessage: String) {
        clearUrgency()
        this.compressionMessage = compressionMessage
        insertUrgency(HotelAdapterItem.Urgency(compressionMessage))
        notifyItemInserted(firstHotelIndex + URGENCY_POSITION)
    }

    fun clearUrgency() {
        this.compressionMessage = null
        val urgencyIndex = data.indexOfFirst { item -> item is HotelAdapterItem.Urgency }
        if (urgencyIndex != -1) {
            data.removeAt(urgencyIndex)
            notifyItemRemoved(urgencyIndex)
        }
    }

    fun isLoading(): Boolean {
        return loading
    }

    fun showLoading() {
        loadingSubject.onNext(Unit)
        notifyDataSetChanged()
    }

    fun insertGenericAttach() {
        if (firstHotelIndex + GENERIC_ATTACH_POSITION < data.size - 1) {
            data.add(firstHotelIndex + GENERIC_ATTACH_POSITION, HotelAdapterItem.GenericAttached())
        } else {
            data.add(data.size - 1, HotelAdapterItem.GenericAttached())
        }
    }

    private fun insertUrgency(item: HotelAdapterItem.Urgency) {
        if (firstHotelIndex + URGENCY_POSITION < data.size - 1) {
            data.add(firstHotelIndex + URGENCY_POSITION, item)
        } else {
            data.add(data.size - 1, item)
        }
    }

    override fun getItemCount(): Int {
        if (loading) {
            return loadingList.size
        } else {
            return data.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loading) {
            return loadingList[position].key
        } else {
            return data[position].key
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbstractHotelCellViewHolder -> {
                val hotel = (data[position] as HotelAdapterItem.Hotel).hotel
                holder.bindHotelData(hotel)
                hotelListItemsMetadata.add(HotelListItemMetadata(holder.hotelId, holder.viewModel.soldOut))
                if (!newResultsConsumed) {
                    newResultsConsumed = true
                    allViewsLoadedTimeObservable.onNext(Unit)
                }
                holder.markPinned(pinnedSearch && position == firstHotelIndex)
                if (position == (FILTER_PROMPT_POSITION + firstHotelIndex)) {
                    filterPromptSubject.onNext(Unit)
                }
            }
            is HotelUrgencyViewHolder -> {
                val urgencyData = (data[position] as HotelAdapterItem.Urgency)
                holder.bind(urgencyData.compressionMessage)
            }
            is LoadingViewHolder -> holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView,
                    position % 2 == 0))
        }
    }

    private fun hotelSelected(context: Context, adapterPosition: Int) {
        val hotel = (data[adapterPosition] as HotelAdapterItem.Hotel).hotel
        hotelSelectedSubject.onNext(hotel)
        if (hotel.isSponsoredListing) {
            AdImpressionTracking.trackAdClickOrImpression(context, hotel.clickTrackingUrl, null)
            HotelTracking.trackHotelSponsoredListingClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == HotelAdapterItem.TRANSPARENT_MAPVIEW) {
            val header = View(parent.context)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.height = if (isHideMiniMapOnResultBucketed(parent.context) || ExpediaBookingApp.isAutomation() || ExpediaBookingApp.isDeviceShitty()) 0 else AndroidUtils.getScreenSize(parent.context).y
            header.layoutParams = lp
            return MapSwitchClickInterceptorTransparentHeaderViewHolder(header)
        } else if (viewType == HotelAdapterItem.LOADING) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_loading_cell, parent, false)
            val loadingViewHolder = HotelLoadingViewHolder(view)
            return loadingViewHolder
        } else if (viewType == HotelAdapterItem.HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_results_header_cell, parent, false)
            val vm = HotelResultsPricingStructureHeaderViewModel(parent.context, getPriceDescriptorMessageIdForHSR(parent.context))
            loadingSubject.subscribe(vm.loadingStartedObserver)
            resultsSubject.subscribe(vm.resultsDeliveredObserver)
            val holder = HotelResultsPricingStructureHeaderViewHolder(view as ViewGroup, vm)
            return holder
        } else if (viewType == HotelAdapterItem.SPACER) {
            val header = View(parent.context)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (isHideMiniMapOnResultBucketed(parent.context)) {
                lp.height = parent.context.resources.getDimensionPixelSize(R.dimen.hotel_results_last_price_buffer_hide_mini_map)
                header.setBackgroundColor((ContextCompat.getColor(parent.context, R.color.gray100)))
            } else {
                lp.height = parent.context.resources.getDimensionPixelSize(R.dimen.hotel_results_last_price_buffer)
            }
            header.layoutParams = lp
            return EndOfListViewHolder(header)
        } else if (viewType == HotelAdapterItem.URGENCY) {
            return HotelUrgencyViewHolder.create(parent)
        } else if (viewType == HotelAdapterItem.EARN_2X) {
            return Earn2xCardViewHolder.create(parent)
        } else if (viewType == HotelAdapterItem.GENERIC_ATTACHED) {
            return GenericAttachViewHolder.create(parent)
        } else {
            val holder: AbstractHotelCellViewHolder = getHotelCellHolder(parent)
            holder.hotelClickedSubject.subscribe { position ->
                hotelSelected(holder.itemView.context, position)
            }
            return holder
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == HotelAdapterItem.LOADING) {
            (holder as LoadingViewHolder).cancelAnimation()
        } else if (holder.itemViewType == HotelAdapterItem.HOTEL) {
            val hotelItemIndex = hotelListItemsMetadata.indexOfFirst {
                it.hotelId == (holder as AbstractHotelCellViewHolder).hotelId
            }
            if (hotelItemIndex != -1) {
                hotelListItemsMetadata.removeAt(hotelItemIndex)
            }
        }
        super.onViewRecycled(holder)
    }

    private fun resetData() {
        data.clear()
        data.add(HotelAdapterItem.TransparentMapView())
        data.add(HotelAdapterItem.Header())
        if (canShow2xMessaging) {
            data.add(HotelAdapterItem.Earn2x())
        }
    }

    inner class HotelResultsPricingStructureHeaderViewHolder(val root: ViewGroup, val vm: HotelResultsPricingStructureHeaderViewModel) : RecyclerView.ViewHolder(root) {
        val resultsDescriptionContainer: RelativeLayout by bindView(R.id.results_description_container)
        val resultsDescriptionHeader: TextView by bindView(R.id.results_description_header)
        val loyaltyPointsAppliedHeader: TextView by bindView(R.id.loyalty_points_applied_message)
        val shadow: View by bindView(R.id.drop_shadow)

        init {
            if (isHideMiniMapOnResultBucketed(root.context) || ExpediaBookingApp.isDeviceShitty()) {
                shadow.visibility = View.GONE
            }

            val offset = getHeaderTopPadding(root.context, resultsDescriptionContainer.paddingTop)
            if (offset != resultsDescriptionContainer.paddingTop) {
                resultsDescriptionContainer.setPadding(resultsDescriptionContainer.paddingLeft, offset,
                        resultsDescriptionContainer.paddingRight, resultsDescriptionContainer.paddingBottom)
            }

            val faqUrl = PointOfSale.getPointOfSale().hotelsResultsSortFaqUrl
            vm.resultsDescriptionHeaderObservable.subscribe { resultsDescription ->
                val resultDescriptionSpannable = SpannableStringBuilder(HtmlCompat.fromHtml(resultsDescription))
                if (faqUrl.isNotEmpty() && !resultDescriptionSpannable.toString().equals(root.context.resources.getString(R.string.progress_searching_hotels_hundreds))) {
                    resultDescriptionSpannable.append("  ")
                    resultDescriptionSpannable.setSpan(ImageSpan(root.context, R.drawable.details_info), resultDescriptionSpannable.length - 1, resultDescriptionSpannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    resultsDescriptionHeader.contentDescription = Phrase.from(root.context, R.string.accessibility_cont_desc_role_button_TEMPLATE)
                            .put("button_label", resultDescriptionSpannable.toString())
                            .format().toString()
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
