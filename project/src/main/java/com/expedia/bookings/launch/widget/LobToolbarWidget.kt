package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.util.notNullAndObservable
import com.expedia.vm.launch.LobToolbarViewModel

class LobToolbarWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val recyclerView: RecyclerView by bindView(R.id.lob_recycler_view)

    var viewModel: LobToolbarViewModel by notNullAndObservable {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = LobToolbarAdapter(viewModel.defaultLob)
        recyclerView.adapter = adapter
        adapter.setLob(viewModel.getSupportedLobs())

        adapter.toolbarItemClickedSubject.subscribe(viewModel.lobSelectedSubject)
    }

    init {
        View.inflate(context, R.layout.widget_lob_toolbar, this)
    }
}
