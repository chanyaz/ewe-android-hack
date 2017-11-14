package com.expedia.bookings.widget.flights

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FareFamilyAmenitiesDialog
import com.expedia.bookings.widget.FareFamilyPrimaryAmenitiesWidget
import com.expedia.bookings.widget.TextView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeInverseVisibilityInvisible
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextAndVisibilityInvisible
import com.expedia.util.subscribeVisibility
import com.expedia.vm.flights.FareFamilyItemViewModel
import com.expedia.vm.flights.FareFamilyPrimaryAmenitiesWidgetViewModel
import com.expedia.vm.flights.FlightFareFamilyAmenityDialogViewModel
import rx.Observer
import java.util.Locale

class FareFamilyItemWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val fareFamilyRadioButton: RadioButton by bindView(R.id.fare_family_radio_button)
    val fareFamilyClassHeader: LinearLayout by bindView(R.id.fare_family_class_header)
    val fareFamilyClassTitle: TextView by bindView(R.id.fare_family_class_title)
    val priceDelta: TextView by bindView(R.id.fare_family_class_price_delta)
    val fareFamilyCabinClass: TextView by bindView(R.id.fare_family_class_subtitle)
    val fareFamilyPrimaryAmenitiesWidget: FareFamilyPrimaryAmenitiesWidget by bindView(R.id.fare_family_primary_amenities_widget)
    val showMoreContainer: View by bindView(R.id.fare_family_show_more_container)
    val fareFamilyDivider: View by bindView(R.id.fare_family_divider)

    val roundTrip: TextView by bindView(R.id.fare_family_class_roundtrip_text)
    val travelerTextView: TextView by bindView(R.id.fare_family_traveler_text)
    var viewModel: FareFamilyItemViewModel? = null

    val fareFamilyAmenitiesDialogView: FareFamilyAmenitiesDialog by lazy {
        val fareFamilyAmenitiesDialog = LayoutInflater.from(context).inflate(R.layout.fare_family_amenities_dialog_view, null) as FareFamilyAmenitiesDialog
        fareFamilyAmenitiesDialog.viewModel = FlightFareFamilyAmenityDialogViewModel(
                context, viewModel?.fareFamilyDetail!!.fareFamilyComponents, viewModel?.fareFamilyDetail?.totalPrice!!.currencyCode)
        fareFamilyAmenitiesDialog.prepareAmenitiesListForDisplay()
        fareFamilyAmenitiesDialog
    }

    val fareFamilyAmenitiesDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        builder.setView(fareFamilyAmenitiesDialogView)
        builder.setPositiveButton(context.getString(R.string.OK), { dialog, which ->
            dialog.dismiss()
        })
        val dialog: AlertDialog = builder.create()
        dialog.setCancelable(true)
        dialog
    }

    val showMoreClickObserver: Observer<Unit> = endlessObserver {
        fareFamilyAmenitiesDialog.show()
    }

    val clickObserver: Observer<Unit> = endlessObserver {
        viewModel?.radioBtnClickObservable?.onNext(Unit)
        fareFamilyRadioButton.isChecked = true
        this.isSelected = true
        viewModel?.dividerVisibilitySubject?.onNext(true)
    }

    fun bindViewModel(viewModel: FareFamilyItemViewModel) {
        this.viewModel = viewModel
        val fareFamilyName = Strings.capitalize(viewModel.fareFamilyName, Locale.US)
        fareFamilyClassTitle.text = fareFamilyName
        fareFamilyCabinClass.text = viewModel.cabinClass
        priceDelta.text = viewModel.fareDeltaAmount
        viewModel.roundTripObservable.subscribeVisibility(roundTrip)
        viewModel.travelerTextObservable.subscribeTextAndVisibilityInvisible(travelerTextView)
        viewModel.dividerVisibilitySubject.subscribeInverseVisibilityInvisible(fareFamilyDivider)
        fareFamilyRadioButton.isChecked = viewModel.defaultChecked
        this.isSelected = viewModel.defaultChecked
        fareFamilyRadioButton.subscribeOnClick(clickObserver)
        fareFamilyClassHeader.subscribeOnClick(clickObserver)
        fareFamilyPrimaryAmenitiesWidget.viewModel = FareFamilyPrimaryAmenitiesWidgetViewModel(context, viewModel.fareFamilyDetail.fareFamilyComponents)
        viewModel.showMoreVisibilitySubject.subscribeVisibility(showMoreContainer)
        viewModel.setShowMoreVisibility()
        showMoreContainer.subscribeOnClick(showMoreClickObserver)
        fareFamilyAmenitiesDialogView.viewModel.fareFamilyNameSubject.onNext(fareFamilyName)
        fareFamilyAmenitiesDialogView.viewModel.fareFamilyCabinClassNameSubject.onNext(viewModel.cabinClass)
        fareFamilyClassHeader.contentDescription = viewModel.getContentDescription()
    }
}
