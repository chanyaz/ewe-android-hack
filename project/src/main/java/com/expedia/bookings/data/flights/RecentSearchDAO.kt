package com.expedia.bookings.data.flights

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable

@Dao
abstract class RecentSearchDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(recentSearch: RecentSearch)

    @Query("SELECT * FROM flight_recent_searches ORDER BY dateSearchedOn asc LIMIT 1")
    abstract fun getOldestRecentSearch() : RecentSearch

    @Delete
    abstract fun delete(recentSearch: RecentSearch)

    @Query("SELECT count(*) FROM flight_recent_searches WHERE sourceAirportCode = :sourceAirportCode AND destinationAirportCode = :destinationAirportCode AND startDate = :startDate AND endDate = :endDate AND flightClass = :flightClass")
    abstract fun checkIfExist(sourceAirportCode: String, destinationAirportCode: String, startDate: String, endDate: String, flightClass: String): Int

    @Query("SELECT count(*) FROM flight_recent_searches")
    abstract fun count(): Int

    @Query("SELECT * FROM flight_recent_searches ORDER BY dateSearchedOn desc")
    abstract fun loadAll(): Flowable<List<RecentSearch>>

    @Transaction
    open fun deleteAndInsertTransaction(oldRecentSearch: RecentSearch, newRecentSearch: RecentSearch) {
        delete(oldRecentSearch)
        insert(newRecentSearch)
    }

}
