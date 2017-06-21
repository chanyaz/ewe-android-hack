package com.expedia.bookings.luggagetags

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
class AddLuggageTag : AppCompatActivity() {

    private var outerContainer: LinearLayout? = null
    private var publicPrivateSwitch: Switch? = null

    private var tagIdTextView: TextView? = null
    private var tagIdEditText: EditText? = null

    private var nameTextView: TextView? = null
    private var nameEditText: EditText? = null

    private var addressTextView: TextView? = null
    private var addressEditText: EditText? = null

    private var zipCodeTextView: TextView? = null
    private var zipCodeEditText: EditText? = null

    private var phoneNumberTextView: TextView? = null
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

        tagIdTextView = findViewById(R.id.tag_id_text_view) as? TextView
        tagIdEditText = findViewById(R.id.tag_id) as? EditText

        nameTextView = findViewById(R.id.name_text_view) as? TextView
        nameEditText = findViewById(R.id.name_edit_text) as? EditText

        addressTextView = findViewById(R.id.address_text_view) as? TextView
        addressEditText = findViewById(R.id.address_edit_text) as? EditText

        zipCodeTextView = findViewById(R.id.zip_code_text_view_luggage_tag) as? TextView
        zipCodeEditText = findViewById(R.id.zip_code_edit_text) as? EditText

        phoneNumberTextView = findViewById(R.id.phone_number_text_view_luggage_tag) as? TextView
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text) as? EditText
        addTagButton = findViewById(R.id.add_tag) as? Button

        guid = Db.getAbacusGuid()

        addTagButton?.setOnClickListener {
            if (!guid.isNullOrEmpty()) {
                val tag = createTag()
                //TODO: add tag to DB for both 'Tag' and 'User'
                showAlert()
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
        return ExpediaLuggageTags(tagIdEditText?.text.toString(), guid, publicOrPrivate, nameEditText?.text.toString(), addressEditText?.text.toString(), phoneNumberEditText?.text.toString())
    }

    fun showAlert() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Your tag has been added")
                .setMessage("We've added the tag to your account. You will be able to manage settings from your account from now on.")
                .setPositiveButton("Done", {dialog, i -> finish() })
                .show()
    }
}
