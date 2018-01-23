package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.lx.LXSearchPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.lob.lx.ui.viewmodel.LXSearchViewModel
import com.expedia.bookings.widget.shared.SearchInputTextView
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class LXSearchTest {

    var vm: LXSearchViewModel by Delegates.notNull()
    var activity: Activity by Delegates.notNull()
    var context: Context? = null
    var inflater: LayoutInflater by Delegates.notNull()
    var searchwidget: LXSearchPresenter by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()
        vm = LXSearchViewModel(activity)
        inflater = activity.layoutInflater
        searchwidget = activity.layoutInflater.inflate(R.layout.test_lx_search_presenter, null) as LXSearchPresenter
        searchwidget.searchViewModel = vm
    }

    @Test
    fun testSearchFormComponentVisibility() {
        val selectDate = searchwidget.findViewById<View>(R.id.calendar_card)
        val locationTextView = searchwidget.findViewById<View>(R.id.destination_card) as SearchInputTextView
        val searchButton = searchwidget.findViewById<View>(R.id.search_btn) as Button
        val toolbarSearchText = searchwidget.findViewById<View>(R.id.title) as TextView
        assertEquals("Search Activities", toolbarSearchText.text.toString())
        assertEquals(View.VISIBLE.toLong(), searchButton.visibility.toLong())
        assertEquals(View.VISIBLE.toLong(), locationTextView.visibility.toLong())
        assertEquals("Location", locationTextView.text.toString())
        assertEquals(View.VISIBLE.toLong(), selectDate.visibility.toLong())
    }
}
