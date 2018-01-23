package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.lx.LXTheme
import com.expedia.bookings.data.lx.LXThemeType
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXThemeListAdapter
import com.expedia.bookings.widget.LXThemeResultsWidget
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class) class LXThemeResultsWidgetTest {
    private var themeResultsWidget: LXThemeResultsWidget by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        activity.setTheme(R.style.V2_Theme_LX)
        themeResultsWidget = LayoutInflater.from(activity).inflate(R.layout.widget_theme_results, null) as LXThemeResultsWidget
    }

    @Test fun themesDescriptionAndTitleUpdated() {
        val themes = createThemes()
        themeResultsWidget.bind(themes, "SFO")

        assertEquals("All Things To Do", themes[0].title)
        assertEquals("Adventure Around", themes[1].title)
        assertEquals("A list of all of the available activities for your selected dates.", themes[0].description)
        assertEquals("Explore, take day trips and find great excursions at your destination", themes[1].description)
    }

    @Test fun categoryResultList() {
        val themes = createThemes()
        themeResultsWidget.bind(themes, "SFO")

        assertEquals(0, themeResultsWidget.recyclerView.visibility)
        assertEquals(2, themeResultsWidget.recyclerView.adapter.itemCount)

        val holder = themeResultsWidget.recyclerView.adapter.createViewHolder(themeResultsWidget.recyclerView, 1) as LXThemeListAdapter.ViewHolder
        themeResultsWidget.recyclerView.adapter.bindViewHolder(holder, 0)
        assertEquals("All Things To Do", holder.themeTitle.text)

        themeResultsWidget.recyclerView.adapter.bindViewHolder(holder, 1)
        assertEquals("Explore, take day trips and find great excursions at your destination", holder.themeDescription.text)
        assertEquals(0, holder.themeTitle.visibility)
        assertEquals(0, holder.themeDescription.visibility)
    }

    private fun createThemes(): ArrayList<LXTheme> {
        val allThingsToDoTheme = LXTheme(LXThemeType.AllThingsToDo)
        val adventureAroundTheme = LXTheme(LXThemeType.AdventureAround)

        val themes = ArrayList<LXTheme>()
        themes.add(allThingsToDoTheme)
        themes.add(adventureAroundTheme)
        return themes
    }
}
