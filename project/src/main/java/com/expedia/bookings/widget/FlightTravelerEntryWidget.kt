package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.PublishSubject

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val travelerButton: TravelerButton by bindView(R.id.traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
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
    }

    init {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
        travelerButton.visibility == View.GONE
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
        return  viewModel.validate()
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
}