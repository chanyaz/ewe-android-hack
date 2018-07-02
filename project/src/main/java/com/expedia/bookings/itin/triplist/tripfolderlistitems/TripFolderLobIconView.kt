package com.expedia.bookings.itin.triplist.tripfolderlistitems

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolderProduct
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.PublishSubject

class TripFolderLobIconView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val lobIconImageView: ImageView by bindView(R.id.trip_folder_lob_icon_image_view)

    var viewModel: TripFolderLobIconViewModel by notNullAndObservable { vm ->
        vm.tripFolderProductSubject.subscribe {
            when (it) {
                TripFolderProduct.HOTEL -> lobIconImageView.setImageResource(R.drawable.trip_folders_hotel_lob_icon)
                TripFolderProduct.FLIGHT -> lobIconImageView.setImageResource(R.drawable.trip_folders_flight_lob_icon)
                TripFolderProduct.CAR -> lobIconImageView.setImageResource(R.drawable.trip_folders_car_lob_icon)
                TripFolderProduct.ACTIVITY -> lobIconImageView.setImageResource(R.drawable.trip_folders_activity_lob_icon)
                TripFolderProduct.CRUISE -> lobIconImageView.setImageResource(R.drawable.trip_folders_cruise_lob_icon)
                else -> lobIconImageView.setImageResource(R.drawable.trip_folders_rail_lob_icon)
            }
        }
    }

    init {
        View.inflate(context, R.layout.trip_folder_lob_icon_view, this)
    }
}

class TripFolderLobIconViewModel {
    val tripFolderProductSubject: PublishSubject<TripFolderProduct> = PublishSubject.create()
}
