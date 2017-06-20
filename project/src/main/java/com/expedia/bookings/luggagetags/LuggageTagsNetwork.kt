package com.expedia.bookings.luggagetags

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LuggageTagsNetwork {
    val dataBase = FirebaseDatabase.getInstance()
    var tagsReference: DatabaseReference? = null
    var usersReference: DatabaseReference? = null
    var tags: List<ExpediaLuggageTags>? = null

    companion object {
        lateinit var instance: LuggageTagsNetwork
    }

    init {
        instance = this
        //dataBase.setPersistenceEnabled(true)
        tagsReference = dataBase.getReference("tags")
        usersReference = dataBase.getReference("users")
    }

    //TO ADD/UPDATE
    fun addTag(tagId: String, userGuid: String, luggageTag: ExpediaLuggageTags) {
        tagsReference?.child(tagId)?.setValue(luggageTag)
        usersReference?.child(userGuid)?.child(tagId)?.setValue(luggageTag)
    }

    fun removeTag(tagId: String, guId: String) {
//        val query: Query? = tagsReference?.orderByChild("tagID")?.equalTo(tagId)
//        query?.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(p0: DataSnapshot?) {
//                val snapshotIterator: Iterable<DataSnapshot>? = p0?.children
//                val iterator: Iterator<DataSnapshot>? = snapshotIterator?.iterator()
//                while (iterator?.hasNext()!!) {
//                    val snapshot: DataSnapshot = iterator.next()
//                    val luggageTag: ExpediaLuggageTags = snapshot.getValue(ExpediaLuggageTags::class.java)
//                    if (luggageTag.tagID == tagId) {
//                        val snapshotKey = snapshot.key
//                        tagsReference?.child(snapshotKey)?.removeValue()
//                    } else {
//                        Log.d("SRINI: ", "nothing to delete")
//                    }
//                }
//            }
//
//            override fun onCancelled(p0: DatabaseError?) {
//            }
//        })
        Log.d("SRINI: ", guId + " " + tagId)
        usersReference?.child(guId)?.child(tagId)?.removeValue()
        tagsReference?.child(tagId)?.removeValue()
    }

    fun retrieveTag(tagId: String): ExpediaLuggageTags {

    }

    fun pushExpediaLuggageTagToFirebase(luggageTag: ExpediaLuggageTags) {
        val luggageTagsReference = dataBase.getReference("ExpediaLuggageTags")
        luggageTagsReference.setValue(luggageTag)
    }

    fun pushExpediaLuggageTagScanToFirebase(luggageTagScan: ExpediaLuggageTagScan) {
        val luggageTagsReference = dataBase.getReference("ExpediaLuggageTagScan")
        luggageTagsReference.setValue(luggageTagScan)
    }
}