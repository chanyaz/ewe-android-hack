package com.expedia.bookings.widget.packages

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.widget.BaseHotelListAdapter
import rx.subjects.PublishSubject

class PackageHotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>, pricingHeaderSelectedSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject, pricingHeaderSelectedSubject) {

    override fun getHotelCellHolder(parent: ViewGroup): PackageHotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_hotel_cell, parent, false)
        return PackageHotelCellViewHolder(view as ViewGroup, parent.width)
    }

    override fun getPriceDescriptorMessageIdForHSR(context: Context): Int? {
        val shouldShowPackageIncludesTaxesMessage = PointOfSale.getPointOfSale().supportsPackagesHSRIncludesHeader()
        val shouldShowPackageIncludesMessage = PointOfSale.getPointOfSale().supportsPackagesHSRHeader()
        val isBreadcrumbsEnabled = isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)
        if (isBreadcrumbsEnabled) {
            return R.string.package_hotel_results_header
        } else if (shouldShowPackageIncludesTaxesMessage) {
            return R.string.package_hotel_results_includes_header_TEMPLATE
        } else if (shouldShowPackageIncludesMessage) {
            return R.string.package_hotel_results_header_TEMPLATE
        } else
            return null
    }
}