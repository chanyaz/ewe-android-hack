package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.argnome.widget.ArGnomeBannerWidget
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.shouldShowTravelocityGnomeArModule
import com.expedia.bookings.vm.ArGnomeBannerViewModel
import kotlinx.android.synthetic.travelocity.ar_gnome_banner_widget.view.*
import kotlinx.android.synthetic.travelocity.travelocity_gnome_custom_view.view.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = [MultiBrand.TRAVELOCITY])
class ArGnomeBannerWidgetTest {
    private lateinit var context: Context
    private lateinit var gnomeBannerWidget: ArGnomeBannerWidget
    private lateinit var phoneLaunchWidget: PhoneLaunchWidget

    @Before
    fun setup() {
        context = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_AppCompat)
        phoneLaunchWidget = android.view.LayoutInflater.from(activity).inflate(R.layout.widget_phone_launch, null) as PhoneLaunchWidget
        gnomeBannerWidget = phoneLaunchWidget.arGnomeBannerWidget as ArGnomeBannerWidget
        gnomeBannerWidget.viewModel = Mockito.mock(ArGnomeBannerViewModel::class.java)
    }

    @Test
    fun arGnomeBannerIsNotVisibleByDefault() {
        assert(!shouldShowTravelocityGnomeArModule(context))
        assert(gnomeBannerWidget.gnome_image_view.visibility == View.GONE)
    }

    @Test
    fun arGnomeBannerIsNotVisibleWhenScrolledAwayFromTop() {
        Mockito.`when`(gnomeBannerWidget.viewModel.shouldShowGnomeAnimations).thenReturn(true)

        gnomeBannerWidget.onParentScrolledAwayFromTop()

        assert(gnomeBannerWidget.top_title_text_view.visibility == View.GONE)
        assert(gnomeBannerWidget.gnome_image_view.visibility == View.GONE)
    }

    @Test
    fun arGnomeBannerIsVisibleWhenScrolledToTop() {
        Mockito.`when`(gnomeBannerWidget.viewModel.shouldShowGnomeAnimations).thenReturn(true)

        gnomeBannerWidget.onParentScrolledToTop()

        assert(gnomeBannerWidget.top_title_text_view.visibility == View.VISIBLE)
        assert(gnomeBannerWidget.gnome_image_view.visibility == View.VISIBLE)
    }
}
