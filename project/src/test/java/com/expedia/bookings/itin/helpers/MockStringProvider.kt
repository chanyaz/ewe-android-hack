package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.StringSource

class MockStringProvider : StringSource {
    var fetchWithPhraseCalled = false
    override fun fetch(stringResource: Int): String {
        return stringResource.toString()
    }

    override fun fetchWithPhrase(stringResource: Int, map: Map<String, String>): String {
        fetchWithPhraseCalled = true
        return stringResource.toString().plus(map.toString())
    }
}
