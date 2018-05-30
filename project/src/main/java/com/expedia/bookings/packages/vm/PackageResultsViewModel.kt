package com.expedia.bookings.packages.vm

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.vm.BaseResultsViewModel

class PackageResultsViewModel : BaseResultsViewModel() {

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }
}
