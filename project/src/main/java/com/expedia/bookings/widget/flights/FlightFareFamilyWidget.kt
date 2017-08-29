package com.expedia.bookings.widget.flights

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.flights.FareFamilyItemViewModel
import com.expedia.vm.flights.FlightFareFamilyViewModel

class FlightFareFamilyWidget(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val toolbar: Toolbar by bindView(R.id.fareFamily_toolbar)
    val fareFamilyContainer: ViewGroup by bindView(R.id.fareFamily_container)
    val fareFamilyRadioGroup: RadioGroup by bindView(R.id.flight_fare_family_radio_group)
    val fareFamilyTripLocation: TextView by bindView(R.id.fare_family_location)
    val fareFamilyTripAirlines: TextView by bindView(R.id.fare_family_airlines)
    val inflater = LayoutInflater.from(context)

    val doneButton: Button by lazy {
        val button = inflater.inflate(R.layout.toolbar_checkmark_item, null) as Button
        button.setTextColor(ContextCompat.getColor(context, R.color.flight_cabin_class_text))
        button.setText(R.string.done)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.flight_cabin_class_text), PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        button.setOnClickListener{
            viewModel.doneButtonObservable.onNext(Unit)
            (context as Activity).onBackPressed()
        }
        button
    }


    var viewModel: FlightFareFamilyViewModel by notNullAndObservable { vm ->

        vm.fareFamilyTripLocationObservable.subscribeTextAndVisibility(fareFamilyTripLocation)
        vm.airlinesObservable.subscribeTextAndVisibility(fareFamilyTripAirlines)
        vm.fareFamilyDetailsObservable.withLatestFrom(vm.selectedFareFamilyObservable, {
            fareDetails, selectedFareFamily ->
            object {
                val fareDetails = fareDetails
                val selectedFareFamily = selectedFareFamily
            }
        }).subscribe { fareDetailsAndSelectedFareFamily ->
            fareFamilyRadioGroup.removeAllViews()
            fareDetailsAndSelectedFareFamily.fareDetails.forEach {
                val defaultChecked = fareDetailsAndSelectedFareFamily.selectedFareFamily.fareFamilyCode == it.fareFamilyCode
                val fareFamilyItemViewModel = FareFamilyItemViewModel(context, it, defaultChecked, vm.roundTripObservable)
                fareFamilyItemViewModel.choosingFareFamilyObservable.subscribe(vm.choosingFareFamilyObservable)

                val fareFamilyItem = inflater.inflate(R.layout.flight_fare_family_item_layout, fareFamilyRadioGroup, false) as FareFamilyItemWidget
                fareFamilyItem.bindViewModel(fareFamilyItemViewModel)
                fareFamilyRadioGroup.addView(fareFamilyItem)
            }

        }
        vm.choosingFareFamilyObservable.subscribe {
            clearChecks()
        }

    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            var lp = fareFamilyContainer.layoutParams as LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_flight_fare_family_details, this)

        toolbar.inflateMenu(R.menu.action_mode_done)
        toolbar.title = resources.getString(R.string.cabin_class)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))
        toolbar.menu.findItem(R.id.menu_done).setActionView(doneButton).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        toolbar.setBackgroundColor(ContextCompat.getColor(this.context, R.color.packages_flight_filter_background_color))


    }

    fun clearChecks() {
        for (i in 0..fareFamilyRadioGroup.childCount - 1) {
            val v = fareFamilyRadioGroup.getChildAt(i) as FareFamilyItemWidget
            v.fareFamilyRadioButton.isChecked = false
        }
    }

}