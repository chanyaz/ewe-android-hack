package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.AboutWebViewActivity
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class AccountSettingsFragmentTest {

    lateinit var fragment: AccountSettingsFragment
    lateinit var activity: FragmentActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentUtilActivity::class.java).create().get()
    }

    @Test
    fun nonRewardsMemberDoesNotSeeRewardsInfo() {
        givenSignedInAsUser(getNonRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("No Rewards ForMe", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("norewards@mobiata.com", R.id.toolbar_email)
        assertViewIsEffectivelyGone(R.id.toolbar_loyalty_tier_text)
        assertViewIsEffectivelyGone(R.id.pending_points)
        assertViewIsEffectivelyGone(R.id.first_row_country)
        assertViewIsEffectivelyGone(R.id.points_monetary_value_label)
        assertViewIsEffectivelyGone(R.id.points_monetary_value)
    }

    @Test
    fun nonRewardsMemberSeesCurrencyAndCountry() {
        givenSignedInAsUser(getNonRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Currency", R.id.currency_label)
        assertTextIsDisplayedInTextView("USD", R.id.currency)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.second_row_country)
    }

    @Test
    fun baseTierRewardsMemberSeesPoints() {
        givenSignedInAsUser(getBaseTierRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Base Tier Rewards", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("basetier@mobiata.com", R.id.toolbar_email)
        assertTextIsDisplayedInTextView(R.string.reward_base_tier_name_short, R.id.toolbar_loyalty_tier_text)
        assertTextIsDisplayedInTextView("1,802", R.id.available_points)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.first_row_country)
        assertViewIsEffectivelyGone(R.id.pending_points)
        assertViewIsEffectivelyGone(R.id.points_monetary_value_label)
        assertViewIsEffectivelyGone(R.id.points_monetary_value)
        assertViewIsEffectivelyGone(R.id.currency_label)
        assertViewIsEffectivelyGone(R.id.currency)
    }

    @Test
    fun middleTierRewardsMemberSeesPoints() {
        givenSignedInAsUser(getMiddleTierRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Middle Tier Rewards", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("middletier@mobiata.com", R.id.toolbar_email)
        assertTextIsDisplayedInTextView(R.string.reward_middle_tier_name_short, R.id.toolbar_loyalty_tier_text)
        assertTextIsDisplayedInTextView("22,996", R.id.available_points)
        assertTextIsDisplayedInTextView("965 pending", R.id.pending_points)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.second_row_country)
        assertTextIsDisplayedInTextView("Points value", R.id.points_monetary_value_label)
        assertTextIsDisplayedInTextView("$3,285.14", R.id.points_monetary_value)
        assertTextIsDisplayedInTextView("Currency", R.id.currency_label)
        assertTextIsDisplayedInTextView("USD", R.id.currency)
    }

    @Test
    fun topTierRewardsMemberSeesPoints() {
        givenSignedInAsUser(getTopTierRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Top Tier Rewards", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("toptier@mobiata.com", R.id.toolbar_email)
        assertTextIsDisplayedInTextView(R.string.reward_top_tier_name_short, R.id.toolbar_loyalty_tier_text)
        assertTextIsDisplayedInTextView("54,206", R.id.available_points)
        assertTextIsDisplayedInTextView("5,601 pending", R.id.pending_points)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.second_row_country)
        assertTextIsDisplayedInTextView("Points value", R.id.points_monetary_value_label)
        assertTextIsDisplayedInTextView("$7,743.41", R.id.points_monetary_value)
        assertTextIsDisplayedInTextView("Currency", R.id.currency_label)
        assertTextIsDisplayedInTextView("USD", R.id.currency)
    }

    @Test
    fun argentinaDisplaysProperly() {
        doCountryTest(PointOfSaleId.ARGENTINA, "ARG", R.drawable.ic_flag_ar_icon)
    }

    @Test
    fun canadaDisplaysProperly() {
        doCountryTest(PointOfSaleId.CANADA, "CAN", R.drawable.ic_flag_ca_icon)
    }

    @Test
    fun hongKongDisplaysProperly() {
        doCountryTest(PointOfSaleId.HONG_KONG, "HKG", R.drawable.ic_flag_hk_icon)
    }

    @Test
    fun koreaDisplaysProperly() {
        doCountryTest(PointOfSaleId.SOUTH_KOREA, "KOR", R.drawable.ic_flag_kr_icon)
    }

    @Test
    fun unitedKingdomDisplaysProperly() {
        doCountryTest(PointOfSaleId.UNITED_KINGDOM, "GBR", R.drawable.ic_flag_uk_icon)
    }

    @Test
    fun appSupportEmailUs() {
        var webViewActivity = Robolectric.buildActivity(AboutWebViewActivity::class.java).create().get()
        var webView = LayoutInflater.from(webViewActivity).inflate(R.layout.web_view_toolbar, null) as FrameLayout
        var toolbarView = webView.findViewById(R.id.toolbar) as android.support.v7.widget.Toolbar

        assertFalse(toolbarView.isOverflowMenuShowing)
    }

    private fun doCountryTest(pointOfSaleId: PointOfSaleId, expectedCountryCode: String, expectedFlagResId: Int) {
        givenPOS(pointOfSaleId)
        givenSignedInAsUser(getTopTierRewardsMember())
        givenFragmentSetup()

        assertCountryViewDisplayed(expectedCountryCode, expectedFlagResId, R.id.second_row_country)
    }

    private fun givenFragmentSetup() {
        fragment = AccountSettingsFragment()
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity::class.java, 1)
    }

    private fun givenSignedInAsUser(user: User) {
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

    private fun givenPOS(pointOfSaleId: PointOfSaleId) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, Integer.toString(pointOfSaleId.id))
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun getNonRewardsMember(): User {
        val user = User()
        val traveler = Traveler()

        traveler.firstName = "No"
        traveler.middleName = "Rewards"
        traveler.lastName = "ForMe"
        traveler.email = "norewards@mobiata.com"
        user.primaryTraveler = traveler

        return user
    }

    private fun getBaseTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Base"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "basetier@mobiata.com"
        traveler.loyaltyPointsAvailable = 1802
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.BASE
        loyaltyInfo.loyaltyPointsAvailable = 1802.0
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getMiddleTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Middle"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "middletier@mobiata.com"
        traveler.loyaltyPointsAvailable = 22996
        traveler.loyaltyPointsPending = 965
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.MIDDLE
        loyaltyInfo.loyaltyPointsAvailable = 22996.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("3285.14", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$3,285.14")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getTopTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Top"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "toptier@mobiata.com"
        traveler.loyaltyPointsAvailable = 54206
        traveler.loyaltyPointsPending = 5601
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        loyaltyInfo.loyaltyPointsAvailable = 54206.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("7743.41", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$7,743.41")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun assertTextIsDisplayedInTextView(expectedString: String, @IdRes viewId: Int) {
        assertViewIsEffectivelyVisibile(viewId)
        assertEquals(expectedString, (fragment.view?.findViewById(viewId) as TextView).text)
    }

    private fun assertTextIsDisplayedInTextView(@StringRes expectedStringId: Int, @IdRes viewId: Int) {
        assertEquals(fragment.view?.context?.getString(expectedStringId), (fragment.view?.findViewById(viewId) as TextView).text)
    }

    private fun assertCountryViewDisplayed(expectedCountryCode: String, @DrawableRes expectedFlagResId: Int, @IdRes viewId: Int) {
        assertViewIsEffectivelyVisibile(viewId)
        val countryView = fragment.view?.findViewById(viewId)
        assertEquals(expectedCountryCode, (countryView?.findViewById(R.id.country) as TextView).text)
        assertEquals(expectedFlagResId, Shadows.shadowOf(countryView?.findViewById(R.id.flagView) as ImageView).imageResourceId)
    }

    private fun assertViewIsEffectivelyGone(@IdRes viewId: Int) {
        val view = fragment.view?.findViewById(viewId)
        if (view != null && view.visibility != View.GONE) {
            var parent = view.parent
            while (parent != null && parent is View) {
                if (parent.visibility == View.GONE) {
                    return
                }
                parent = (parent as ViewParent).parent
            }
        }
        assertEquals(View.GONE, view?.visibility)
    }

    private fun assertViewIsEffectivelyVisibile(@IdRes viewId: Int) {
        val view = fragment.view?.findViewById(viewId)
        assertEquals(View.VISIBLE, view?.visibility)
        var parent = view?.parent
        while (parent != null && parent is View) {
            assertEquals(View.VISIBLE, parent.visibility)
            parent = (parent as ViewParent).parent
        }
    }

    class FragmentUtilActivity() : FragmentActivity(), AboutSectionFragment.AboutSectionFragmentListener {
        override fun onAboutRowClicked(tag: Int): Boolean {
            return true
        }

        override fun onAboutRowRebind(tag: Int, titleTextView: TextView?, descriptionTextView: TextView?) {
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val view = LinearLayout(this)
            view.id = 1

            setContentView(view)
        }
    }

}