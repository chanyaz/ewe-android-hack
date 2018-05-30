package com.expedia.bookings.packages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.packages.widget.PackageHotelCellViewHolder
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.utils.isPackagesHSRPriceDisplayEnabled
import com.expedia.bookings.widget.BaseHotelListAdapter
import io.reactivex.subjects.PublishSubject

class PackageHotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>, pricingHeaderSelectedSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject, pricingHeaderSelectedSubject) {

    override fun getHotelCellHolder(parent: ViewGroup): PackageHotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_hotel_cell, parent, false)
        return PackageHotelCellViewHolder(view as ViewGroup)
    }

    override fun getPriceDescriptorMessageIdForHSR(context: Context): Int? {
        if (isPackagesHSRPriceDisplayEnabled(context)) {
            return null
        }

        val shouldShowPackageIncludesTaxesMessage = PointOfSale.getPointOfSale().supportsPackagesHSRIncludesHeader()
        val shouldShowPackageIncludesMessage = PointOfSale.getPointOfSale().supportsPackagesHSRHeader()
        val isBreadcrumbsEnabled = isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)
        if (shouldShowPackageIncludesTaxesMessage) {
            return R.string.package_hotel_results_includes_header_TEMPLATE
        } else if (shouldShowPackageIncludesMessage || isBreadcrumbsEnabled) {
            return R.string.package_hotel_results_header_TEMPLATE
        } else return null
    }

    override fun getHeaderTopPadding(context: Context, currentPadding: Int): Int {
        return currentPadding
    }
}
