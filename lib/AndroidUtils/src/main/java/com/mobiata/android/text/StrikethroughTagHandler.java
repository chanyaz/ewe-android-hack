package com.mobiata.android.text;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;

/**
 * Allows parsing of &lt;strike&gt; (or &lt;s&gt;) tags using Html.fromHtml().
 */
public class StrikethroughTagHandler implements TagHandler {

	@Override
	public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
		if (tag.equalsIgnoreCase("strike") || tag.equalsIgnoreCase("s")) {
			int len = output.length();
			if (opening) {
				output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
			}
			else {
				// Get the last span
				Object[] objs = output.getSpans(0, len, StrikethroughSpan.class);

				if (objs.length == 0) {
					return;
				}

				Object obj = objs[objs.length - 1];
				int where = output.getSpanStart(obj);
				output.removeSpan(obj);

				if (where != len) {
					output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
	}
}
