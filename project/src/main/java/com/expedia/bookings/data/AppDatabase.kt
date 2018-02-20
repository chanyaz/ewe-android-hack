package com.expedia.bookings.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.data.flights.RecentSearchDAO

@Database(entities = arrayOf(RecentSearch::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDAO(): RecentSearchDAO
}
