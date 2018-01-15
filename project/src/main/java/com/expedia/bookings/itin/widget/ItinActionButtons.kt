package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.ItinActionButtonsViewModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility

class ItinActionButtons(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    val leftButton: LinearLayout by bindView(R.id.itin_action_button_left)
    val leftButtonText: TextView by bindView(R.id.itin_action_button_left_text)
    val rightButton: LinearLayout by bindView(R.id.itin_action_button_right)
    val rightButtonText: TextView by bindView(R.id.itin_action_button_right_text)
    val divider: View by bindView(R.id.itin_action_vertical_divider)

    val viewModel by lazy {
        val itinActionButtonsViewModel = ItinActionButtonsViewModel()

        itinActionButtonsViewModel.leftButtonVisibilityObservable.subscribeVisibility(leftButton)
        itinActionButtonsViewModel.rightButtonVisibilityObservable.subscribeVisibility(rightButton)
        itinActionButtonsViewModel.leftButtonTextObservable.subscribe { text ->
            leftButtonText.text = text
            AccessibilityUtil.appendRoleContDesc(leftButton, text, R.string.accessibility_cont_desc_role_button)
        }
        itinActionButtonsViewModel.rightButtonTextObservable.subscribe { text ->
            rightButtonText.text = text
            AccessibilityUtil.appendRoleContDesc(rightButton, text, R.string.accessibility_cont_desc_role_button)
        }
        itinActionButtonsViewModel.leftButtonDrawableObservable.subscribe {
            leftButtonText.setTintedDrawable(context.getDrawable(it), ContextCompat.getColor(context, R.color.exp_launch_blue))
        }
        itinActionButtonsViewModel.rightButtonDrawableObservable.subscribe {
            rightButtonText.setTintedDrawable(context.getDrawable(it), ContextCompat.getColor(context, R.color.exp_launch_blue))
        }
        itinActionButtonsViewModel.dividerVisibilityObservable.subscribeVisibility(divider)
        leftButton.subscribeOnClick(itinActionButtonsViewModel.leftButtonClickedObservable)
        rightButton.subscribeOnClick(itinActionButtonsViewModel.rightButtonClickedObservable)

        itinActionButtonsViewModel
    }

    init {
        View.inflate(context, R.layout.widget_itin_action_buttons, this)
    }
}