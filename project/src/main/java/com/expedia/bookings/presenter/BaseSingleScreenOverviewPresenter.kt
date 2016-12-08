package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.CVVEntryWidget

abstract class BaseSingleScreenOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    init {
        inflate()
    }

    abstract fun inflate()

}