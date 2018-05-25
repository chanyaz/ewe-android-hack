package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.vm.BaseCostSummaryBreakdownViewModel

class PackageCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {

    override fun trackBreakDownClicked() {
        PackagesTracking().trackBundleOverviewCostBreakdownClick()
    }
}
