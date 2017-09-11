package com.expedia.bookings.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.expedia.bookings.R
import com.expedia.bookings.widget.TextView

class ScrollableContentDialogFragment : DialogFragment() {
    companion object {
        val TITLE_KEY = "title"
        val CONTENT_KEY = "content"

        @JvmStatic
        fun newInstance(title: String, content: String): ScrollableContentDialogFragment {
            val fragment = ScrollableContentDialogFragment()
            val arguments = Bundle()
            arguments.putString(TITLE_KEY, title)
            arguments.putString(CONTENT_KEY, content)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val layoutInflater = activity.layoutInflater
        val view = layoutInflater.inflate(R.layout.fragment_dialog_scrollable_content, null)
        val contentText = view.findViewById<TextView>(R.id.fragment_dialog_scrollable_text_content)

        alertDialogBuilder.setTitle(arguments.getString(TITLE_KEY) ?: "")
        alertDialogBuilder.setView(view)
        contentText.text = arguments.getString(CONTENT_KEY) ?: ""
        alertDialogBuilder.setNegativeButton(context.resources.getString(R.string.done), { dialog, _ ->
            dialog.dismiss()
        })

        return alertDialogBuilder.create()
    }
}