package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.StringSource

class MockStringProvider : StringSource {
    var lastSeenFetchArgs: Int? = null
    override fun fetch(stringResource: Int): String {
        lastSeenFetchArgs = stringResource
        return "someString"
    }

    var lastSeenFetchWithMapArgs: Pair<Int, Map<String, String>>? = null
    override fun fetch(stringResource: Int, map: Map<String, String>): String {
        lastSeenFetchWithMapArgs = Pair(stringResource, map)
        return "somePhraseString"
    }
}
