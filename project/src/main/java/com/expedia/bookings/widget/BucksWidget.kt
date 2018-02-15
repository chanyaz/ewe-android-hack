package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeChecked
import com.expedia.bookings.extensions.subscribeOnCheckChanged
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.interfaces.IBucksViewModel
import javax.inject.Inject

class BucksWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val bucksMessage: TextView by bindView(R.id.bucks_message_view)
    val bucksSwitchView: Switch by bindView(R.id.bucks_switch)
    val payWithRewardMessage: TextView by bindView(R.id.pay_with_reward_message)

    var viewModel: IBucksViewModel by notNullAndObservable {
        it.bucksWidgetVisibility.subscribeVisibility(this)
        it.bucksMessage.subscribeText(bucksMessage)
        bucksSwitchView.subscribeOnCheckChanged(it.bucksOpted)
        it.pointsAppliedMessageColor.subscribeTextColor(bucksMessage)
        it.payWithRewardsMessage.subscribeText(payWithRewardMessage)
        it.updateToggle.subscribeChecked(bucksSwitchView)
    }
        @Inject set

    init {
        View.inflate(getContext(), R.layout.bucks_widget, this)
        Ui.getApplication(context).hotelComponent().inject(this)
    }
}
