package com.expedia.bookings.widget.flights

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.flights.RecentSearch
import io.reactivex.subjects.PublishSubject

class RecentSearchListAdapter(recentSearchesObservable: PublishSubject<List<RecentSearch>>, val context: Context) : RecyclerView.Adapter<RecentSearchViewHolder>() {

    private val abacusVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsRecentSearch)

    private val TYPE_VERTICAL = 1
    private val TYPE_HORIZONTAL = 2
    private val recentSearches = arrayListOf<RecentSearch>()

    init {
        recentSearchesObservable.subscribe { recentSearchList ->
            recentSearches.clear()
            recentSearches.addAll(recentSearchList)
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (abacusVariant == AbacusVariant.TWO.value) {
            return TYPE_HORIZONTAL
        } else {
            return TYPE_VERTICAL
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view: View
        val viewInfalter = LayoutInflater.from(parent.context)
        if (viewType == TYPE_HORIZONTAL) {
            view = viewInfalter.inflate(R.layout.flight_recent_search_card_horizontal, parent, false)
        } else {
            view = viewInfalter.inflate(R.layout.flight_recent_search_card_vertical, parent, false)
        }
        return RecentSearchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recentSearches.size
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder?, position: Int) {
        holder?.viewModel?.recentSearchObservable?.onNext(recentSearches[position])
    }
}
