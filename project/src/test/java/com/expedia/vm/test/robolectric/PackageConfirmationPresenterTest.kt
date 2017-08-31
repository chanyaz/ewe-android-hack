package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.presenter.packages.PackageConfirmationPresenter
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageConfirmationPresenterTest {

    private var presenter: PackageConfirmationPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(com.expedia.bookings.R.style.V2_Theme_Packages)
        presenter = LayoutInflater.from(activity).inflate(com.expedia.bookings.R.layout.package_confirmation_stub, null) as PackageConfirmationPresenter
    }

    @Test
    fun testShouldShowCarsCrossSellButton() {
        setupCarsCrossSellButton(shouldShow = true)
        assertShouldShowCarsCrossSellButton(true)
    }

    @Test
    fun testShouldNotShowCarsCrossSellButton() {
        setupCarsCrossSellButton(shouldShow = false)
        assertShouldShowCarsCrossSellButton(false)
    }

    fun assertShouldShowCarsCrossSellButton(show: Boolean) {
        if (show) assertTrue(presenter.addCarLayout.visibility == View.VISIBLE)
        else assertTrue(presenter.addCarLayout.visibility == View.GONE)
    }

    fun setupCarsCrossSellButton(shouldShow: Boolean = true) {
        PointOfSaleTestConfiguration.configurePointOfSale(activity, if (shouldShow) "MockSharedData/pos_with_car_cross_sell.json"
        else "MockSharedData/pos_with_no_car_cross_sell.json", false)
        presenter = LayoutInflater.from(activity).inflate(com.expedia.bookings.R.layout.package_confirmation_stub, null) as PackageConfirmationPresenter
    }
}
