package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.TravelersViewModel

class FlightTravelersPresenter(context: Context, attrs: AttributeSet) : AbstractTravelersPresenter(context, attrs) {

    val boardingWarning: TextView by bindView(R.id.boarding_warning)
    val materialFormsEnabled = isMaterialFormsEnabled()
    private val selectToEntryFlights = object : SelectToEntryTransition(FlightTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            boardingWarning.visibility = if (forward) View.VISIBLE else View.GONE
            if (materialFormsEnabled) {
                val color = if (forward)  ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.gray1)
                setBackgroundColor(color)
            }
        }
    }

    init {
        addTransition(selectToEntryFlights)
        travelerEntryWidget.nameEntryViewFocused.subscribeVisibility(boardingWarning)
    }

    override fun setUpTravelersViewModel(vm: TravelersViewModel) {
        vm.passportRequired.subscribe(travelerPickerWidget.viewModel.passportRequired)
    }

    override fun inflateTravelersView() {
        View.inflate(context, R.layout.multiple_traveler_presenter, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (materialFormsEnabled) {
            val newParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            newParams.setMargins(0,0,0,0)
            travelerEntryWidget.layoutParams = newParams
        }
    }
}