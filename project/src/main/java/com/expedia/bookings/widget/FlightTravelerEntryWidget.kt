package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.PublishSubject

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        TravelerButton.ITravelerButtonListener  {

    val travelerButton: TravelerButton by bindView(R.id.traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val passportCountrySpinner: Spinner by bindView(R.id.passport_country_spinner)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)

    val doneButton: TextView by bindView(R.id.new_traveler_done_button)
    val advancedButton: TextView by bindView(R.id.advanced_options_button)
    val advancedOptionsIcon: ImageView by bindView(R.id.traveler_advanced_options_icon)

    val travelerCompleteSubject = PublishSubject.create<Traveler>()

    var viewModel: TravelerViewModel by notNullAndObservable { vm ->
        nameEntryView.viewModel = vm.nameViewModel
        phoneEntryView.viewModel = vm.phoneViewModel
        tsaEntryView.viewModel = vm.tsaViewModel
        advancedOptionsWidget.viewModel = vm.advancedOptionsViewModel

        vm.passportCountrySubject.subscribe { countryCode ->
            val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
            val position = if (countryCode.isNullOrEmpty()) adapter.defaultLocalePosition else adapter.getPositionByCountryThreeLetterCode(countryCode)
            passportCountrySpinner.setSelection(position)
        }
        vm.showPassportCountryObservable.subscribeVisibility(passportCountrySpinner)
    }

    init {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
        travelerButton.visibility == View.GONE

        val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                R.layout.material_spinner_item, R.layout.spinner_dropdown_item, false)
        adapter.setPrefix(context.getString(R.string.passport_country_colon))
        adapter.setColoredPrefix(false)

        passportCountrySpinner.adapter = adapter
        passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
        travelerButton.setTravelButtonListener(this)
    }

    override fun onTravelerChosen(traveler: Traveler) {
        viewModel.updateTraveler(traveler)
    }

    override fun onAddNewTravelerSelected() {
        // Adding for packages mvp
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        doneButton.setOnClickListener {
            if (isValid()) {
                travelerCompleteSubject.onNext(viewModel.getTraveler())
            }
        }

        advancedButton.setOnClickListener {
            if (advancedOptionsWidget.visibility == Presenter.GONE) {
                showAdvancedOptions()
            } else {
                hideAdvancedOptions()
            }
        }
    }

    private fun isValid(): Boolean {
        return viewModel.validate()
    }

    private fun showAdvancedOptions() {
        advancedOptionsWidget.visibility = Presenter.VISIBLE
        AnimUtils.rotate(advancedOptionsIcon)
    }

    private fun hideAdvancedOptions() {
        advancedOptionsWidget.visibility = Presenter.GONE
        AnimUtils.reverseRotate(advancedOptionsIcon)
        advancedOptionsIcon.clearAnimation()
    }

    private inner class CountryItemSelectedListener() : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
            viewModel.passportCountryObserver.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
        }
    }
}