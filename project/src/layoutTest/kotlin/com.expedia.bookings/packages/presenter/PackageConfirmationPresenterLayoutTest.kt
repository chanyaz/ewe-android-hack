package com.expedia.bookings.packages.presenter

import android.content.Context
import android.graphics.Bitmap
import android.support.test.runner.AndroidJUnit4
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.dataspecs.DataSpecItin
import com.expedia.layouttestandroid.LayoutTestCase
import com.expedia.layouttestandroid.LayoutViewProvider
import com.expedia.layouttestandroid.dataspecs.LayoutDataSpecValues
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.util.ExtractOptionalValue
import com.expedia.layouttestandroid.util.ExtractValue
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
            val view = View.inflate(context, R.layout.package_confirmation_presenter_test, null) as PackageConfirmationPresenter

            ValueExtractor(dataSpec).apply {
                view.itinNumber.text = itinNumber
                view.destination.text = destination
                view.expediaPoints.text = expediaPoints
                view.destinationCard.title.text = destinationCardTitle
                view.destinationCard.subTitle.text = destinationCardSubTitle
                view.outboundFlightCard.title.text = outboundFlightCardTitle
                view.outboundFlightCard.subTitle.text = outboundFlightCardSubTitle
                view.inboundFlightCard.title.text = inboundFlightCardTitle
                view.inboundFlightCard.subTitle.text = inboundFlightCardSubTitle
            }

            return view
        }

        override fun dataSpecForTest(): Map<String, LayoutDataSpecValues> {
            val map = hashMapOf<String, LayoutDataSpecValues>()
            map.put("itinNumber", DataSpecItin)
            map.put("itinNumber", DataSpecItin)
            return map
        }
    }

//    private class ValueInjector() {
//
//    }

    private class ValueExtractor(dataSpec: Map<String, Any?>) {
        val itinNumber: String by ExtractValue(dataSpec)
        val destination: String by ExtractValue(dataSpec)
        val expediaPoints: String? by ExtractOptionalValue(dataSpec)
        val destinationCardTitle: String by ExtractValue(dataSpec)
        val destinationCardSubTitle: String by ExtractValue(dataSpec)
        val outboundFlightCardTitle: String by ExtractValue(dataSpec)
        val outboundFlightCardSubTitle: String by ExtractValue(dataSpec)
        val inboundFlightCardTitle: String by ExtractValue(dataSpec)
        val inboundFlightCardSubTitle: String by ExtractValue(dataSpec)
    }
}
