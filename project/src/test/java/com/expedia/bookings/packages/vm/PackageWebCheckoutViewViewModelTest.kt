package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class PackageWebCheckoutViewViewModelTest {
    private lateinit var context: Context
    private lateinit var viewModel: PackageWebCheckoutViewViewModel

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        viewModel = PackageWebCheckoutViewViewModel(context, Ui.getApplication(context).appComponent().endpointProvider())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.AIRASIAGO))
    fun testAirAsiaGoThailandPosCheckoutURL() {
        val testObserver = TestObserver<String>()
        viewModel.webViewURLObservable.subscribe(testObserver)
        PointOfSaleTestConfiguration.configurePOS(context, "MockSharedData/pos_thailand_test_config.json", Integer.toString(PointOfSaleId.AIRASIAGO_THAILAND.getId()), false)
        viewModel.packageCreateTripViewModel = PackageCreateTripViewModel(packageServiceRule.services!!, context)
        val multiItemApiCreateTripResponse = MultiItemApiCreateTripResponse()
        multiItemApiCreateTripResponse.tripId = "64b1aec3-1950-4988-86c4-753b2322c43f"
        viewModel.packageCreateTripViewModel.multiItemResponseSubject.onNext(multiItemApiCreateTripResponse)
        testObserver.assertValue("https://thailand.airasiago.com/MultiItemCheckout?tripid=" + multiItemApiCreateTripResponse.tripId)
    }
}
