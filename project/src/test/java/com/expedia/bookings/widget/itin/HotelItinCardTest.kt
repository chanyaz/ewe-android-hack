package com.expedia.bookings.widget.itin;

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelItinCardTest {

    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    lateinit private var sut: HotelItinCard

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun vipLabelText() {
        createSystemUnderTest()
        assertEquals("+VIP", getVipLabelTextView().text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun vipLabelTextVisible() {
        createSystemUnderTest()
        givenGoldMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(true).build()
        sut.bind(itinCardData)
        assertEquals(View.VISIBLE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelPosVipSupportDisabled() {
        createSystemUnderTest()
        givenGoldMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(true).build()
        givenPointOfSaleVipSupportDisabled()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelToBlueMember() {
        createSystemUnderTest()
        givenBlueMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(true).build()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelForNonVipAccessHotel() {
        createSystemUnderTest()
        givenGoldMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(false).build()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerVisible() {
        SettingUtils.save(activity, R.string.preference_itin_hotel_upgrade, true)
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withUpgradeableRoom().build()
        sut.bind(itinCardData)

        assertEquals(View.VISIBLE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerGoneFeatureOff() {
        SettingUtils.save(activity, R.string.preference_itin_hotel_upgrade, false)
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withUpgradeableRoom().build()
        sut.bind(itinCardData)

        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerGoneForSharedItin() {
        SettingUtils.save(activity, R.string.preference_itin_hotel_upgrade, true)
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder()
                            .withUpgradeableRoom()
                            .isSharedItin(true).build()
        sut.bind(itinCardData)

        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    private fun givenPointOfSaleVipSupportDisabled() {
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_vipaccess_disabled.json")
    }

    private fun getVipLabelTextView(): TextView {
        val vipLabelTextView = sut.findViewById(R.id.vip_label_text_view) as TextView
        return vipLabelTextView
    }

    private fun getUpgradeBannerTextView(): TextView {
        val upgradeBanner = sut.findViewById(R.id.room_upgrade_available_banner) as TextView
        return upgradeBanner
    }

    private fun createSystemUnderTest() {
        activity.setTheme(R.style.NewLaunchTheme)
        val itinCard = HotelItinCard(activity, null)
        LayoutInflater.from(activity).inflate(R.layout.widget_itin_card, itinCard)
        sut = itinCard
    }

    private fun givenBlueMember() {
        createUser(LoyaltyMembershipTier.BASE)
    }

    private fun givenGoldMember() {
        createUser(LoyaltyMembershipTier.TOP)
    }

    private fun createUser(loyaltyMembershipTier: LoyaltyMembershipTier) {
        val goldCustomer = UserLoginTestUtil.mockUser(loyaltyMembershipTier)
        UserLoginTestUtil.setupUserAndMockLogin(goldCustomer)
    }
}
