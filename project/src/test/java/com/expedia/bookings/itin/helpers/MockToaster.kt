package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IToaster

class MockToaster : IToaster {
    var toasted = false
    override fun toastAndCopy(message: CharSequence) {
        toasted = true
    }
}
