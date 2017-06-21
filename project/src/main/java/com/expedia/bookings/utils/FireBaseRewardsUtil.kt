package com.expedia.bookings.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.expedia.bookings.R
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.mobiata.android.util.SettingUtils


class FireBaseRewardsUtil {
    companion object {
        val database = FirebaseDatabase.getInstance().reference
        lateinit var userRefernce: DatabaseReference
        var numberofRefers = 0L
        val LAST_REFER_VALUE = "LAST_REFER_VALUE"

        fun saveUserAndReferIds(context: Context, userName: String) {
            userRefernce = database.child("users").child(userName)
            userRefernce.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.value != null) {
                        val value = dataSnapshot.value as Long
                        if (dataSnapshot.exists()) {
                            numberofRefers = value
                        }
                        if (SettingUtils.get(context, LAST_REFER_VALUE, 0L) != value) {
                            issueNotification(context)
                        }
                    } else {
                        userRefernce.setValue(0)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        private fun issueNotification(context: Context) {
            val mBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_expedia)
                    .setContentTitle(context.getString(R.string.congratulations))
                    .setContentText(context.getString(R.string.referral_accepted))

            val resultIntent = Intent(context, NewPhoneLaunchActivity::class.java)
            val resultPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            mBuilder.setContentIntent(resultPendingIntent);
            val mNotifyMgr = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.notify(1, mBuilder.build())
        }

        fun getNumberOfRefers(): Long {
            return numberofRefers
        }

        fun onReferClicked(context: Context, userName: String) {
            userRefernce = database.child("users").child(userName)
            userRefernce.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    if (mutableData.value == null) {
                        mutableData.value = 1
                    } else {
                        mutableData.value = mutableData.value as Long + 1
                    }
                    SettingUtils.save(context, LAST_REFER_VALUE, mutableData.value as Long)
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                        dataSnapshot: DataSnapshot?) {
                }
            })
        }

        fun shareRewards(context: Context, userId: String) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Ready for a getaway? Use my link and get 20% off your first hotel booking on the Expedia App https://play.google.com/store/apps/details?id=com.expedia.bookings&hl=en&referrer=$userId")
            context.startActivity(Intent.createChooser(shareIntent, "Share using"))
        }


    }


}