package com.expedia.bookings.launch.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Bookmark
import com.expedia.bookings.utils.BookmarkUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BookmarksListAdapter
import com.expedia.bookings.widget.BookmarksListRecyclerView
import com.expedia.bookings.widget.ScrollView
import java.util.*

class BookmarksLaunchFragment: Fragment() {

    val scrollContainer: ScrollView by bindView(R.id.scroll_container)
    val recyclerView: BookmarksListRecyclerView by bindView(R.id.list_view)
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bookmarks, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookmarks = BookmarkUtils.getAllBookmarks(context)
        val adapter = BookmarksListAdapter(bookmarks)

        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        recyclerView.adapter = adapter
    }

    fun smoothScrollToTop() {
        scrollContainer.smoothScrollTo(0, 0)
    }
}