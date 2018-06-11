package com.expedia.account.util

class MockStringSource : StringSource {
    override fun getString(resId: Int): String {
        return resId.toString()
    }

    override fun getBrandedString(resId: Int, brand: String): String {
        return brand + resId
    }
}
