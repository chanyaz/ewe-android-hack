package com.expedia.bookings.luggagetags

import java.io.Serializable

data class ExpediaLuggageTags(val tagID: String = "",
                              val expediaUserID: String = "",
                              val public: Boolean = false,
                              val name: String = "",
                              val address: String = "",
                              val phoneNumber: String = "") : Serializable
