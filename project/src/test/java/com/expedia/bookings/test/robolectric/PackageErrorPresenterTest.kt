package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.packages.presenter.PackagePresenter
import com.expedia.bookings.packages.vm.PackageErrorViewModel
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.Ui
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricRunner::class)
class PackageErrorPresenterTest {

    private lateinit var activity: Activity
    private lateinit var packagePresenter: PackagePresenter

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultHotelComponents()
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
        packagePresenter.errorPresenter.viewmodel = PackageErrorViewModel(activity)
    }

    @Test
    fun testPackageSearchErrorScreen() {
        packagePresenter.errorPresenter.viewmodel.paramsSubject.onNext(PackageTestUtil.getPackageSearchParams())
        packagePresenter.errorPresenter.viewmodel.packageSearchApiErrorObserver.onNext(getSearchAPIErrorDetails(PackageApiError.Code.search_response_null))

        Assert.assertEquals(View.VISIBLE, packagePresenter.errorPresenter.errorButton.visibility)
        Assert.assertEquals(View.VISIBLE, packagePresenter.errorPresenter.errorImage.visibility)
        Assert.assertEquals(View.VISIBLE, packagePresenter.errorPresenter.errorText.visibility)

        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.error_package_search_message), packagePresenter.errorPresenter.errorText.text)
        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.edit_search), packagePresenter.errorPresenter.errorButton.text)
        val errorImageDrawable = Shadows.shadowOf(packagePresenter.errorPresenter.errorImage.drawable)
        Assert.assertEquals(R.drawable.error_default, errorImageDrawable.createdFromResId)
    }

    private fun getSearchAPIErrorDetails(errorCode: PackageApiError.Code): Pair<PackageApiError.Code, ApiCallFailing> {
        return Pair(errorCode, ApiCallFailing.PackageHotelSearch(errorCode.name))
    }
}
