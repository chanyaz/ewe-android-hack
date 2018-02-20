package com.expedia.bookings.data.flights

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

@Entity(primaryKeys = arrayOf("sourceAirportCode", "destinationAirportCode", "isRoundTrip"),
        tableName = "flight_recent_searches")
data class RecentSearch(
        val sourceAirportCode: String,
        val destinationAirportCode: String,
        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        val sourceSuggestion: ByteArray,
        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        val destinationSuggestion: ByteArray,
        val startDate: String,
        val endDate: String,
        val flightClass: String,
        val dateSearchedOn: Long,
        val amount: Long,
        val currencyCode: String,
        val adultTravelerCount: Int,
        val childTraveler: String,
        val isInfantInLap: Boolean,
        val isRoundTrip: Boolean)
