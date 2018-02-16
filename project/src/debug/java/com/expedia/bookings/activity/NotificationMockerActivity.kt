package com.expedia.bookings.activity

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import kotlinx.android.synthetic.main.activity_notification_mocker.small_body_edit_text
import kotlinx.android.synthetic.main.activity_notification_mocker.shenanigans_text_view
import kotlinx.android.synthetic.main.activity_notification_mocker.title_edit_text
import kotlinx.android.synthetic.main.activity_notification_mocker.expanded_body_edit_text

class NotificationMockerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_mocker)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        small_body_edit_text.hint = "Required"
        setupListener()
    }

    private fun setupListener() = shenanigans_text_view.setOnClickListener {
        if (!small_body_edit_text.text.isNullOrBlank()) {
            @Suppress("DEPRECATION")
            val builder = NotificationCompat.Builder(this)
                    .setContentText(small_body_edit_text.text)
                    .setDefaults(Notification.DEFAULT_ALL)
            val notificationIconResourceId = ProductFlavorFeatureConfiguration.getInstance()
                    .notificationIconResourceId
            builder.setSmallIcon(notificationIconResourceId)
            if (!title_edit_text.text.isNullOrBlank()) {
                builder.setTicker(title_edit_text.text)
                builder.setContentTitle(title_edit_text.text)
            }
            if (!expanded_body_edit_text.text.isNullOrBlank()) {
                builder.setStyle(NotificationCompat.BigTextStyle()
                        .bigText(expanded_body_edit_text.text))
            }
            val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.notify(1337, builder.build())
        } else {
            Toast.makeText(baseContext, "small body plz", Toast.LENGTH_SHORT).show()
        }
    }
}
