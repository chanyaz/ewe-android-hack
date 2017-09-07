package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.flights.FareFamilyItemViewModel
import rx.Observer
import java.util.Locale

class FareFamilyItemWidget (context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val fareFamilyRadioButton: RadioButton by bindView(R.id.fare_family_radio_button)
    val fareFamilyClassHeader: LinearLayout by bindView(R.id.fare_family_class_header)
    val fareFamilyclassTitle: TextView by bindView(R.id.fare_family_class_title)
    val priceDelta: TextView by bindView(R.id.fare_family_class_price_delta)
    val fareFamilyCabinClass: TextView by bindView(R.id.fare_family_class_subtitle)
    val roundTrip: TextView by bindView(R.id.fare_family_class_roundtrip_text)
    val travellerTextView: TextView by bindView(R.id.fare_family_traveller_text)
    var viewModel: FareFamilyItemViewModel? = null

    val clickObserver: Observer<Unit> = endlessObserver {
        viewModel?.radioBtnClickObservable?.onNext(Unit)
        fareFamilyRadioButton.isChecked = true
    }

    fun bindViewModel(viewModel: FareFamilyItemViewModel) {
        this.viewModel = viewModel
        fareFamilyclassTitle.text = Strings.capitalize(viewModel.fareFamilyName, Locale.US)
        fareFamilyCabinClass.text = viewModel.cabinClass
        priceDelta.text = viewModel.fareDeltaAmount
        viewModel.roundTripObservable.subscribeVisibility(roundTrip)
        travellerTextView.text = viewModel.travellerText
        travellerTextView.visibility = viewModel.travellerTextVisibility
        fareFamilyRadioButton.isChecked = viewModel.defaultChecked
        fareFamilyRadioButton.subscribeOnClick(clickObserver)
        fareFamilyClassHeader.subscribeOnClick(clickObserver)
    }

}
