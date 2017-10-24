package com.expedia.bookings.lob.lx.ui.presenter

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.bookings.widget.ContactDetailsCompletenessStatusImageView

class CrowdFunBarWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val detailsText: TextView by bindView(R.id.enter_details_text)
    val fundSetting: ContactDetailsCompletenessStatusImageView by bindView(R.id.funds_setting)
    val title: EditText by bindView(R.id.activity_name)

    override fun onFinishInflate() {
        super.onFinishInflate()
        setOnClickListener {
            AlertDialog.Builder(context)
                    .setView(R.layout.activity_crowd_fund)
                    .setPositiveButton("Done", { _, _ ->
                        fundSetting.status = ContactDetailsCompletenessStatus.COMPLETE
                        detailsText.text = "Details completed"
                    })
                    .show()
        }
    }
}