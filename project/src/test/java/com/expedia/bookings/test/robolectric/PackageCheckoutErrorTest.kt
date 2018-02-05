package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.Ui
import com.expedia.vm.packages.PackageCheckoutViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageCheckoutErrorTest {

    lateinit var presenter: PackagePresenter
    lateinit var activity: FragmentActivity
    lateinit var viewModel: PackageCheckoutViewModel

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        presenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
        viewModel = presenter.bundlePresenter.getCheckoutPresenter().getCheckoutViewModel()
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun testUnknownCheckoutErrorClearsCVV() {
        viewModel.builder.cvv("344")
        assertTrue(presenter.bundlePresenter.getCheckoutPresenter().getCheckoutViewModel().builder.hasValidCVV())

        viewModel.checkoutErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
        assertFalse(viewModel.builder.hasValidCVV())
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun testUnknownCheckoutErrorGoesToCheckout() {
        presenter.show(presenter.bundlePresenter)
        presenter.show(presenter.errorPresenter)
        presenter.errorPresenter.viewmodel.checkoutApiErrorObserver.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
        presenter.errorPresenter.viewmodel.errorButtonClickedObservable.onNext(Unit)
        assertTrue(presenter.bundlePresenter.getCheckoutPresenter().visibility == View.VISIBLE)
    }
}
