package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.packages.presenter.PackagePresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageCheckoutErrorTest {

    lateinit var presenter: PackagePresenter
    lateinit var activity: FragmentActivity

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        presenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
    }

    @Test
    fun testWebCheckoutViewErrorShowsNativeSearch() {
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        presenter.show(presenter.bundlePresenter)
        val testmaskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        val testUrlObservable = TestObserver.create<String>()
        val testShowNativeObserver = TestObserver.create<Unit>()
        presenter.bundlePresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(testmaskWebCheckoutActivityObservable)
        presenter.bundlePresenter.webCheckoutView.viewModel.showNativeSearchObservable.subscribe(testShowNativeObserver)
        presenter.bundlePresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testUrlObservable)

        presenter.bundlePresenter.webCheckoutView.goToSearchAndClearWebView()

        testmaskWebCheckoutActivityObservable.assertValues(false)
        testUrlObservable.assertValues("about:blank")
        testShowNativeObserver.assertValueCount(1)
        assertTrue(presenter.searchPresenter.visibility == View.VISIBLE)
        assertTrue(presenter.bundlePresenter.webCheckoutView.visibility == View.GONE)
    }
}
