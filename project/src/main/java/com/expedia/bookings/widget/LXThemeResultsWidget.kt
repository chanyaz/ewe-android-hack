package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.lx.LXTheme
import com.expedia.bookings.data.lx.LXThemeType
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.bindView
import com.squareup.otto.Subscribe
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class LXThemeResultsWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val recyclerView: RecyclerView by bindView(R.id.lx_theme_list)
    val errorScreen: LXErrorWidget by bindView(R.id.theme_error_widget)

    val adapter = LXThemeListAdapter()
    private val CARDS_FOR_LOADING_ANIMATION = 4
    private val themeClickSubject = PublishSubject.create<LXTheme>()
    private val LIST_DIVIDER_HEIGHT = 14

    override fun onFinishInflate() {
        super.onFinishInflate()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.scrollToPosition(0)
        recyclerView.layoutManager = layoutManager

        recyclerView.addItemDecoration(
                RecyclerDividerDecoration(context, 0, LIST_DIVIDER_HEIGHT, 0, LIST_DIVIDER_HEIGHT,
                        0, 0, false))
        recyclerView.setHasFixedSize(true)

        recyclerView.adapter = adapter
        errorScreen.visibility = View.GONE
        errorScreen.setToolbarVisibility(View.GONE)
    }

    fun bind(categories: List<LXTheme>, imageCode: String?) {
        updateThemeTitleAndDescription(categories)
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager.scrollToPosition(0)
        errorScreen.visibility = View.GONE
        adapter.setThemes(categories, themeClickSubject)
        adapter.setDestinationImageCode(imageCode)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Events.register(this)
    }

    override fun onDetachedFromWindow() {
        Events.unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe
    fun onLXSearchError(event: Events.LXShowSearchError) {
        recyclerView.visibility = View.GONE
        errorScreen.bind(event.error, event.searchType)
        errorScreen.visibility = View.VISIBLE
    }

    @Subscribe
    fun onLXShowLoadingAnimation(@Suppress("UNUSED_PARAMETER") event: Events.LXShowLoadingAnimation) {
        recyclerView.visibility = View.VISIBLE
        errorScreen.visibility = View.GONE
        val elements = createDummyListForAnimation()
        adapter.setDummyItems(elements)
    }

    // Create list to show cards for loading animation
    private fun createDummyListForAnimation(): List<LXTheme> {
        val elements = ArrayList<LXTheme>(CARDS_FOR_LOADING_ANIMATION)
        for (i in 0..CARDS_FOR_LOADING_ANIMATION - 1) {
            elements.add(LXTheme(LXThemeType.AllThingsToDo))
        }
        return elements
    }

    fun getThemePublishSubject(): PublishSubject<LXTheme> {
        return themeClickSubject
    }

    fun updateThemeTitleAndDescription(themes: List<LXTheme>): List<LXTheme> {

        for (theme in themes) {
            when (theme.themeType!!) {
                LXThemeType.AllThingsToDo -> {
                    theme.title = context.resources.getString(R.string.lx_category_all_things_to_do)
                    theme.description = context.resources.getString(R.string.all_things_to_do_description)
                    theme.titleEN = context.resources.getString(R.string.lx_category_key_all_things_to_do)
                }
                LXThemeType.AdventureAround -> {
                    theme.title = context.resources.getString(R.string.adventure_around)
                    theme.description = context.resources.getString(R.string.adventure_around_description)
                    theme.titleEN = context.resources.getString(R.string.adventure_around_en)
                }
                LXThemeType.EatPlayEnjoy -> {
                    theme.title = context.resources.getString(R.string.eat_play_enjoy)
                    theme.description = context.resources.getString(R.string.eat_play_enjoy_description)
                    theme.titleEN = context.resources.getString(R.string.eat_play_enjoy_en)
                }
                LXThemeType.FunForTheFamily -> {
                    theme.title = context.resources.getString(R.string.fun_for_the_family)
                    theme.description = context.resources.getString(R.string.fun_for_the_family_description)
                    theme.titleEN = context.resources.getString(R.string.fun_for_the_family_en)
                }
                LXThemeType.PureEntertainment -> {
                    theme.title = context.resources.getString(R.string.pure_entertainment)
                    theme.description = context.resources.getString(R.string.pure_entertainment_description)
                    theme.titleEN = context.resources.getString(R.string.pure_entertainment_en)
                }
                LXThemeType.SeeTheSights -> {
                    theme.title = context.resources.getString(R.string.see_the_sights)
                    theme.description = context.resources.getString(R.string.see_the_sights_description)
                    theme.titleEN = context.resources.getString(R.string.see_the_sights_en)
                }
                LXThemeType.TopRatedActivities -> {
                    theme.title = context.resources.getString(R.string.top_rated_activities)
                    theme.description = context.resources.getString(R.string.top_rated_activities_description)
                    theme.titleEN = context.resources.getString(R.string.top_rated_activities_en)
                }
                LXThemeType.TourTheArea -> {
                    theme.title = context.resources.getString(R.string.tour_the_area)
                    theme.description = context.resources.getString(R.string.tour_the_area_description)
                    theme.titleEN = context.resources.getString(R.string.tour_the_area_en)
                }
                else -> {
                    // No theme
                }
            }
        }
        return themes
    }
}
