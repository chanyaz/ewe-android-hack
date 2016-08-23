package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.hotel.HotelCellViewHolder
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelResultsPricingStructureHeaderViewModel
import com.expedia.vm.hotel.HotelViewModel
import com.mobiata.android.util.AndroidUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

abstract class BaseHotelListAdapter(val hotelSelectedSubject: PublishSubject<Hotel>,
                                    val headerSubject: PublishSubject<Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder
    abstract fun getHotelCellViewModel(context: Context, hotel: Hotel): HotelViewModel

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
        if (ExpediaBookingApp.isDeviceShitty())
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
            is HotelCellViewHolder -> {
                val viewModel = getHotelCellViewModel(holder.itemView.context, hotels.get(fixedPosition))
                hotelListItemsMetadata.add(HotelListItemMetadata(viewModel.hotelId, viewModel.soldOut))
                holder.bind(viewModel)
            }
            is LoadingViewHolder -> holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView, fixedPosition % 2 == 0))
        }
    }

    private fun hotelSelected(context: Context, adapterPosition: Int) {
        val hotel: Hotel = getHotel(adapterPosition)
        hotelSelectedSubject.onNext(hotel)
        if (hotel.isSponsoredListing) {
            AdImpressionTracking.trackAdClickOrImpression(context, hotel.clickTrackingUrl, null)
            HotelTracking().trackHotelSponsoredListingClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == MAP_SWITCH_CLICK_INTERCEPTOR_TRANSPARENT_HEADER_VIEW) {
            val header = View(parent.context)
            var lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.height = if (ExpediaBookingApp.isAutomation() || ExpediaBookingApp.isDeviceShitty()) 0 else AndroidUtils.getScreenSize(parent.context).y
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
            val holder: HotelCellViewHolder = getHotelCellHolder(parent)
            holder.hotelClickedSubject.subscribe { position ->
                hotelSelected(holder.itemView.context, position)
            }
            return holder
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == LOADING_VIEW) {
            (holder as LoadingViewHolder).cancelAnimation()
        } else if (holder.itemViewType == HOTEL_VIEW) {
            val hotelItemIndex = hotelListItemsMetadata.indexOfFirst {
                it.hotelId == (holder as HotelCellViewHolder).hotelId
            }
            if (hotelItemIndex != -1) {
                hotelListItemsMetadata.removeAt(hotelItemIndex)
            }
        }
        super.onViewRecycled(holder)
    }

    inner class HotelResultsPricingStructureHeaderViewHolder(val root: ViewGroup, val vm: HotelResultsPricingStructureHeaderViewModel) : RecyclerView.ViewHolder(root) {
        val pricingStructureHeader: TextView by root.bindView(R.id.pricing_structure_header)
        val loyaltyPointsAppliedHeader: TextView by root.bindView(R.id.loyalty_points_applied_message)
        val shadow: View by root.bindView(R.id.drop_shadow)

        init {
            if (ExpediaBookingApp.isDeviceShitty()) {
                shadow.visibility = View.GONE
            }
            vm.pricingStructureHeaderObservable.subscribeText(pricingStructureHeader)
            vm.loyaltyAvailableObservable.subscribeVisibility(loyaltyPointsAppliedHeader)
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
}
