package Utils

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.LegalClickableSpan
import com.squareup.phrase.Phrase

object StrUtils {

    fun generateAgreeToTermsLink(context: Context, termsURL: String): SpannableStringBuilder {
        val termsTextSpan = SpannableStringBuilder()
        termsTextSpan.append(HtmlCompat.fromHtml(Phrase.from(context.resources, R.string.agree_to_terms_text_TEMPLATE)
                .put("terms_url", termsURL)
                .format().toString()))
        val spans = termsTextSpan.getSpans(0, termsTextSpan.length, URLSpan::class.java)

        return formatAgreeToTermsLinkSpan(termsTextSpan, spans, context)
    }

    private fun formatAgreeToTermsLinkSpan(termsTextSpan: SpannableStringBuilder, spans: Array<URLSpan>, context: Context): SpannableStringBuilder {
        for (span in spans) {
            val start = termsTextSpan.getSpanStart(span)
            val end = termsTextSpan.getSpanEnd(span)

            termsTextSpan.removeSpan(span)
            termsTextSpan.setSpan(LegalClickableSpan(span.url, termsTextSpan.subSequence(start, end).toString(), true), start, end, 0)
            termsTextSpan.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
            termsTextSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray600)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return termsTextSpan
    }
}
