package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.util.notNullAndObservable
import com.expedia.vm.launch.LobToolbarViewModel

class LobToolbarWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val recyclerView: RecyclerView by bindView(R.id.lob_recycler_view)

    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    private val lobPositionCache = Ui.getApplication(context).appComponent().searchLobToolbarCache()

    var viewModel: LobToolbarViewModel by notNullAndObservable {
        setupRecyclerView()
    }

    init {
        View.inflate(context, R.layout.widget_lob_toolbar, this)
        recyclerView.layoutManager = layoutManager
    }


    private fun setupRecyclerView() {
        val adapter = LobToolbarAdapter(viewModel.defaultLob)
        recyclerView.adapter = adapter
        adapter.setLob(viewModel.supportedLobs)

        scrollToSavedPosition()

        adapter.toolbarItemClickedSubject.subscribe { lob ->
            storePosition(viewModel.getLobPosition(lob))
            viewModel.lobSelectedSubject.onNext(lob)
        }
    }

    private fun scrollToSavedPosition() {
        val defaultLobPosition = viewModel.getLobPosition(viewModel.defaultLob)
        if (lobPositionCache.itemPosition != defaultLobPosition) {
            layoutManager.scrollToPosition(defaultLobPosition)
        } else {
            layoutManager.scrollToPositionWithOffset(lobPositionCache.itemPosition,
                    lobPositionCache.offset)
        }
    }

    private fun storePosition(selectedPosition: Int) {
        lobPositionCache.itemPosition = selectedPosition

        lobPositionCache.offset = 0
        if (selectedPosition > layoutManager.findLastCompletelyVisibleItemPosition()) {
            lobPositionCache.offset = width - layoutManager.findViewByPosition(selectedPosition).width
        } else if (selectedPosition >= layoutManager.findFirstCompletelyVisibleItemPosition()) {
            lobPositionCache.offset = layoutManager.findViewByPosition(selectedPosition).left
        }
    }
}
