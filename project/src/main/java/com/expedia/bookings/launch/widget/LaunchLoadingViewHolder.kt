package com.expedia.bookings.launch.widget

import android.animation.ValueAnimator
import android.support.v7.widget.RecyclerView
import android.view.View

import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView

open class LaunchLoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val backgroundImageView: View by bindView(R.id.background_image_view)

    private var animation: ValueAnimator? = null

    /**
     * Loading animation that alternates between rows

     * | |____*____| |
     * | |_ _| |_ _| |
     * | |_*_| |_*_| |
     * | |____ ____| |
     * | |_*_| |_*_| |
     * | |_ _| |_ _| |
     * | etc etc etc |

     */
    fun bind() {
        when (adapterPosition % 10) {
            0, 3, 4, 6, 7 -> animation = AnimUtils.setupLoadingAnimation(backgroundImageView, true)
            else -> animation = AnimUtils.setupLoadingAnimation(backgroundImageView, false)
        }
    }

	fun cancelAnimation() {
		animation?.removeAllUpdateListeners()
		animation?.cancel()
	}
}
