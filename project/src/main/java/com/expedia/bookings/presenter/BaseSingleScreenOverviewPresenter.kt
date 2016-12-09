package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.CVVEntryWidget

abstract class BaseSingleScreenOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

}