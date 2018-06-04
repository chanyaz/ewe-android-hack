package com.expedia.bookings.packages.adapter

import android.content.Context
import android.support.annotation.UiThread
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.bookings.packages.vm.PackageFlightViewModel
import com.expedia.bookings.widget.shared.FlightCellWidget
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class PackageFlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, val isChangePackageSearch: Boolean) : AbstractFlightListAdapter(context, flightSelectedSubject, isRoundTripSearch = true) {

    var shouldShowBestFlight = false

    @UiThread
    override fun setNewFlights(flights: List<FlightLeg>) {
        val newFlights = ArrayList(flights)

        //best flight could be filtered out
        shouldShowBestFlight = !isChangePackageSearch && (newFlights[0]?.isBestFlight ?: false)

        //remove best flight view if there is only 1 flight
        if (shouldShowBestFlight && newFlights.size == 2) {
            shouldShowBestFlight = false
            newFlights.removeAt(0)
        }

        super.setNewFlights(newFlights)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BestFlightViewHolder) {
            holder.bind(PackageFlightViewModel(holder.itemView.context, flights[0]))
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    override fun getRoundTripStringResourceId(): Int {
        if (PointOfSale.getPointOfSale().shouldAdjustPricingMessagingForAirlinePaymentMethodFee())
            return R.string.prices_roundtrip_minimum_label
        else return R.string.prices_roundtrip_label
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ViewTypes.BEST_FLIGHT_VIEW.ordinal -> {
                val view = FlightCellWidget(parent.context)
                return BestFlightViewHolder(view)
            }
            else -> {
                return super.onCreateViewHolder(parent, viewType)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (isLoadingState()) {
            return if (position == 0) ViewTypes.LOADING_FLIGHTS_HEADER_VIEW.ordinal else ViewTypes.LOADING_FLIGHTS_VIEW.ordinal
        }
        if (position == 0) {
            return ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal
        } else if (!shouldShowBestFlight) {
            return ViewTypes.FLIGHT_CELL_VIEW.ordinal
        } else {
            when (position) {
                1 -> return ViewTypes.BEST_FLIGHT_VIEW.ordinal
                2 -> return ViewTypes.ALL_FLIGHTS_HEADER_VIEW.ordinal
                else -> return ViewTypes.FLIGHT_CELL_VIEW.ordinal
            }
        }
    }

    inner class BestFlightViewHolder(root: FlightCellWidget) : FlightViewHolder(root) {
        init {
            flightCell.bestFlightView.visibility = View.VISIBLE
        }

        override fun onClick(view: View) {
            flightSelectedSubject.onNext(flights[0])
        }
    }

    //Two additional views (best flight cell and "All flights" header) when showing best flights
    override fun adjustPosition(): Int {
        return if (shouldShowBestFlight) 3 else 1
    }

    override fun getPriceDescriptorMessageIdForFSR(): Int? {
        val shouldShowPackageIncludesTaxesMessage = PointOfSale.getPointOfSale().supportsPackagesHSRIncludesHeader()
        val shouldShowPackageIncludesMessage = PointOfSale.getPointOfSale().supportsPackagesHSRHeader()
        if (shouldShowPackageIncludesTaxesMessage) {
            return R.string.package_prices_taxes_fees_included_label
        } else if (shouldShowPackageIncludesMessage) {
            return R.string.package_prices_roundtrip_person_minimum_fsr_label
        } else return null
    }

    override fun showAllFlightsHeader(): Boolean {
        return true
    }

    override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): PackageFlightViewModel {
        return PackageFlightViewModel(context, flightLeg)
    }

    override fun showAdvanceSearchFilterHeader(): Boolean {
        return false
    }

    override fun isShowOnlyNonStopSearch(): Boolean {
        return false
    }

    override fun isShowOnlyRefundableSearch(): Boolean {
        return false
    }
}
