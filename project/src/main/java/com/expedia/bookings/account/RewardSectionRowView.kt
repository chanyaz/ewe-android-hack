package com.expedia.bookings.account

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import rx.subjects.BehaviorSubject

open class RewardSectionRowView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs){
    val rowTitle: TextView by bindView(R.id.reward_row_title)
    val currentAmount: TextView by bindView(R.id.reward_row_current_amount)
    val totalAmount: TextView by bindView(R.id.reward_row_total_amount)
    val leftAmount: TextView by bindView(R.id.reward_row_left_amount)
    val progressBar: ProgressBar by bindView(R.id.reward_progress_bar)
    var targetProgressBarValue = 0

    fun progressBarAnimate() {
        println(progressBar.progress)
        if (!progressBar.progress.equals(targetProgressBarValue)) {
            val progressAnimator: ObjectAnimator
            progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgressBarValue)
            progressAnimator.duration = 800
            progressAnimator.startDelay = 2000
            progressAnimator.start()
        }
    }
}
