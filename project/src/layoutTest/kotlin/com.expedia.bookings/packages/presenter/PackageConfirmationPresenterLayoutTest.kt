package com.expedia.bookings.packages.presenter

import android.content.Context
import android.graphics.Bitmap
import android.support.test.runner.AndroidJUnit4
import android.view.View
import com.expedia.bookings.R
import com.expedia.layouttestandroid.LayoutTestCase
import com.expedia.layouttestandroid.LayoutViewProvider
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.viewsize.LayoutViewSize
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageConfirmationPresenterLayoutTest : LayoutTestCase() {
    @Test
    fun testLayout() {
        runLayoutTests(PackageConfirmationPresenterProvider())
    }

    override fun errorsFound(view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize, bitmap: Bitmap, exceptions: MutableList<LayoutTestException>) {
        super.errorsFound(view, dataSpec, size, bitmap, exceptions)
    }

    private class PackageConfirmationPresenterProvider : LayoutViewProvider() {

        override fun getView(context: Context, dataSpec: Map<String, Any?>, size: LayoutViewSize, reuseView: View?): View {
            return View.inflate(context, R.layout.package_confirmation_presenter_test, null)
        }
    }
}
