package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.support.v7.view.menu.MenuItemImpl
import android.view.LayoutInflater
import android.widget.ImageButton
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class HotelPresenterTest {

    lateinit var hotelPresenter: HotelPresenter
    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
    }

    @Test
    fun testToolbarShowsGrowthSharingWhenBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppGrowthSocialSharing)
        val shareMenuItem = hotelPresenter.detailPresenter.hotelDetailView.hotelDetailsToolbar.toolbar.menu.findItem(R.id.menu_share) as? MenuItemImpl
        val actionView = shareMenuItem?.actionView
        val shareButton = actionView?.findViewById<ImageButton>(R.id.share_button)

        assertNotNull(shareMenuItem)
        assertNotNull(actionView)
        assertNotNull(shareButton)
        assertEquals(activity.getDrawable(R.drawable.ic_share), shareButton?.drawable)
        assertEquals("Share", shareButton?.contentDescription)
    }

    @Test
    fun testToolbarHidesShareIconControl() {
        val toolbar = hotelPresenter.detailPresenter.hotelDetailView.hotelDetailsToolbar.toolbar
        val shareMenuItem = toolbar.menu.findItem(R.id.menu_share) as? MenuItemImpl

        assertNull(shareMenuItem)
        assertFalse(toolbar.menu.hasVisibleItems())
    }
}
