package com.expedia.bookings.server

import com.expedia.bookings.R
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class EndpointProviderTest {

    private val context = RuntimeEnvironment.application
    private val endpointProvider = Ui.getApplication(context).appComponent().endpointProvider()

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetEssEndpointUrlForTrunk() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Trunk")
        assertEquals("https://ess.us-west-2.int.expedia.com/", endpointProvider.essEndpointUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetEssEndpointUrlForIntegration() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Integration")
        assertEquals("https://ess.us-west-2.int.expedia.com/", endpointProvider.essEndpointUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetEssEndpointUrlForProduction() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Production")
        assertEquals("https://suggest.expedia.com/", endpointProvider.essEndpointUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetHotelShortlistEndpointUrlForMockMode() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Mock Mode")
        assertEquals("https://localhost:3000/", endpointProvider.hotelShortlistEndpointUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetHotelShortlistEndpointUrlForProduction() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Production")
        assertEquals("https://www.expedia.com/", endpointProvider.hotelShortlistEndpointUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetHotelShortlistEndpointUrlForIntegration() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Integration")
        assertEquals("https://wwwexpediacom.integration.sb.karmalab.net/", endpointProvider.hotelShortlistEndpointUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGetHotelShortlistEndpointUrlForOthers() {
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Dev")
        assertEquals("https://www.expedia.com/", endpointProvider.hotelShortlistEndpointUrl)
    }
}
