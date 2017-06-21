package com.expedia.bookings.luggagetags
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.expedia.bookings.R
import com.mobiata.android.util.Ui

class LuggageTagInfoFragment : Fragment() {

    private var tagIdTextView: TextView? = null
    private var nameTextView: TextView? = null
    private var addressTextView: TextView? = null
    private var editTagInfoButton: Button? = null

    //TODO: Replace this VVV
    private var luggageTag: ExpediaLuggageTags? = ExpediaLuggageTags("1234", "mytuid", true, "Eric", "1729 hay", "4084084084")


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_luggage_tag_info, container, false)
        tagIdTextView = Ui.findView(view, R.id.tag_id_luggage_tag_info)
        nameTextView = Ui.findView(view, R.id.name_luggage_tag_info)
        addressTextView = Ui.findView(view, R.id.address_luggage_tag_info)
        editTagInfoButton = Ui.findView(view, R.id.edit_account_info)

        tagIdTextView?.setText(luggageTag?.tagID)
        nameTextView?.setText(luggageTag?.name)
        addressTextView?.setText(luggageTag?.address)

        editTagInfoButton?.setOnClickListener {
            val intent = Intent(context, AddLuggageTag::class.java)
            intent.putExtra("luggageTag", luggageTag)
            startActivity(intent)
        }

        return view
    }
}