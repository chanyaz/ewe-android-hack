package com.expedia.bookings.presenter.hotel

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.vm.DatedSearchViewModel
import org.joda.time.LocalDate

abstract class BaseHotelSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    abstract fun getSearchViewModel() : DatedSearchViewModel

    open fun animationStart(forward: Boolean) {
    }

    open fun animationUpdate(f: Float, forward: Boolean) {
    }

    open fun animationFinalize(forward: Boolean) {
    }

    // will be used only v2 search screen
    open fun showSuggestionState() {

    }

    abstract fun selectTravelers(params: TravelerParams)
    abstract fun selectDates(startDate: LocalDate?, endDate: LocalDate?)

    val maxHotelStay = context.resources.getInteger(R.integer.calendar_max_days_hotel_stay)
    val maxHotelStayDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.search_error)
        builder.setMessage(context.getString(R.string.hotel_search_range_error_TEMPLATE, maxHotelStay))
        builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        })
        builder.create()
    }

}
