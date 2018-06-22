package com.expedia.bookings.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.TextView

class ScrollableContentDialogFragment : DialogFragment() {
    companion object {
        val TITLE_KEY = "title"
        val CONTENT_KEY = "content"
        val SECONDARY_TITLE = "SECONDARY_TITLE"
        val SECONDARY_CONTENT = "SECONDARY_CONTENT"

        @JvmStatic
        fun newInstance(title: String, content: String, secondaryTitle: String = "", secondaryContent: String = ""): ScrollableContentDialogFragment {
            val fragment = ScrollableContentDialogFragment()
            val arguments = Bundle()
            arguments.putString(TITLE_KEY, title)
            arguments.putString(CONTENT_KEY, content)
            if (secondaryTitle.isNotEmpty() && secondaryContent.isNotEmpty()) {
                arguments.putString(SECONDARY_TITLE, secondaryTitle)
                arguments.putString(SECONDARY_CONTENT, secondaryContent)
            }
            fragment.arguments = arguments
            return fragment
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view = activity!!.layoutInflater.inflate(R.layout.fragment_dialog_scrollable_content, null)
        val contentText = view.findViewById<TextView>(R.id.fragment_dialog_scrollable_text_content)
        alertDialogBuilder.setTitle(arguments?.getString(TITLE_KEY) ?: "")
        alertDialogBuilder.setView(view)
        val content = arguments?.getString(CONTENT_KEY) ?: ""
        contentText.text = if (getSpannableString(content) == null) content else removeUnderline(getSpannableString(content), contentText)
        val secondaryTitleText = view.findViewById<TextView>(R.id.fragment_dialog_second_heading)
        val secondaryContentText = view.findViewById<TextView>(R.id.fragment_dialog_scrollable_second_text_content)
        setSecondaryContent(secondaryTitleText, secondaryContentText)
        alertDialogBuilder.setNegativeButton(resources.getString(R.string.done), { dialog, _ ->
            dialog.dismiss()
        })

        return alertDialogBuilder.create()
    }

    private fun setSecondaryContent(secondaryTitleText: TextView, secondaryContentText: TextView) {
        val secondaryTitle = arguments?.getString(SECONDARY_TITLE, "") ?: ""
        val secondaryContent = arguments?.getString(SECONDARY_CONTENT, "") ?: ""
        if (Strings.isNotEmpty(secondaryTitle) && Strings.isNotEmpty(secondaryContent)) {
            secondaryTitleText.visibility = View.VISIBLE
            secondaryContentText.visibility = View.VISIBLE
            secondaryTitleText.text = secondaryTitle
            secondaryContentText.text = if (getSpannableString(secondaryContent) == null) secondaryContent else removeUnderline(getSpannableString(secondaryContent), secondaryContentText)
        }
    }

    private fun removeUnderline(spannableString: SpannableString?, textview: TextView): SpannableString? {
        val spans = spannableString?.getSpans(0, spannableString.length, URLSpan::class.java)
        if (spans != null && spans.isNotEmpty()) {
            textview.movementMethod = LinkMovementMethod.getInstance()
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
