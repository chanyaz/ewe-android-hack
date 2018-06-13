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
    private var isCrossSellPackageOnFSREnabled = false
    private val IS_RESTORED = "isRestored"

    val packagePresenter by bindView<PackagePresenter>(R.id.package_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(IS_RESTORED) ?: false) {
            PackagesTracking().trackDormantUserHomeRedirect()
            val intent = Intent(this, PhoneLaunchActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            finish()
            startActivity(intent)
        } else {
            PackagesPageUsableData.SEARCH.pageUsableData.markPageLoadStarted()
            Ui.getApplication(this).defaultPackageComponents()
            Ui.getApplication(this).defaultTravelerComponent()
            setContentView(R.layout.package_activity)
            Ui.showTransparentStatusBar(this)
            isCrossSellPackageOnFSREnabled = intent.getBooleanExtra(Constants.INTENT_PERFORM_HOTEL_SEARCH, false)
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
            Activity.RESULT_CANCELED -> {
                packagePresenter.handleActivityCanceled()
            }
            Activity.RESULT_OK ->
                when (requestCode) {
                    Constants.HOTEL_REQUEST_CODE -> {
                        val errorKey = data?.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR_KEY)
                        val errorString = data?.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
                        val isErrorFromInfositeCall = data?.extras?.getBoolean(Constants.PACKAGE_HOTEL_DID_INFOSITE_CALL_FAIL)
                                ?: false
                        val filterSearchErrorKey = data?.extras?.getString(Constants.PACKAGE_FILTER_SEARCH_ERROR_KEY)
                        val filterSearchErrorString = data?.extras?.getString(Constants.PACKAGE_FILTER_SEARCH_ERROR)
                        when {
                            errorKey != null && errorString != null -> packagePresenter.handleHotelOffersAPIError(isErrorFromInfositeCall, errorKey, errorString)
                            filterSearchErrorKey != null && filterSearchErrorString != null -> packagePresenter.handleHotelFilterAPIError(filterSearchErrorKey, filterSearchErrorString)
                            else -> packagePresenter.hotelSelectedSuccessfully()
                        }
                    }
                    Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE -> packagePresenter.flightOutboundSelectedSuccessfully()

                    Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE -> packagePresenter.flightInboundSelectedSuccessfully()
                }
        }
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
