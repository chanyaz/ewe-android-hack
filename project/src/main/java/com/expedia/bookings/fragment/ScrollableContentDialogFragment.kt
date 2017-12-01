package com.expedia.bookings.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.widget.TextView
import android.text.TextPaint
import android.text.style.URLSpan

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
        val content = arguments.getString(CONTENT_KEY) ?: ""
        contentText.text = if (getSpannableString(content) == null) content else removeUnderline(getSpannableString(content))
        contentText.movementMethod = LinkMovementMethod.getInstance()
        alertDialogBuilder.setNegativeButton(context.resources.getString(R.string.done), { dialog, _ ->
            dialog.dismiss()
        })

        return alertDialogBuilder.create()
    }

    private fun removeUnderline(spannableString: SpannableString?): SpannableString? {
        val spans = spannableString?.getSpans(0, spannableString?.length, URLSpan::class.java)
        if (spans != null) {
            for (span in spans) {
                val start = spannableString.getSpanStart(span)
                val end = spannableString.getSpanEnd(span)
                spannableString.removeSpan(span)
                val noUnderLinespan = URLSpanNoUnderline(span.url)
                spannableString.setSpan(noUnderLinespan, start, end, 0)
            }
        }
        return spannableString
    }

    private inner class URLSpanNoUnderline(url: String) : URLSpan(url) {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }

    private fun getSpannableString(htmlContent: String): SpannableString? {
        try {
            return SpannableString(HtmlCompat.fromHtml(htmlContent))
        } catch (e: Exception) {
        }
        return null
    }

}