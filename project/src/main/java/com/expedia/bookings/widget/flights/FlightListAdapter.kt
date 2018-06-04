package com.expedia.bookings.widget.flights

import android.content.Context
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.ui.FlightActivity
import com.expedia.vm.flights.FlightViewModel
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class FlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, val isRoundTripSearchSubject: BehaviorSubject<Boolean>,
                             val isOutboundSearch: Boolean, val flightCabinClassSubject: BehaviorSubject<String>,
                             val nonStopSearchFilterAppliedSubject: BehaviorSubject<Boolean>, val refundableFilterAppliedSearchSubject: BehaviorSubject<Boolean> )
                            : AbstractFlightListAdapter(context, flightSelectedSubject, isRoundTripSearchSubject) {

    val ScrollDepth1 = 25
    val ScrollDepth2 = 60
    val ScrollDepth3 = 90
    lateinit var scrollDepthMap: HashMap<Int, Int>
    val trackScrollDepthSubject = PublishSubject.create<Int>()

    override fun adjustPosition(): Int {
        isCrossSellPackageOnFSR = showCrossSellPackageBannerCell()
        val allHeadersCount = (if (showAllFlightsHeader()) 2 else 1)
        return (if (isCrossSellPackageOnFSR) (allHeadersCount + 1) else allHeadersCount)
    }

    override fun getPriceDescriptorMessageIdForFSR(): Int? {
        return null
    }

    override fun showAllFlightsHeader(): Boolean {
        return false
    }

    override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): FlightViewModel {
        return FlightViewModel(context, flightLeg, isOutboundSearch)
    }

    override fun getRoundTripStringResourceId(): Int {
        if (!isOutboundSearch) {
            return R.string.delta_price_roundtrip_inbound_label
        } else if (PointOfSale.getPointOfSale().shouldAdjustPricingMessagingForAirlinePaymentMethodFee()) {
            return R.string.prices_roundtrip_minimum_label
        } else {
            return R.string.prices_roundtrip_label
        }
    }

    private fun shouldShowCrossSellPackageBanner() = (PointOfSale.getPointOfSale().isCrossSellPackageOnFSR &&
            AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR) &&
            (context as FlightActivity).intent.getStringExtra(Codes.SEARCH_PARAMS)?.isEmpty() ?: true)

    private fun showCrossSellPackageBannerCell(): Boolean {
        return (shouldShowCrossSellPackageBanner() && isRoundTripSearchSubject.value && isOutboundSearch &&
                (flightCabinClassSubject.value == FlightServiceClassType.CabinCode.COACH.name))
    }

    override fun showAdvanceSearchFilterHeader(): Boolean {
        return !PointOfSale.getPointOfSale().hideAdvancedSearchOnFlights() &&
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightAdvanceSearch)
    }

    override fun isShowOnlyNonStopSearch(): Boolean {
        return if (nonStopSearchFilterAppliedSubject.value != null) nonStopSearchFilterAppliedSubject.value else false
    }

    override fun isShowOnlyRefundableSearch(): Boolean {
        return if (refundableFilterAppliedSearchSubject.value != null) refundableFilterAppliedSearchSubject.value else false
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is FlightViewHolder && shouldTrackScrollDepth()) {
            val scrolledPosition = position - adjustPosition()
            scrollDepthMap[scrolledPosition]?.let {
                trackScrollDepthSubject.onNext(it)
                scrollDepthMap.remove(scrolledPosition)
            }
        }
    }

    fun initializeScrollDepthMap() {
        scrollDepthMap = HashMap<Int, Int>()
        scrollDepthMap.put(findScrolledPosition(ScrollDepth1), ScrollDepth1)
        scrollDepthMap.put(findScrolledPosition(ScrollDepth2), ScrollDepth2)
        scrollDepthMap.put(findScrolledPosition(ScrollDepth3), ScrollDepth3)
    }

    private fun findScrolledPosition(percentage: Int): Int {
        return (percentage * flights.size) / 100
    }

    private fun shouldTrackScrollDepth(): Boolean {
        return scrollDepthMap.isNotEmpty()
    }
}
