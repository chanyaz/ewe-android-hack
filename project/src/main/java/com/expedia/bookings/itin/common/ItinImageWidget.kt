package com.expedia.bookings.itin.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.itin.scopes.ItinImageViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class ItinImageWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr), ItinImageViewModelSetter {
    override fun setupViewModel(vm: ItinImageViewModel) {
        viewModel = vm
    }

    val itinImage: ImageView by bindView(R.id.itin_image_view)
    val itinName: TextView by bindView(R.id.itin_name)

    var viewModel: ItinImageViewModel by notNullAndObservable { vm ->
        vm.imageUrlSubject.subscribe { url ->
            PicassoHelper.Builder(itinImage)
                    .setPlaceholder(R.drawable.room_fallback)
                    .fit()
                    .centerCrop()
                    .build()
                    .load(url)
        }
        vm.nameSubject.subscribeTextAndVisibility(itinName)
    }

    init {
        View.inflate(context, R.layout.itin_image_widget, this)
    }
}
