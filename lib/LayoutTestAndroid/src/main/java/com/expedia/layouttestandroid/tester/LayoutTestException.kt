package com.expedia.layouttestandroid.tester

import android.view.View
import com.expedia.layouttestandroid.extension.hashCodeString
import org.json.JSONArray
import org.json.JSONObject

data class LayoutTestException(override val message: String, val views: List<View>, val extra: Any? = null) : Exception(message) {
    fun toJson(): JSONObject {
        val node = JSONObject()
        node.put("message", message)
        node.put("extra", extra)
        val viewsJsonArray = JSONArray()
        views.forEach {
            val childViewDetails = JSONObject()
            childViewDetails.put("class", it.javaClass.canonicalName)
            childViewDetails.put("hashCode", it.hashCodeString())
            viewsJsonArray.put(childViewDetails)
        }
        node.put("views", viewsJsonArray)
        return node
    }
}
