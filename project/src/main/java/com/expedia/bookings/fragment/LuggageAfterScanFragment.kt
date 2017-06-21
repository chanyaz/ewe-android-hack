package com.expedia.bookings.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.luggagetags.AddLuggageTag
import com.expedia.bookings.luggagetags.ExpediaLuggageTags
import com.expedia.bookings.luggagetags.LuggageTagsNetwork
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mobiata.android.util.Ui


class LuggageAfterScanFragment : Fragment() {
    private var tagFoundPublicContainer: ScrollView? = null
    private var tagFoundPrivateContainer: ScrollView? = null
    private var tagNotFoundContainer: ScrollView? = null
    private var tagId: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_luggage_after_scan, container, false)
        tagId = arguments.getString("TAG_ID")

        tagFoundPublicContainer = Ui.findView(view, R.id.tag_found_public_container)
        tagFoundPrivateContainer = Ui.findView(view, R.id.tag_found_private_container)
        tagNotFoundContainer = Ui.findView(view, R.id.tag_not_found_container)

        LuggageTagsNetwork().tagsReference?.child(tagId)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                Log.d("SRINI: TAG_ID", tagId)
                val luggageTag: ExpediaLuggageTags? = p0?.getValue(ExpediaLuggageTags::class.java)
                if (luggageTag != null) {
                    Log.d("SRINI: ", "Luggage tag not null " + luggageTag.tagID + " " + luggageTag.name + " " + luggageTag.public)
                    if (luggageTag.public) {
                        tagFoundPublicContainer?.visibility = View.VISIBLE
                        val nameText: TextView = tagFoundPublicContainer?.findViewById(R.id.tag_found_public_name) as TextView
                        nameText.text = luggageTag.name
                        val addressText: TextView = tagFoundPublicContainer?.findViewById(R.id.tag_found_public_address) as TextView
                        addressText.text = luggageTag.address
                        tagFoundPublicContainer?.findViewById(R.id.tag_found_public_call)?.setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:" + luggageTag.phoneNumber)
                            startActivity(intent)
                        }
                    } else {
                        tagFoundPrivateContainer?.visibility = View.VISIBLE
                    }
                } else {
                    Log.d("SRINI: ", "Luggage tag null")
                    tagNotFoundContainer?.visibility = View.VISIBLE
                    tagNotFoundContainer?.findViewById(R.id.tag_not_found_register)?.setOnClickListener {
                        val intent = Intent(activity, AddLuggageTag::class.java)
                        intent.putExtra("TAG_ID", tagId)
                        startActivity(intent)
                    }
                }
            }

        })

        return view
    }
}

