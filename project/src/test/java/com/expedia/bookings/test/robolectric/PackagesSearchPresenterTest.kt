package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.packages.PackageSearchPresenter
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerPickerView
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackagesSearchPresenterTest {
    lateinit private var widget: PackageSearchPresenter
    lateinit private var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_packages_search_presenter,
                null) as PackageSearchPresenter
    }

    @Test
    fun testNewTravelerPickerSelectionOperations(){
        setUpPackagesTravelerRevamp(true)

        var travelerCard = widget.travelerWidgetV2
        travelerCard.performClick()
        var view = travelerCard.travelerDialogView
        var travelerPicker = view.findViewById(R.id.flight_traveler_view) as FlightTravelerPickerView

        travelerPicker.youthCountSelector.travelerPlus.performClick()
        travelerPicker.childCountSelector.travelerPlus.performClick()
        travelerPicker.infantCountSelector.travelerPlus.performClick()

        travelerPicker.getViewModel().showSeatingPreference = true
        travelerPicker.infantInLap.isChecked = true

        assertEquals("[16, 10, 1]", travelerPicker.viewmodel.travelerParamsObservable.value.childrenAges.toString())
        assertEquals("1",travelerPicker.viewmodel.travelerParamsObservable.value.numberOfAdults.toString())
        assertEquals("4",travelerPicker.viewmodel.travelerParamsObservable.value.getTravelerCount().toString())

        assertTrue(travelerPicker.viewmodel.isInfantInLapObservable.value)

        travelerPicker.infantCountSelector.travelerMinus.performClick()
        assertEquals("[16, 10]", travelerPicker.viewmodel.travelerParamsObservable.value.childrenAges.toString())
        assertEquals("3",travelerPicker.viewmodel.travelerParamsObservable.value.getTravelerCount().toString())

        travelerPicker.adultCountSelector.travelerPlus.performClick()
        assertEquals("2",travelerPicker.viewmodel.travelerParamsObservable.value.numberOfAdults.toString())

    }

    @Test
    fun testNewTravelerPickerWidgetItemsVisiblity() {
        setUpPackagesTravelerRevamp(true)

        var travelerCard = widget.travelerWidgetV2
        travelerCard.performClick()
        var view = travelerCard.travelerDialogView
        var travelerPicker = view.findViewById(R.id.flight_traveler_view) as FlightTravelerPickerView

        assertEquals(View.VISIBLE, travelerPicker.visibility)
        assertEquals(View.VISIBLE, travelerPicker.adultCountSelector.visibility)
        assertEquals(View.VISIBLE, travelerPicker.youthCountSelector.visibility)
        assertEquals(View.VISIBLE, travelerPicker.childCountSelector.visibility)
        assertEquals(View.VISIBLE, travelerPicker.infantCountSelector.visibility)

        travelerPicker.infantCountSelector.travelerPlus.performClick()
        assertEquals(View.VISIBLE, travelerPicker.infantInLap.visibility)
    }

    private fun setUpPackagesTravelerRevamp(isUserBucketed: Boolean) {
        SettingUtils.save(activity, R.string.preference_flight_traveler_form_revamp, true)
        if (isUserBucketed) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        } else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        }
        Ui.getApplication(activity).defaultPackageComponents()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_packages_search_presenter,
                null) as PackageSearchPresenter
    }


}