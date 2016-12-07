package com.expedia.bookings.widget.itin;

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.widget.TextView
import com.mobiata.android.util.SettingUtils
import okio.Okio
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelItinCardTest {

    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    lateinit private var sut: HotelItinCard
    lateinit private var itinCardData: ItinCardDataHotel

    @Test
    fun vipLabelText() {
        createSystemUnderTest()
        assertEquals("+VIP", getVipLabelTextView().text)
    }

    @Test
    fun vipLabelTextVisible() {
        createSystemUnderTest()
        givenGoldMember()
        itinCardData = givenHotel(vipHotel = true)
        sut.bind(itinCardData)
        assertEquals(View.VISIBLE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelPosVipSupportDisabled() {
        createSystemUnderTest()
        givenGoldMember()
        itinCardData = givenHotel(vipHotel = true)
        givenPointOfSaleVipSupportDisabled()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelToBlueMember() {
        createSystemUnderTest()
        givenBlueMember()
        itinCardData = givenHotel(vipHotel = true)
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelForNonVipAccessHotel() {
        createSystemUnderTest()
        givenGoldMember()
        itinCardData = givenHotel(vipHotel = false)
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun roomUpgradeAvailable() {
        SettingUtils.save(activity, R.string.preference_itin_hotel_upgrade, true)
        createSystemUnderTest()
        itinCardData = givenExpandedHotel()
        sut.bind(itinCardData)
        sut.expand(false)
        assertEquals(View.VISIBLE, getUpgradeTextView().visibility)
    }

    @Test
    fun upgradeBannerAvailable() {
        createSystemUnderTest()
        itinCardData = givenExpandedHotel()
        sut.bind(itinCardData)
        sut.expand(false)
        assertEquals(View.VISIBLE, getUpgradeBannerTextView().visibility)
    }

    fun hotelSoftChangeButtonAvailable() {
        SettingUtils.save(activity, R.string.preference_hotel_itin_soft_change_button, true)
        createSystemUnderTest()
        itinCardData = givenExpandedHotel()
        sut.bind(itinCardData)
        sut.expand(false)
        assertEquals(View.VISIBLE, getHotelSoftChangeButtonTextView().visibility)
    }

    private fun givenPointOfSaleVipSupportDisabled() {
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_vipaccess_disabled.json")
    }

    private fun getVipLabelTextView(): TextView {
        val vipLabelTextView = sut.findViewById(R.id.vip_label_text_view) as TextView
        return vipLabelTextView
    }

    private fun getUpgradeTextView(): TextView {
        val upgradeText = sut.findViewById(R.id.upgrade_string) as TextView
        return upgradeText
    }

    private fun getUpgradeBannerTextView(): TextView {
        val upgradeBanner = sut.findViewById(R.id.room_upgrade_message) as TextView
        return upgradeBanner
    }

    private fun getHotelSoftChangeButtonTextView(): TextView {
        val hotelSoftChangeText = sut.findViewById(R.id.edit_hotel_room_info) as TextView
        return hotelSoftChangeText
    }

    private fun createSystemUnderTest() {
        activity.setTheme(R.style.NewLaunchTheme)
        val itinCard = HotelItinCard(activity, null)
        LayoutInflater.from(activity).inflate(R.layout.widget_itin_card, itinCard)
        sut = itinCard
    }

    private fun givenHotel(vipHotel: Boolean): ItinCardDataHotel {
        val fileName = if (vipHotel) "hotel_trip_vip_booking" else "hotel_trip_non_vip_booking"
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/$fileName.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val tripHotel = getHotelTrip(jsonArray)!!

        return ItinCardDataHotel(tripHotel)
    }

    private fun givenExpandedHotel(): ItinCardDataHotel {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val tripHotel = getHotelTrip(jsonArray)!!

        return ItinCardDataHotel(tripHotel)
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

    private fun getHotelTrip(jsonArray: JSONArray): TripHotel? {
        val tripParser = TripParser()

        var index = 0
        while (index < jsonArray.length()) {
            val tripJsonObj = jsonArray.get(index) as JSONObject
            val tripObj = tripParser.parseTrip(tripJsonObj)
            val tripComponent = tripObj.tripComponents[0]
            if (tripComponent is TripHotel) {
                return tripComponent
            }
            index++
        }
        return null
    }
}