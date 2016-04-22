package com.expedia.bookings.presenter.hotel

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ShopWithPointsWidget
import com.expedia.vm.BaseSearchViewModel
import org.joda.time.LocalDate

abstract class BaseHotelSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {
    val shopWithPointsWidget: ShopWithPointsWidget by bindView(R.id.widget_points_details)

    abstract fun getSearchViewModel() : BaseSearchViewModel

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

    fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.search_error)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        })
        val dialog = builder.create()
        dialog.show()
    }
}
