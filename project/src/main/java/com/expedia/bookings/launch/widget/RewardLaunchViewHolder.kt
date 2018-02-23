package com.expedia.bookings.launch.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.utils.Akeakamai
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.squareup.picasso.Picasso

class RewardLaunchViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

    val backgroundImage by bindView<ImageView>(R.id.reward_image_background)
    var rewardWebViewUrl: String? = null

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(vm: RewardLaunchViewModel) {
        Picasso.with(view.context)
                .load(getResizeImageUrl(view.context.resources.getString(vm.backgroundUrlStringId)))
                .error(vm.backgroundFallbackColorId)
                .placeholder(vm.backgroundPlaceHolderResId)
                .into(backgroundImage)
        backgroundImage.setColorFilter(ContextCompat.getColor(view.context, vm.backgroundGradientColorId), PorterDuff.Mode.SRC_ATOP)
        rewardWebViewUrl = view.context.resources.getString(vm.rewardWebViewUrlStringId)
    }

    private fun getResizeImageUrl(url: String): String {
        val akeakamai = Akeakamai(url)
        akeakamai.resizeExactly(view.context.resources.getDimensionPixelSize(R.dimen.launch_big_image_card_width),
                view.context.resources.getDimensionPixelSize(R.dimen.launch_big_image_card_height))
        return akeakamai.build()
    }

    override fun onClick(view: View) {
        rewardWebViewUrl?.let { url -> goToRewardWebViewPage(view.context, url) }
    }

    private fun goToRewardWebViewPage(context: Context, url: String) {
        val builder = WebViewActivity.IntentBuilder(context)
                .setUrl(url)
                .setTitle(BuildConfig.brand)
                .setHandleBack(true)
                .setRetryOnFailure(true)
        NavUtils.startActivity(context, builder.intent, null)
    }
}
