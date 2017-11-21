package com.expedia.vm.test.robolectric

import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.MaterialFormsCouponWidget
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelCheckoutPresenterTest {
    private var checkout: HotelCheckoutMainViewPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.unbucketTestAndDisableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
    }

    @Test
    fun testMaterialCheckoutScrollViewColor() {
        setupHotelMaterialForms()
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(true)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.white))
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(false)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.gray100))
    }

    @Test
    fun testCheckoutScrollViewColor() {
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(true)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.gray100))
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(false)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.gray100))
    }

    @Test
    fun testMaterialCouponWidgetInflatedOnMaterialFormsBucketedAndFeatureToggleON() {
        setupHotelMaterialForms()
        assertTrue(checkout.couponCardView is MaterialFormsCouponWidget)
    }

    @Test
    fun testOldCouponWidgetInflatedOnMaterialFormsBucketedAndFeatureToggleOFF() {
        assertTrue(checkout.couponCardView is CouponWidget)
    }

    @Test
    fun testMaterialCouponWidgetApplyFiresDone() {
        val doneClickedSubscriber = TestSubscriber<Unit>()

        setupHotelMaterialForms()
        checkout.toolbar.viewModel.doneClicked.subscribe { doneClickedSubscriber.onNext(Unit) }

        doneClickedSubscriber.assertValueCount(0)

        checkout.toolbar.onMenuItemClicked(activity, "Apply")

        doneClickedSubscriber.assertValueCount(1)

        checkout.toolbar.onMenuItemClicked(activity, "Apply Button")

        doneClickedSubscriber.assertValueCount(2)
    }

    private fun setupHotelMaterialForms() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
    }
}
