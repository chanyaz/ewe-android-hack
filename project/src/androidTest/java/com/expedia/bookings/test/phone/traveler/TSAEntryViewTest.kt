package com.expedia.bookings.test.phone.traveler

import android.support.test.rule.UiThreadTestRule
import android.support.test.runner.AndroidJUnit4
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.rules.PlaygroundRule
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.properties.Delegates

@RunWith(AndroidJUnit4::class)
class TSAEntryViewTest {
    var tsaEntryView: TSAEntryView by Delegates.notNull()

    @Rule @JvmField
    var uiThreadTestRule = UiThreadTestRule()

    @Rule @JvmField
    var activityTestRule = PlaygroundRule(R.layout.test_tsa_entry_view, R.style.V2_Theme_Packages)

    @Test
    fun birthDateErrorState() {
        tsaEntryView = activityTestRule.root as TSAEntryView
        val tsaVM = TravelerTSAViewModel(activityTestRule.activity)
        tsaVM.updateTraveler(Traveler())

        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel = tsaVM
            tsaVM.dateOfBirthErrorSubject.onNext(true)
        }

        //test for accessibility content description
        assertEquals(tsaEntryView.dateOfBirth.errorContDesc, "Error")

    }
}
