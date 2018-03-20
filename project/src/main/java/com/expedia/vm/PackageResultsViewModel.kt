package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled

class PackageResultsViewModel(context: Context) : BaseResultsViewModel() {
    override val doNotOverrideFilterButton = isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }
}
