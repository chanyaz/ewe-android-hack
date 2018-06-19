package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils

class MockTripFolderFileJsonUtil : ITripsJsonFileUtils {
    var writeToFileCalled = false
    var readFromFileCalled = false
    var deleteFileCalled = false
    var deleteAllFilesCalled = false
    var readFromFileDirectoryCalled = false
    var lastFileName = ""

    override fun writeToFile(filename: String?, content: String?) {
        writeToFileCalled = true
        lastFileName = filename!!
    }

    override fun readFromFile(filename: String?): String? {
        readFromFileCalled = true
        lastFileName = filename!!
        return ""
    }

    override fun deleteFile(filename: String?): Boolean {
        deleteFileCalled = true
        lastFileName = filename!!
        return true
    }

    override fun deleteAllFiles() {
        deleteAllFilesCalled = true
    }

    override fun readFromFileDirectory(): List<String> {
        readFromFileDirectoryCalled = true
        return emptyList()
    }
}
