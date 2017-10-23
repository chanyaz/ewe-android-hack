package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.Bookmark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookmarkUtils {
    companion object {
        val BOOKMARK_SHARED_PREFERENCE_KEY = "com.expedia.bookings.bookmarks"
        val BOOKMARKS = "bookmarks"
        val TYPE = object : TypeToken<List<Bookmark>>() {}.type

        fun saveBookmark(context: Context, bookmark: Bookmark) {
            val allBookmarks = getAllBookmarks(context)
            allBookmarks.add(bookmark)
            saveAllBookmarks(context, allBookmarks)
        }

        private fun saveAllBookmarks(context: Context, bookmarks: ArrayList<Bookmark>) {
            val gson = Gson()
            val toJson = gson.toJson(bookmarks, TYPE)
            val bookmarksSharedPref = context.getSharedPreferences(BOOKMARK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            bookmarksSharedPref.edit().putString(BOOKMARKS, toJson).apply()

        }

        fun getAllBookmarks(context: Context): ArrayList<Bookmark> {
            val gson = Gson()
            val bookmarksSharedPref = context.getSharedPreferences(BOOKMARK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val fromJson = gson.fromJson<ArrayList<Bookmark>>(bookmarksSharedPref.getString(BOOKMARKS, "")
                    , TYPE) ?: arrayListOf()
            return fromJson
        }

    }
}
