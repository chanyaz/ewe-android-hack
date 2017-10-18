package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ShopWithPointsWidget
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.text.NumberFormat
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class ShopWithPointsWidgetTest {
    private val context = RuntimeEnvironment.application
    lateinit private var shopWithPointsWidget: ShopWithPointsWidget

    @Before
    fun before() {
        Ui.getApplication(context).defaultHotelComponents()
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun pointsAvailableMessage() {
        val user = User()
        val traveler = Traveler()
        user.primaryTraveler = traveler
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        loyaltyInfo.loyaltyPointsAvailable = 1500.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        user.loyaltyMembershipInformation = loyaltyInfo
        UserLoginTestUtil.setupUserAndMockLogin(user)

        shopWithPointsWidget = ShopWithPointsWidget(context, null)
        val expectedText = Phrase.from(context.resources, R.string.swp_widget_points_value_TEMPLATE).put("points_or_amount", NumberFormat.getInstance().format(1500)).format().toString()
        assertEquals(expectedText, shopWithPointsWidget.loyaltyPointsInfo.text)
    }

    @Test
    fun toggleSWPSwitch() {
        shopWithPointsWidget = ShopWithPointsWidget(context, null)
        val toggleObservable = shopWithPointsWidget.shopWithPointsViewModel.shopWithPointsToggleObservable
        val headerTestObservable = TestSubscriber.create<String>()
        shopWithPointsWidget.shopWithPointsViewModel.swpHeaderStringObservable.subscribe(headerTestObservable)

        assertTrue(toggleObservable.value)
        assertEquals(context.getString(R.string.swp_on_widget_header), shopWithPointsWidget.loyaltyAppliedHeader.text)

        shopWithPointsWidget.swpSwitchView.isChecked = false
        assertFalse(toggleObservable.value)
        assertEquals(context.getString(R.string.swp_off_widget_header), shopWithPointsWidget.loyaltyAppliedHeader.text)

        shopWithPointsWidget.swpSwitchView.isChecked = true
        assertTrue(toggleObservable.value)
        assertEquals(context.getString(R.string.swp_on_widget_header), shopWithPointsWidget.loyaltyAppliedHeader.text)
    }
}