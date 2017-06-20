package com.expedia.bookings.luggagetags

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch

import com.expedia.bookings.R
import com.expedia.bookings.data.Db

class AddLuggageTag : AppCompatActivity() {

    private var outerContainer: LinearLayout? = null
    private var publicPrivateSwitch: Switch? = null
    private var tagIdEditText: EditText? = null
    private var nameEditText: EditText? = null
    private var addressEditText: EditText? = null
    private var zipCodeEditText: EditText? = null
    private var phoneNumberEditText: EditText? = null
    private var addTagButton: Button? = null

    private var guid: String = ""
    var publicOrPrivate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_luggage_tag)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        outerContainer = findViewById(R.id.outer_container_add_luggage_tag) as? LinearLayout
        publicPrivateSwitch = findViewById(R.id.public_private_switch) as? Switch
        tagIdEditText = findViewById(R.id.tag_id) as? EditText
        nameEditText = findViewById(R.id.name_edit_text) as? EditText
        addressEditText = findViewById(R.id.address_edit_text) as? EditText
        zipCodeEditText = findViewById(R.id.zip_code_edit_text) as? EditText
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text) as? EditText
        addTagButton = findViewById(R.id.add_tag) as? Button

        guid = Db.getAbacusGuid()

        addTagButton?.setOnClickListener {
            //TODO: add tag to DB for both 'Tag' and 'User'
            if (!guid.isNullOrEmpty()) {
                val tag = createTag()

            }
        }

        if (publicPrivateSwitch != null) {
            publicPrivateSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    publicOrPrivate = true
                } else {
                    publicOrPrivate = false
                }
            }
        }
    }

    fun createTag(): ExpediaLuggageTags {
        return ExpediaLuggageTags(tagIdEditText?.text.toString(), true, guid, publicOrPrivate, nameEditText?.text.toString(), addressEditText?.text.toString(), phoneNumberEditText?.text.toString())
    }
}
//startActivity(Intent(context, AddLuggageTag::class.java))
