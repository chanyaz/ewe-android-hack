package com.expedia.bookings.luggagetags

data class ExpediaLuggageTags(val tagID: String,
                              val isRegistered: Boolean,
                              val expediaUserID: String,
                              val isPublic: Boolean,
                              val name: String,
                              val address: String,
                              val phoneNumber: String)
