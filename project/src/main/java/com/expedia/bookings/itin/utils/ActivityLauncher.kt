package com.expedia.bookings.itin.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.ActivityOptionsCompat
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinExpandedMapViewModel.MapUri
import com.google.android.gms.maps.model.LatLng

class ActivityLauncher(val context: Context) : IActivityLauncher {
    override fun launchActivity(intentable: Intentable, id: String, animationDirection: AnimationDirection) {
        when (animationDirection) {
            AnimationDirection.SLIDE_RIGHT -> context.startActivity(intentable.createIntent(context, id), slideRightAnimation)
            AnimationDirection.SLIDE_UP -> context.startActivity(intentable.createIntent(context, id), slideUpAnimation)
        }
    }

    override fun launchActivity(intentable: IntentableWithType, id: String, animationDirection: AnimationDirection, itinType: String) {
        when (animationDirection) {
            AnimationDirection.SLIDE_RIGHT -> context.startActivity(intentable.createIntent(context, id, itinType), slideRightAnimation)
            AnimationDirection.SLIDE_UP -> context.startActivity(intentable.createIntent(context, id, itinType), slideUpAnimation)
        }
    }

    override fun launchExternalMapActivity(data: MapUri) {
        val uri = buildUri(data.latLng, data.title)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
        intent.flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
        intent.data = uri
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.itin_hotel_map_directions_no_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    val slideRightAnimation = ActivityOptionsCompat
            .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete)
            .toBundle()

    val slideUpAnimation = ActivityOptionsCompat
            .makeCustomAnimation(context, R.anim.slide_up_partially, R.anim.slide_down_partially)
            .toBundle()

    private fun buildUri(latLng: LatLng, title: String): Uri {
        var urlEncodedTitle = ""
        if (!title.isNotEmpty()) {
            urlEncodedTitle = Uri.encode(title)
        }
        return Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=$urlEncodedTitle")
    }
}
