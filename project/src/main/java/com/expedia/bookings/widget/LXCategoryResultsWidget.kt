package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.lx.LXCategoryMetadata
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.squareup.otto.Subscribe
import rx.subjects.PublishSubject
import java.util.ArrayList

public class LXCategoryResultsWidget(context: Context, attrs: AttributeSet): FrameLayout(context, attrs) {

    val recyclerView: RecyclerView by bindView(R.id.lx_category_list)
    val errorScreen: LXErrorWidget by bindView(R.id.category_error_widget)

    val adapter = LXCategoryListAdapter()
    private val CARDS_FOR_LOADING_ANIMATION = 4
    private val lxCategorySubject = PublishSubject.create<LXCategoryMetadata>()
    private val LIST_DIVIDER_HEIGHT = 14

    override fun onFinishInflate() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.scrollToPosition(0)
        recyclerView.layoutManager = layoutManager

        recyclerView.addItemDecoration(
                RecyclerDividerDecoration(context, 0, LIST_DIVIDER_HEIGHT, 0, LIST_DIVIDER_HEIGHT,
                        0, 0, false))
        recyclerView.setHasFixedSize(true)

        recyclerView.setAdapter(adapter)
        errorScreen.setVisibility(View.GONE)
        errorScreen.setToolbarVisibility(View.GONE)
    }

    public fun bind(categories: List<LXCategoryMetadata>, imageCode: String?) {
        recyclerView.setVisibility(View.VISIBLE)
        recyclerView.layoutManager.scrollToPosition(0)
        errorScreen.setVisibility(View.GONE)
        adapter.setCategories(categories, lxCategorySubject)
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
    public fun onLXSearchError(event: Events.LXShowSearchError) {
        recyclerView.setVisibility(View.GONE)
        errorScreen.bind(event.error, event.searchType)
        errorScreen.setVisibility(View.VISIBLE)
    }

    @Subscribe
    public fun onLXShowLoadingAnimation(event: Events.LXShowLoadingAnimation) {
        recyclerView.setVisibility(View.VISIBLE)
        errorScreen.setVisibility(View.GONE)
        val elements = createDummyListForAnimation()
        adapter.setDummyItems(elements)
    }

    // Create list to show cards for loading animation
    private fun createDummyListForAnimation(): List<LXCategoryMetadata> {
        val elements = ArrayList<LXCategoryMetadata>(CARDS_FOR_LOADING_ANIMATION)
        for (i in 0..CARDS_FOR_LOADING_ANIMATION - 1) {
            elements.add(LXCategoryMetadata())
        }
        return elements
    }

    fun getCategoryPublishSubject(): PublishSubject<LXCategoryMetadata> {
        return lxCategorySubject
    }
}