package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.RecentSearchViewModel
import com.expedia.vm.RecentSearchesAdapterViewModel

public class RecentSearchesAdapter(val viewmodel: RecentSearchesAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val HEADER_VIEW = 0
    val RECENT_SEARCH_VIEW = 1


    override fun getItemCount(): Int {
        return viewmodel.recentSearches.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == HEADER_VIEW) {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.recent_search_header, parent, false)
            return HeaderViewHolder(parent.context,view as ViewGroup)
        } else {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.recent_search_item, parent, false)
            val vm = RecentSearchViewModel(parent.context)
            vm.recentSearchSelected.subscribe(viewmodel.recentSearchSelectedSubject)
            return RecentSearchesViewHolder(view as ViewGroup, vm)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is RecentSearchesViewHolder -> holder.vm.recentSearchObserver.onNext(viewmodel.recentSearches.get(position-1))
        }
    }

    init {
        viewmodel.recentSearchesObservable.subscribe {
            viewmodel.recentSearches = it
            notifyDataSetChanged()
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return HEADER_VIEW
        } else {
            return RECENT_SEARCH_VIEW
        }
    }

}

public class HeaderViewHolder(context: Context, view: ViewGroup) : RecyclerView.ViewHolder(view) {
    val recentSearchesTitle: TextView by view.bindView(R.id.recent_searches_title)

    init {
        val drawable = ContextCompat.getDrawable(context, R.drawable.recent_searches)
        drawable.setColorFilter(ContextCompat.getColor(context, R.color.hotel_recent_search_icon_color), PorterDuff.Mode.SRC_IN)
        recentSearchesTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }
}


public class RecentSearchesViewHolder(val root: ViewGroup, val vm : RecentSearchViewModel) : RecyclerView.ViewHolder(root), View.OnClickListener {
    val title: TextView by root.bindView(R.id.title_textview)
    val description: TextView by root.bindView(R.id.description)

    init {
        itemView.setOnClickListener(this)
        vm.titleObservable.subscribeText(title)
        vm.descriptionObservable.subscribeText(description)
    }

    override fun onClick(view: View) {
        val recentSearch = vm.recentSearchObserver.value
        vm.recentSearchSelected.onNext(recentSearch)
    }
}

