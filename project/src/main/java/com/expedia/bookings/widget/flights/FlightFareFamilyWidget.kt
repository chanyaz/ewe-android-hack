package com.expedia.bookings.widget.flights

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ScrollView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.flights.FareFamilyItemViewModel
import com.expedia.vm.flights.FareFamilyTotalPriceViewModel
import com.expedia.vm.flights.FlightFareFamilyViewModel
import com.squareup.phrase.Phrase
import java.util.Locale

class FlightFareFamilyWidget(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val toolbar: Toolbar by bindView(R.id.fareFamily_toolbar)
    val fareFamilyContainer: ViewGroup by bindView(R.id.fareFamily_container)
    val fareFamilyRadioGroup: RadioGroup by bindView(R.id.flight_fare_family_radio_group)
    val fareFamilyTripLocation: TextView by bindView(R.id.fare_family_location)
    val fareFamilyTripAirlines: TextView by bindView(R.id.fare_family_airlines)
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.upsell_total_price_widget)
    val scrollViewContainer: ScrollView by bindView(R.id.fareFamilyDetailContainer)
    val inflater = LayoutInflater.from(context)
    var selectedFareFamilyIndex = 0

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
            fareDetailsAndSelectedFareFamily.fareDetails.forEachIndexed { index, fareFamilyDetails ->
                val defaultChecked = fareDetailsAndSelectedFareFamily.selectedFareFamily.fareFamilyCode == fareFamilyDetails.fareFamilyCode
                val fareFamilyItemViewModel = FareFamilyItemViewModel(context, fareFamilyDetails, defaultChecked, vm.roundTripObservable)
                fareFamilyItemViewModel.choosingFareFamilyObservable.subscribe(vm.choosingFareFamilyObservable)

                val fareFamilyItem = inflater.inflate(R.layout.flight_fare_family_item_layout, fareFamilyRadioGroup, false) as FareFamilyItemWidget
                fareFamilyItem.bindViewModel(fareFamilyItemViewModel)
                fareFamilyItemViewModel.travelerTextObservable.onNext(StrUtils.formatMultipleTravelerString(context, Db.getFlightSearchParams().guests))
                vm.airlinesObservable.subscribe(fareFamilyItem.fareFamilyAmenitiesDialogView.viewModel.airlineNameSubject)
                fareFamilyRadioGroup.addView(fareFamilyItem)
                if (defaultChecked) {
                    selectedFareFamilyIndex = index
                }
            }
            fareFamilyRadioGroup.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    fareFamilyRadioGroup.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val yCoordinate = fareFamilyRadioGroup.getChildAt(selectedFareFamilyIndex).y
                    scrollViewContainer.smoothScrollTo(0, yCoordinate.toInt())
                }
            })
        }
        vm.choosingFareFamilyObservable.subscribe { fareFamilyDetails ->
            clearChecks()
            totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(createTripTotalText(fareFamilyDetails.fareFamilyName))
            totalPriceWidget.viewModel.total.onNext(fareFamilyDetails.totalPrice)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val lp = fareFamilyContainer.layoutParams as LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_flight_fare_family_details, this)
        totalPriceWidget.visibility = View.VISIBLE
        totalPriceWidget.viewModel = FareFamilyTotalPriceViewModel(context)
        toolbar.inflateMenu(R.menu.action_mode_done)
        toolbar.title = resources.getString(R.string.cabin_class)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))
        toolbar.menu.findItem(R.id.menu_done).setActionView(doneButton).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        toolbar.setBackgroundColor(ContextCompat.getColor(this.context, R.color.packages_flight_filter_background_color))
        totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
    }

    fun clearChecks() {
        for (i in 0..fareFamilyRadioGroup.childCount - 1) {
            val v = fareFamilyRadioGroup.getChildAt(i) as FareFamilyItemWidget
            v.fareFamilyRadioButton.isChecked = false
        }
    }

    fun createTripTotalText(fareFamilyName: String): String {
        return (Phrase.from(context, R.string.trip_total_upsell_TEMPLATE)
                .put("farefamily", Strings.capitalize(fareFamilyName, Locale.US))
                .format().toString())
    }
}