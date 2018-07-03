package com.expedia.layouttestandroid.viewsize

import org.json.JSONObject

open class LayoutViewSize(val width: Int?,
                          val height: Int?,
                          val density: Int = 1) {
    companion object {
        @JvmStatic
        val Nexus6P = LayoutViewSize(1440, 2560)
        @JvmStatic
        val Nexus5X = LayoutViewSize(1080, 1920)
        @JvmStatic
        val GooglePixel = LayoutViewSize(1080, 1920)
        @JvmStatic
        val GooglePixelXL = LayoutViewSize(1440, 2560)
        @JvmStatic
        val Nexus7Tablet = LayoutViewSize(1920, 1200)
        @JvmStatic
        val Nexus9Tablet = LayoutViewSize(2048, 1536)
        @JvmStatic
        val SamsungGalaxyTablet10 = LayoutViewSize(1280, 800)
        @JvmStatic
        val ChromebookPixel = LayoutViewSize(1700, 2560)
    }

    fun toJson(): JSONObject {
        val node = JSONObject()
        node.put("width", width)
        node.put("height", height)
        node.put("density", density)
        return node
    }

    val computedWidth: Int? by lazy {
        val width = this.width ?: return@lazy null
        width * density
    }

    val computedHeight: Int? by lazy {
        val height = this.height ?: return@lazy null
        height * density
    }
}