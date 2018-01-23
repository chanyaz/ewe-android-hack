package com.expedia.bookings.widget.shared
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class TravelerPickerSpinnersContainer(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val travelerAgesBottomContainer1: View by bindView(R.id.traveler_ages_bottom_container1)
    val travelerAgesBottomContainer2: View by bindView(R.id.traveler_ages_bottom_container2)
    val travelerAgesBottomContainer3: View by bindView(R.id.traveler_ages_bottom_container3)
    val travelerAgesBottomContainer4: View by bindView(R.id.traveler_ages_bottom_container4)

    val ageSpinner1: Spinner by bindView(R.id.traveler_age_spinner_1)
    val ageSpinner2: Spinner by bindView(R.id.traveler_age_spinner_2)
    val ageSpinner3: Spinner by bindView(R.id.traveler_age_spinner_3)
    val ageSpinner4: Spinner by bindView(R.id.traveler_age_spinner_4)
    val ageSpinner5: Spinner by bindView(R.id.traveler_age_spinner_5)
    val ageSpinner6: Spinner by bindView(R.id.traveler_age_spinner_6)
    val ageSpinner7: Spinner by bindView(R.id.traveler_age_spinner_7)
    val ageSpinner8: Spinner by bindView(R.id.traveler_age_spinner_8)

    val ageSpinners by lazy {
        listOf(ageSpinner1, ageSpinner2, ageSpinner3, ageSpinner4, ageSpinner5, ageSpinner6, ageSpinner7, ageSpinner8)
    }

    init {
        View.inflate(context, R.layout.traveler_picker_spinners_container, this)
    }
}
