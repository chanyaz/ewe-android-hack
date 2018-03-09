package com.expedia.bookings.itin.utils

import android.content.Context
import com.squareup.phrase.Phrase

class StringProvider(val context: Context): StringSource {

    override fun fetch(stringResource: Int): String {
        return context.resources.getText(stringResource).toString()
    }

    override fun fetch(stringResource: Int, map: Map<String, String>): String {
        val string = Phrase.from(context, stringResource)
        map.forEach {
            string.put(it.key, it.value)
        }
        return string.format().toString()
    }
}
