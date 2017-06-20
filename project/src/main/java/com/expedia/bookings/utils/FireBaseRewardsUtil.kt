package com.expedia.bookings.utils

import android.content.Context
import android.content.Intent
import android.text.Html
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener


class FireBaseRewardsUtil {
    companion object {
        val database = FirebaseDatabase.getInstance().reference
        lateinit var userRefernce: DatabaseReference
        var numberofRefers = 0L

        fun saveUserAndReferIds(userName: String) {
            database.child("users").child(userName).setValue(0)
            userRefernce = database.child("users").child(userName)
            userRefernce.addValueEventListener(responseListener)
        }

        val responseListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value as? Long ?: 0
                if (dataSnapshot.exists()) {
                    numberofRefers = value
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }


        fun getNumberOfRefers(): Long {
            return numberofRefers
        }

        fun onReferClicked() {
            userRefernce.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    if (mutableData.value == null) {
                        mutableData.value = 1
                    } else {
                        mutableData.value = mutableData.value as Long + 1
                    }
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                        dataSnapshot: DataSnapshot?) {
                }
            })
        }

        fun shareRewards(context: Context, userId: String) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/html"
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    Html.fromHtml("Book some trips with Expedia and get $25 off if you use below link https://play.google.com/store/apps/details?id=com.expedia.bookings&hl=en&referrer=$userId"))
            context.startActivity(Intent.createChooser(shareIntent, "Share using"))
        }


    }



}