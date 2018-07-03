package com.expedia.layouttestandroid

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.support.test.InstrumentationRegistry
import android.support.test.rule.GrantPermissionRule
import android.view.View
import com.expedia.layouttestandroid.dataspecs.LayoutDataSpecValues
import com.expedia.layouttestandroid.extension.setSize
import com.expedia.layouttestandroid.helper.ScreenshotHelper
import com.expedia.layouttestandroid.helper.StorageHelper
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.tester.LayoutTestExceptionWithDescription
import com.expedia.layouttestandroid.tester.LayoutTester
import com.expedia.layouttestandroid.tester.LayoutTesterConfigurator
import com.expedia.layouttestandroid.util.LayoutFile
import com.expedia.layouttestandroid.util.LayoutTestUtils
import com.expedia.layouttestandroid.util.TestCombinations
import com.expedia.layouttestandroid.viewsize.LayoutViewSize
import com.facebook.testing.screenshot.TestNameDetector
import com.facebook.testing.screenshot.layouthierarchy.LayoutHierarchyDumper
import org.junit.Rule

abstract class LayoutTestCase {
    @Rule
    @JvmField
    val mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var reuseView: View? = null
    private val allTestExceptions: MutableList<LayoutTestExceptionWithDescription> = ArrayList()

    private val context: Context
        get() {
            return InstrumentationRegistry.getTargetContext()
        }

    fun runLayoutTests(viewProvider: LayoutViewProvider) {
        val packageName = context.packageName
        val testClass = TestNameDetector.getTestClass()
        val testName = TestNameDetector.getTestName()

        if (!LayoutTestUtils.isUiThread()) {
            LayoutTestUtils.runOnUiThread {
                runLayoutTests(viewProvider, packageName, testClass, testName)
            }
        } else {
            runLayoutTests(viewProvider, packageName, testClass, testName)
        }
    }

    open fun errorsFound(view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize, bitmap: Bitmap, exceptions: MutableList<LayoutTestException>) {
        //ignore
    }

    private fun runLayoutTests(viewProvider: LayoutViewProvider, packageName: String, testClass: String, testName: String) {
        StorageHelper.cleanup(packageName, testClass)

        var validationsCount = 0
        val dataSpecAndSizes = computeDataSpecAndSizes(viewProvider.sizesForView(), viewProvider.dataSpecForTest())
        dataSpecAndSizes.forEach { (dataSpec, size) ->
            val view = setupView(viewProvider, dataSpec, size)
            reuseView = view
            val configurator = LayoutTesterConfigurator()
            viewProvider.setupLayoutTesters(configurator, view, dataSpec, size)
            runValidations(view, dataSpec, size, configurator.getAllTesters(), packageName, testClass, "$testName.${validationsCount++}")
        }
        if (allTestExceptions.size > 0) {
            exportReport(packageName, testClass)
            throw AssertionError("Found layout exceptions: pull report using \$adb pull /sdcard/layoutTests .")
        }
    }

    private fun exportReport(packageName: String, testClass: String) {
        var exceptionScriptTags = ""
        allTestExceptions.forEach { exception ->
            exceptionScriptTags += "<script type=\"text/javascript\" src=\"${exception.testName}.json\"></script>"
        }
        val finalVal = LayoutFile.layoutIndexFile.replace("{script-tags}", exceptionScriptTags)
        StorageHelper.storeMetaData(finalVal, "index.html", packageName, testClass)
    }

    private fun setupView(viewProvider: LayoutViewProvider, dataSpec: Map<String, Any?>, size: LayoutViewSize): View {
        return viewProvider
                .getView(context, dataSpec, size, reuseView)
                .setSize(size)
    }

    private fun runValidations(view: View,
                               dataSpec: Map<String, Any?>,
                               size: LayoutViewSize,
                               testers: Set<LayoutTester>,
                               packageName: String,
                               testClass: String,
                               testName: String) {
        val exceptions: MutableList<LayoutTestException> = ArrayList()

        testers.forEach {
            try {
                it.runTest(view, dataSpec, size)
            } catch (e: LayoutTestException) {
                exceptions += e
            }
        }
        if (exceptions.isNotEmpty()) {
            val bitmap = ScreenshotHelper.capture(view)
            errorsFound(view, dataSpec, size, bitmap, exceptions)
            val dump = LayoutHierarchyDumper.create().dumpHierarchy(view)
            val layoutTestExceptionWithDescription = LayoutTestExceptionWithDescription(packageName, testClass, testName, dataSpec, size, bitmap, dump, exceptions)
            allTestExceptions.add(layoutTestExceptionWithDescription)
            saveOnDisk(layoutTestExceptionWithDescription)
        }
    }

    private fun saveOnDisk(obj: LayoutTestExceptionWithDescription) {
        val bitmap = ScreenshotHelper.getScaledBitmap(obj.bitmap)
        StorageHelper.storeBitmap(bitmap, obj.testName, obj.appPackageName, obj.testClass)
        StorageHelper.storeMetaData("jsonData.push(${obj.toJson()})", "${obj.testName}.json", obj.appPackageName, obj.testClass)
    }

    private fun computeDataSpecAndSizes(sizesForView: Array<LayoutViewSize>, dataSpecForTest: Map<String, LayoutDataSpecValues>): List<DataSpecAndSize> {
        val combinationsOfDataSpec: List<Map<String, Any?>> = TestCombinations.getAllCombinationsOfDataSpec(dataSpecForTest)
        val dataSpecAndSizes = ArrayList<DataSpecAndSize>()

        val sizesForViewWithAtLeastOneValue = if (sizesForView.isEmpty()) arrayOf(LayoutViewSize(null, null)) else sizesForView

        sizesForViewWithAtLeastOneValue.forEach { size ->
            if (combinationsOfDataSpec.isEmpty()) {
                dataSpecAndSizes.add(DataSpecAndSize(emptyMap(), size))
            } else {
                combinationsOfDataSpec.forEach { dataSpec ->
                    dataSpecAndSizes.add(DataSpecAndSize(dataSpec, size))
                }
            }
        }
        return dataSpecAndSizes
    }

    data class DataSpecAndSize(val dataSpec: Map<String, Any?>,
                               val size: LayoutViewSize)
}
