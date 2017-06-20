package com.expedia.bookings.luggagetags

import android.util.Log
import com.google.firebase.database.*

class LuggageTagsNetwork {

    val dataBase = FirebaseDatabase.getInstance()
    var tagsReference: DatabaseReference? = null
    var usersReference: DatabaseReference? = null

    companion object {
        lateinit var instance: LuggageTagsNetwork
    }

    init {
        instance = this
        //dataBase.setPersistenceEnabled(true)
        tagsReference = dataBase.getReference("tags")
        usersReference = dataBase.getReference("users")
    }

    fun addTag(tagId: String, luggageTag: ExpediaLuggageTags) {
        tagsReference?.child(tagId)?.setValue(luggageTag)
    }

    fun addUserTag(userGuid: String, tagId: String, luggageTag: ExpediaLuggageTags) {
        usersReference?.child(userGuid)?.child(tagId)?.setValue(luggageTag)
    }

    //TODO
    fun updateTag(tagId: String, luggageTag: ExpediaLuggageTags) {

    }

    //TODO
    fun removeTag(tagId: String) {
        /*val query: Query? = tagsReference?.orderByChild("tagID")?.equalTo(tagId)
        query?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                val snapshotIterator: Iterable<DataSnapshot>? = p0?.children
                val iterator: Iterator<DataSnapshot>? = snapshotIterator?.iterator()
                while (iterator?.hasNext()!!) {
                    val snapshot: DataSnapshot = iterator.next()
                    val luggageTag: ExpediaLuggageTags = snapshot.getValue(ExpediaLuggageTags::class.java)
                    if (luggageTag.tagID == tagId) {
                        val snapshotKey = snapshot.key
                        tagsReference?.child(snapshotKey)?.removeValue()
                    } else {
                        Log.d("SRINI: ", "nothing to delete")
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
            }
        })*/
        if (tagsReference?.child(tagId) != null) {
            Log.d("SRINI: ", "found")
        } else {
            Log.d("SRINI: ", "not found")
        }
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