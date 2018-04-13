package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.vm.BaseResultsViewModel

class PackageResultsViewModel(context: Context) : BaseResultsViewModel() {
    override val doNotOverrideFilterButton = isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }
}
