package com.expedia.bookings.test.stepdefs.phone.model

import java.util.HashMap

data class ApiRequestData(
    val queryParams: HashMap<String, List<String>>,
    val formData: HashMap<String, String>
)
