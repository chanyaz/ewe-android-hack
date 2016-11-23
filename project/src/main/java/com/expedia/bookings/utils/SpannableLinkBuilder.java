package com.expedia.bookings.utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.expedia.bookings.text.HtmlCompat;

public class SpannableLinkBuilder {
	protected boolean bolded;
	protected boolean bubbleUpClicks;
	protected Integer color;
	protected String content;
	protected boolean italicized;
	protected boolean underlined;

	public SpannableStringBuilder build() {
		Spanned spannedContent = HtmlCompat.fromHtml(content);
		SpannableStringBuilder builder = new SpannableStringBuilder(spannedContent);

		URLSpan[] urlSpans = builder.getSpans(0, spannedContent.length(), URLSpan.class);
		for (final URLSpan urlSpan : urlSpans) {
			int start = builder.getSpanStart(urlSpan);
			int end = builder.getSpanEnd(urlSpan);

			builder.removeSpan(urlSpan);
			LegalClickableSpan span = new LegalClickableSpan(urlSpan.getURL(),
				builder.subSequence(start, end).toString(), underlined) {
					@Override
					public void onClick(View widget) {
						super.onClick(widget);
						if (bubbleUpClicks) {
							widget.performClick();
						}
					}
			};
			builder.setSpan(span, start, end, 0);

			if (bolded && !italicized) {
				builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
			}
			if (bolded && italicized) {
				builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, 0);
			}
			if (color != null) {
				builder.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (italicized && !bolded) {
				builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 0);
			}
			if (underlined) {
				builder.setSpan(new UnderlineSpan(), start, end, 0);
			}
		}

		return builder;
	}

	public SpannableLinkBuilder bolded() {
		bolded = true;
		return this;
	}

	public SpannableLinkBuilder bubbleUpClicks() {
		this.bubbleUpClicks = true;
		return this;
	}

	public SpannableLinkBuilder withColor(int color) {
		this.color = color;
		return this;
	}

	public SpannableLinkBuilder withContent(String content) {
		this.content = content;
		return this;
	}

	public SpannableLinkBuilder italicized() {
		italicized = true;
		return this;
	}

	public SpannableLinkBuilder underlined() {
		underlined = true;
		return this;
	}
}
