package com.expedia.layouttestandroid.dataspecs

object DataSpecNonNullStringValues : LayoutDataSpecValues {
    override fun values(): Array<Any?> {
        return arrayOf("Normal length string",
                "",
                "Very long string. This string is so long that it's longer than I want it to be. Just a really really long string. In fact, it's just long as long can be. I'm just lengthening the longest string by making it longer with more characters. Do you think this is long enough yet? I think so, so I'm going to stop making this long string longer by adding more characters.",
                "漢語 ♔ 🚂 ☎ ‰ 🚀 Here are some more special characters ˆ™£‡‹·Ú‹›`å∑œ´®∆ƒ∆√˜Ω≥µ˜ƒª•")
    }
}
