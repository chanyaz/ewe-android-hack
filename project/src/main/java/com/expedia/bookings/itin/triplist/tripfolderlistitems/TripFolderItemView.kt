package com.expedia.bookings.itin.triplist.tripfolderlistitems

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.itin.utils.StringProvider
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class TripFolderItemView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val folderTitle: TextView by bindView(R.id.trip_folder_list_item_title)
    val folderTiming: TextView by bindView(R.id.trip_folder_list_item_timing)
    val folderLobIconContainer: LinearLayout by bindView(R.id.trip_folder_list_item_lob_icon_container)

    var viewModel: ITripFolderItemViewModel by notNullAndObservable { vm ->
        vm.titleSubject.subscribeText(folderTitle)
        vm.timingSubject.subscribeText(folderTiming)
        vm.lobIconSubject.subscribe { products ->
            folderLobIconContainer.removeAllViews()
            products.forEach {
                val icon = TripFolderLobIconView(context, attrs)
                icon.viewModel = TripFolderLobIconViewModel()
                icon.viewModel.tripFolderProductSubject.onNext(it)

                folderLobIconContainer.addView(icon)
            }
        }
    }

    fun setViewModel() {
        this.viewModel = TripFolderItemViewModel(StringProvider(context))
    }
}
