package com.expedia.bookings.packages.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.clarisite.mobile.ClarisiteAgent
import com.clarisite.mobile.exceptions.EyeViewException
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.packages.presenter.PackagePresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isGlassboxForPackagesEnabled
import com.expedia.ui.AbstractAppCompatActivity

class PackageActivity : AbstractAppCompatActivity() {
    private val IS_RESTORED = "isRestored"

    val packagePresenter by bindView<PackagePresenter>(R.id.package_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(IS_RESTORED) == true) {
            PackagesTracking().trackDormantUserHomeRedirect()
            val intent = Intent(this, PhoneLaunchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            finish()
            startActivity(intent)
        } else {
            PackagesPageUsableData.SEARCH.pageUsableData.markPageLoadStarted()
            Ui.getApplication(this).defaultPackageComponents()
            Ui.getApplication(this).defaultTravelerComponent()
            setContentView(R.layout.package_activity)
            Ui.showTransparentStatusBar(this)
            if (isGlassboxForPackagesEnabled()) {
                glassBoxStart()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Constants.PACKAGE_PARAMS_NULL_RESTORE || Db.sharedInstance.packageParams == null) {
            finish()
            return
        }
        packagePresenter.bundlePresenter.bundleWidget.collapseBundleWidgets()

        when (resultCode) {
            Activity.RESULT_CANCELED -> packagePresenter.handleActivityCanceled()

            Constants.PACKAGE_HOTEL_OFFERS_API_ERROR_RESULT_CODE -> handleOffersAndInfositeError(data?.extras, false)

            Constants.PACKAGE_HOTEL_INFOSITE_API_ERROR_RESULT_CODE -> handleOffersAndInfositeError(data?.extras, true)

            Constants.PACKAGE_HOTEL_FILTER_API_ERROR_RESULT_CODE -> packagePresenter.handleHotelFilterAPIError(data?.extras?.getString(Constants.PACKAGE_FILTER_SEARCH_ERROR))

            Activity.RESULT_OK -> when (requestCode) {
                Constants.HOTEL_REQUEST_CODE -> packagePresenter.hotelSelectedSuccessfully()

                Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE -> packagePresenter.flightOutboundSelectedSuccessfully()

                Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE -> packagePresenter.flightInboundSelectedSuccessfully()
            }
        }
    }

    private fun handleOffersAndInfositeError(extras: Bundle?, isErrorFromInfositeCall: Boolean) {
        val errorKey = extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
        val errorString = extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        packagePresenter.handleHotelOffersAndInfositeAPIError(errorKey, errorString, isErrorFromInfositeCall)
    }

    override fun onBackPressed() {
        if (!packagePresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            Ui.hideKeyboard(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Db.getTripBucket().clearPackages()
    }

    private fun glassBoxStart() {
        try {
            ClarisiteAgent.start()
        } catch (e: EyeViewException) {
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(IS_RESTORED, true)
        super.onSaveInstanceState(outState)
    }
}
