package com.expedia.bookings.widget.flights

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.flights.RecentSearchViewModel

class RecentSearchWidgetContainer(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val recyclerView: RecyclerView by bindView(R.id.recent_search_recyclerview)
    private val recentSearchWidget: LinearLayout by bindView(R.id.recent_search_widget)
    private val recentSearchHeaderContainer: LinearLayout by bindView(R.id.recent_search_header_container)
    private val abacusVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsRecentSearch)
    private val recentSearchChevron: ImageView by bindView(R.id.recent_search_header_chevron)
    private val appDB = Ui.getApplication(context).appComponent().provideAppDatabase()

    val viewModel by lazy {
        val recentSearchViewModel = RecentSearchViewModel(context, appDB.recentSearchDAO())
        recentSearchViewModel.recentSearchVisibilityObservable.subscribeVisibility(recentSearchWidget)
        recentSearchViewModel
    }

    init {
        View.inflate(context, R.layout.recent_search_widget, this)
        if (abacusVariant == AbacusVariant.ONE.value) {
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.setHasFixedSize(false)
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
