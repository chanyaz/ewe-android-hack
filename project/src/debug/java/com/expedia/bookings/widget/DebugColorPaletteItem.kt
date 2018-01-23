package com.expedia.bookings.widget

class DebugColorPaletteItem(val type: Int = COLOR, val title: String? = null, val colorId: Int? = null) {
    companion object Type {
        @JvmStatic val TITLE = 0
        @JvmStatic val COLOR = 1
    }
}
