package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.bookings.packages.vm.PackageErrorViewModel

class PackageErrorPresenter(context: Context, attr: AttributeSet) : BaseErrorPresenter(context, attr) {

    override fun getViewModel(): PackageErrorViewModel {
        return viewmodel as PackageErrorViewModel
    }
}
