package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.launch.vm.LaunchLobViewModel
import com.expedia.bookings.launch.widget.LaunchLobWidget
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import io.reactivex.subjects.BehaviorSubject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowAlertDialog
import java.util.HashMap
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(RobolectricRunner::class)
class LaunchLobWidgetTest {

    var launchLobWidget: LaunchLobWidget by Delegates.notNull()

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    fun setUp() {
        launchLobWidget = LayoutInflater.from(getContext()).inflate(R.layout.widget_launch_lob, null, false) as LaunchLobWidget
        launchLobWidget.viewModel = LaunchLobViewModel(getContext(), BehaviorSubject.create<Boolean>(), BehaviorSubject.create<Unit>())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun pointOfSaleDeterminesLobsAvailable() {
        setPOS(PointOfSaleId.INDIA)
        setUp()
        checkLOBsAvailable()
        for (pos in PointOfSale.getAllPointsOfSale(getContext())) {
            SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.pointOfSaleId.id.toString())
            launchLobWidget.viewModel.posChangeSubject?.onNext(Unit)
            checkLOBsAvailable()
        }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightNotAvailableIndiaPOS() {
        setPOS(PointOfSaleId.INDIA)
        validateFlightNotAvailable()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightNotAvailableArgentinaPOS() {
        setPOS(PointOfSaleId.ARGENTINA)
        validateFlightNotAvailable()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightNotAvailableVietnamPOS() {
        setPOS(PointOfSaleId.VIETNAM)
        validateFlightNotAvailable()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSVariant1() {
        setPOS(PointOfSaleId.UNITED_STATES)
        AbacusTestUtils.bucketTests(AbacusUtils.PackagesTitleChange)
        RoboTestHelper.updateABTest(AbacusUtils.PackagesTitleChange, AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal)
        setUp()
        packagesTitleChange("Hotel + Flight")
    }

    private fun checkLOBsAvailable() {
        var isHotelsLOBDisplayed = false
        var isFlightsLOBDisplayed = false
        val recyclerView = launchLobWidget.findViewById<View>(R.id.lob_grid_recycler) as RecyclerView
        recyclerView.measure(0, 0)
        recyclerView.layout(0, 0, 100, 10000)
        val itemCount = recyclerView.layoutManager.itemCount
        val lobInfoLabelMap = getLOBInfoLabelMap()
        for (position in 0..itemCount - 1) {
            val childAt = recyclerView.layoutManager.findViewByPosition(position)
            val textView = childAt.findViewById<View>(R.id.lob_cell_text) as TextView
            val lobText = textView.text
            val lobInfo = lobInfoLabelMap[lobText]
            assertNotNull(lobInfo)
            val lineOfBusiness = lobInfo?.lineOfBusiness
            if (lineOfBusiness == LineOfBusiness.HOTELS) {
                assert(!isHotelsLOBDisplayed)
                isHotelsLOBDisplayed = true
            } else if (lineOfBusiness == LineOfBusiness.FLIGHTS) {
                assert(!isFlightsLOBDisplayed)
                isFlightsLOBDisplayed = true
            } else {
                assert(PointOfSale.getPointOfSale().supports(lineOfBusiness))
            }
        }
        assert(isHotelsLOBDisplayed && isFlightsLOBDisplayed)
    }

    private fun packagesTitleChange(expectedTitle: String) {
        val recyclerView = launchLobWidget.findViewById<View>(R.id.lob_grid_recycler) as RecyclerView
        recyclerView.measure(0, 0)
        recyclerView.layout(0, 0, 100, 10000)
        val itemCount = recyclerView.layoutManager.itemCount
        for (position in 0..itemCount - 1) {
            val childAt = recyclerView.layoutManager.findViewByPosition(position)
            val textView = childAt.findViewById<TextView>(R.id.lob_cell_text)
            val lobText = textView.text
            if (lobText == expectedTitle) {
                assertTrue(true)
                return
            }
        }
        fail("Package title should be present")
    }

    private fun getLOBInfoLabelMap(): HashMap<String, LobInfo> {
        val lobLabelInfoMap = HashMap<String, LobInfo>()
        for (lobInfo in LobInfo.values()) {
            lobLabelInfoMap.put(getContext().getString(lobInfo.labelRes), lobInfo)
        }
        return lobLabelInfoMap
    }

    private fun validateFlightNotAvailable() {
        setUp()
        val allLobsRecycler = launchLobWidget.findViewById<View>(R.id.lob_grid_recycler) as android.support.v7.widget.RecyclerView
        // workaround robolectric recyclerView issue
        allLobsRecycler.measure(0, 0)
        allLobsRecycler.layout(0, 0, 100, 1000)
        allLobsRecycler.findViewHolderForAdapterPosition(1).itemView.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val errorMessage = alertDialog.findViewById<View>(android.R.id.message) as android.widget.TextView
        assertEquals("Sorry, but mobile flight booking is not yet available in your location.", errorMessage.text.toString())
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
    }
}

