package com.expedia.bookings.widget.flights

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.vm.flights.RecentSearchViewModel

class RecentSearchWidgetContainer(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val recyclerView: RecyclerView by bindView(R.id.recent_search_recyclerview)
    private val recentSearchWidget: LinearLayout by bindView(R.id.recent_search_widget)
    private val recentSearchHeaderContainer: LinearLayout by bindView(R.id.recent_search_header_container)
    private val abacusVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsRecentSearch)
    private val recentSearchChevron: ImageView by bindView(R.id.recent_search_header_chevron)

    val viewModel by lazy {
        val recentSearchViewModel = RecentSearchViewModel(context)
        recentSearchViewModel
    }

    private val recyclerViewLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            if (recyclerView.adapter.itemCount > 0) {
                val layoutParams = recyclerView.layoutParams as android.widget.LinearLayout.LayoutParams
                recyclerView.layoutParams
                layoutParams.height = recyclerView.adapter.itemCount *
                        (recyclerView.getChildAt(0).height
                                + resources.getDimensionPixelSize(R.dimen.flight_recent_search_item_margin_bottom))
                recyclerView.layoutParams = layoutParams
            }
        }
    }

    init {
        View.inflate(context, R.layout.recent_search_widget, this)

        if (abacusVariant == AbacusVariant.ONE.value) {
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(recyclerViewLayoutListener)
        }
        recyclerView.adapter = RecentSearchListAdapter(viewModel.recentSearchesObservable, context)
        recentSearchHeaderContainer.setOnClickListener {
            if (isRecentSearchViewExpanded()) {
                collapsedRecentSearchView()
            } else {
                expandedRecentSearchView()
            }
        }
        AnimUtils.rotate(recentSearchChevron)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (abacusVariant == AbacusVariant.TWO.value) {
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun collapsedRecentSearchView() {
        recyclerView.visibility = View.GONE
        AnimUtils.reverseRotate(recentSearchChevron)
    }

    fun expandedRecentSearchView() {
        recyclerView.visibility = View.VISIBLE
        AnimUtils.rotate(recentSearchChevron)
    }

    fun isRecentSearchViewExpanded(): Boolean {
        return recyclerView.visibility == View.VISIBLE
    }
}
