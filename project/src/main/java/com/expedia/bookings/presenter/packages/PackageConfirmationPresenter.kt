package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

public class PackageConfirmationPresenter(context: Context, attributeSet: AttributeSet) : Presenter(context, attributeSet) {
    val itinNumber: TextView by bindView(R.id.itin_number)

    init {
        View.inflate(context, R.layout.package_confirmation_presenter, this)
    }
}