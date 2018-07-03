package com.expedia.layouttestandroid

import android.content.Context
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.EditText
import com.expedia.layouttestandroid.dataspecs.DataSpecNonNullStringValues
import com.expedia.layouttestandroid.dataspecs.LayoutDataSpecValues
import com.expedia.layouttestandroid.tester.LayoutAssertion.Companion.assertTextViewNoTextWrap
import com.expedia.layouttestandroid.tester.LayoutAssertion.Companion.assertView
import com.expedia.layouttestandroid.tester.LayoutTester
import com.expedia.layouttestandroid.tester.LayoutTesterConfigurator
import com.expedia.layouttestandroid.viewsize.LayoutViewSize
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test, which will execute on an Android device.

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class MyLayoutTest: LayoutTestCase() {
    @Test
    fun testMyLayout() {
        runLayoutTests(MyLayoutProvider())
    }

    class MyLayoutProvider: LayoutViewProvider() {
        override fun getView(context: Context, dataSpec: Map<String, Any?>, size: LayoutViewSize, reuseView: View?): View {
            val rootView = View.inflate(context, R.layout.my_layout, null)
            val searchBox: EditText = rootView.findViewById(R.id.search_box)
            val searchBoxText = dataSpec["searchBoxText"] as String
            searchBox.setText(searchBoxText)
            return rootView
        }

        override fun dataSpecForTest(): Map<String, LayoutDataSpecValues> {
            return mapOf("searchBoxText" to DataSpecNonNullStringValues)
        }

        override fun sizesForView(): Array<LayoutViewSize> {
            return arrayOf(LayoutViewSize.Nexus5X)
        }

        override fun setupLayoutTesters(configurator: LayoutTesterConfigurator, view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize) {
            configurator.addTester(MyTester())
        }

        class MyTester: LayoutTester {
            override fun runTest(view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize) {
                assertView(view.visibility == View.VISIBLE, "View is visible", listOf(view))
                assertTextViewNoTextWrap(view.findViewById(R.id.search_box))
            }
        }
    }
}
