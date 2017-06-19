package com.expedia.bookings.luggagetags

import com.google.firebase.database.FirebaseDatabase

class LuggageTagsNetwork {

    val dataBase = FirebaseDatabase.getInstance()

    companion object {
        lateinit var instance: LuggageTagsNetwork
    }

    init {
        instance = this
    }

    fun pushExpediaLuggageTagToFirebase(luggageTag: ExpediaLuggageTags) {
        val luggageTagsReference = dataBase.getReference("ExpediaLuggageTags")
        luggageTagsReference.setValue(luggageTag)
    }

    fun pushExpediaLuggageTagScanToFirebase(luggageTagScan: ExpediaLuggageTagScan) {
        val luggageTagsReference = dataBase.getReference("ExpediaLuggageTagScan")
        luggageTagsReference.setValue(luggageTagScan)
    }
}