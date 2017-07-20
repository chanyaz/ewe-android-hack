package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.launch.vm.NewLaunchLobViewModel
import com.expedia.bookings.launch.widget.NewLaunchLobWidget
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowAlertDialog
import rx.subjects.BehaviorSubject
import java.util.HashMap
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@RunWith(RobolectricRunner::class)
class NewLaunchLobWidgetTest {

    var newLaunchLobWidget: NewLaunchLobWidget by Delegates.notNull()

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    fun setUp() {
        newLaunchLobWidget = LayoutInflater.from(getContext()).inflate(R.layout.widget_new_launch_lob, null, false) as NewLaunchLobWidget
        newLaunchLobWidget.viewModel = NewLaunchLobViewModel(getContext(), BehaviorSubject.create<Boolean>(), BehaviorSubject.create<Unit>())
    }

    @Test
    fun pointOfSaleDeterminesLobsAvailable() {
        setUp()
        checkLOBsAvailable()
        for (pos in PointOfSale.getAllPointsOfSale(getContext())) {
            SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.pointOfSaleId.id.toString())
            newLaunchLobWidget.viewModel.posChangeSubject?.onNext(Unit)
            checkLOBsAvailable()
        }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightNotAvailableIndiaPOS() {
        setPOS(PointOfSaleId.INDIA)
        validateFlightNotAvialable()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightNotAvailableArgentinaPOS() {
        setPOS(PointOfSaleId.ARGENTINA)
        validateFlightNotAvialable()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightNotAvailableVietnamPOS() {
        setPOS(PointOfSaleId.VIETNAM)
        validateFlightNotAvialable()
    }

    @Test
    fun checkTitleChangeStringVariant1() {
        SettingUtils.save(getContext(), R.string.preference_packages_title_change, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesTitleChange)
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppPackagesTitleChange, AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal)
        setUp()
        packagesTitleChange(1)
    }

    @Test
    fun checkTitleChangeStringVariant2() {
        SettingUtils.save(getContext(), R.string.preference_packages_title_change, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesTitleChange)
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppPackagesTitleChange, AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal)
        setUp()
        packagesTitleChange(2)
    }

    private fun checkLOBsAvailable() {
        var isHotelsLOBDisplayed = false
        var isFlightsLOBDisplayed = false
        val recyclerView = newLaunchLobWidget.findViewById(R.id.lob_grid_recycler) as RecyclerView
        recyclerView.measure(0, 0)
        recyclerView.layout(0, 0, 100, 10000)
        val itemCount = recyclerView.layoutManager.itemCount
        val lobInfoLabelMap = getLOBInfoLabelMap()
        for (position in 0..itemCount - 1) {
            val childAt = recyclerView.layoutManager.findViewByPosition(position)
            val textView = childAt.findViewById(R.id.lob_cell_text) as TextView
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

    private fun packagesTitleChange(titleVariant: Int) {
        val recyclerView = newLaunchLobWidget.findViewById(R.id.lob_grid_recycler) as RecyclerView
        recyclerView.measure(0, 0)
        recyclerView.layout(0, 0, 100, 10000)
        val itemCount = recyclerView.layoutManager.itemCount
        val lobInfoLabelMap = getLOBInfoLabelMap()
        for (position in 0..itemCount - 1) {
            val childAt = recyclerView.layoutManager.findViewByPosition(position)
            val textView = childAt.findViewById(R.id.lob_cell_text) as TextView
            val lobText = textView.text
            val lobInfo = lobInfoLabelMap[lobText]
            assertNotNull(lobInfo)
            val lineOfBusiness = lobInfo?.lineOfBusiness
            if (lineOfBusiness == LineOfBusiness.PACKAGES && titleVariant == 1) {
                assertEquals("Hotel + Flight", lobText)
            } else if (lineOfBusiness == LineOfBusiness.PACKAGES && titleVariant == 2) {
                assertEquals("Hotel + Flight Deals", lobText)
            }
        }
    }

    private fun getLOBInfoLabelMap(): HashMap<String, LobInfo> {
        val lobLabelInfoMap = HashMap<String, LobInfo>()
        for (lobInfo in LobInfo.values()) {
            lobLabelInfoMap.put(getContext().getString(lobInfo.labelRes), lobInfo)
        }
        if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(getContext(), AbacusUtils.EBAndroidAppPackagesTitleChange, R.string.preference_packages_title_change)) {
            lobLabelInfoMap.put("Hotel + Flight", LobInfo.PACKAGES)
            lobLabelInfoMap.put("Hotel + Flight Deals", LobInfo.PACKAGES)
        }
        return lobLabelInfoMap
    }

    private fun validateFlightNotAvialable() {
        setUp()
        var allLobsRecycler = newLaunchLobWidget.findViewById(R.id.lob_grid_recycler) as android.support.v7.widget.RecyclerView
        // workaround robolectric recyclerView issue
        allLobsRecycler.measure(0, 0)
        allLobsRecycler.layout(0, 0, 100, 1000)
        allLobsRecycler.findViewHolderForAdapterPosition(1).itemView.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val errorMessage = alertDialog.findViewById(android.R.id.message) as android.widget.TextView
        assertEquals("Sorry, but mobile flight booking is not yet available in your location.", errorMessage.text.toString())
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
    }
}

