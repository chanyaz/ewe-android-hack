package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IPhoneHandler

class MockPhoneHandler : IPhoneHandler {
    var handleCalled = false
    override fun handle(phoneNumber: String) {
        handleCalled = true
    }
}
