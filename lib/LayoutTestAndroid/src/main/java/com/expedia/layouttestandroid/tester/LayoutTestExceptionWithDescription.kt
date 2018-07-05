package com.expedia.layouttestandroid.tester

import android.graphics.Bitmap
import com.expedia.layouttestandroid.viewsize.LayoutViewSize
import org.json.JSONArray
import org.json.JSONObject

data class LayoutTestExceptionWithDescription(val appPackageName: String,
                                              val testClass: String,
                                              val testName: String,
                                              val dataSpec: Map<String, Any?>,
                                              val size: LayoutViewSize,
                                              val bitmap: Bitmap,
                                              val hierarchyDump: JSONObject,
                                              val layoutTestExceptions: List<LayoutTestException>) {

    fun toJson(): JSONObject {
        val node = JSONObject()
        node.put("appPackageName", appPackageName)
        node.put("testClass", testClass)
        node.put("testName", testName)
        node.put("size", size.toJson())
        node.put("hierarchyDump", hierarchyDump)
        val viewsJsonArray = JSONArray()
        layoutTestExceptions.forEach {
            viewsJsonArray.put(it.toJson())
        }
        node.put("layoutTestExceptions", viewsJsonArray)
        return node
    }
}
