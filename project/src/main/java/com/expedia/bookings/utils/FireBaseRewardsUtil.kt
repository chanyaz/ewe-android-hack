package com.expedia.bookings.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FireBaseRewardsUtil {
    companion object {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("userid")
        val myRef1 = database.getReference("refer")

        fun saveUserAndReferIds(userName: String) {
            myRef.setValue(userName)
            myRef1.addValueEventListener(responseListener)
        }

        val responseListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value  = dataSnapshot.value as? Long ?: 0
                if (!dataSnapshot.exists()) {
                    myRef1.setValue(0)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
    }


}