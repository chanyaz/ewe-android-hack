package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.Bookmark
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.joda.time.LocalDate
import java.util.ArrayList

class BookmarkUtils {
    companion object {
        val BOOKED_MARKS_SHARED_PREFERENCE_KEY = "com.expedia.bookings.booked.marks"
        val BOOKMARK_SHARED_PREFERENCE_KEY = "com.expedia.bookings.bookmarks"
        val BOOKMARKS = "bookmarks"
        val BOOKED_MARKS = "booked_marks"

        val BOOKMARKS_TYPE = object : TypeToken<List<Bookmark>>() {}.type

        val BOOKEDMARKS_TYPE = object : TypeToken<HashMap<String, Bookmark>>() {}.type

        fun saveBookmark(context: Context, bookmark: Bookmark) {
            val allBookmarks = getAllBookmarks(context)
            allBookmarks.add(bookmark)
            saveAllBookmarks(context, allBookmarks)
        }

        fun saveAllBookmarks(context: Context, bookmarks: ArrayList<Bookmark>) {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, DeeplinkSharedPrefParserUtils.LOCAL_DATE_TYPE).create()
            val toJson = gson.toJson(bookmarks, BOOKMARKS_TYPE)
            val bookmarksSharedPref = context.getSharedPreferences(BOOKMARK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            bookmarksSharedPref.edit().putString(BOOKMARKS, toJson).apply()

        }

        fun getAllBookmarks(context: Context): ArrayList<Bookmark> {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, DeeplinkSharedPrefParserUtils.LOCAL_DATE_TYPE).create()

            val bookmarksSharedPref = context.getSharedPreferences(BOOKMARK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val fromJson = gson.fromJson<ArrayList<Bookmark>>(bookmarksSharedPref.getString(BOOKMARKS, "")
                    , BOOKMARKS_TYPE) ?: arrayListOf()
            return fromJson
        }

        fun saveTripBooked(context: Context, tripID: String, bookmark: Bookmark) {
            val allBookedMarks = getAllBookedMarks(context)
            allBookedMarks.put(tripID, bookmark)
            saveAllBookedMarks(context, allBookedMarks)
        }

        private fun saveAllBookedMarks(context: Context, allBookedMarks: HashMap<String, Bookmark>) {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, DeeplinkSharedPrefParserUtils.LOCAL_DATE_TYPE).create()
            val toJson = gson.toJson(allBookedMarks, BOOKEDMARKS_TYPE)
            val bookmarksSharedPref = context.getSharedPreferences(BOOKED_MARKS_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            bookmarksSharedPref.edit().putString(BOOKED_MARKS, toJson).apply()

        }

        fun getAllBookedMarks(context: Context): HashMap<String, Bookmark> {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, DeeplinkSharedPrefParserUtils.LOCAL_DATE_TYPE).create()
            val bookedMarksSharedPref = context.getSharedPreferences(BOOKED_MARKS_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val fromJson = gson.fromJson<HashMap<String,Bookmark>>(bookedMarksSharedPref.getString(BOOKED_MARKS, "")
                    , BOOKEDMARKS_TYPE) ?: hashMapOf<String, Bookmark>()
            return fromJson
        }
    }
}
