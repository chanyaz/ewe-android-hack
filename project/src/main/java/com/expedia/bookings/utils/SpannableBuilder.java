package com.expedia.bookings.utils;

import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;

// Much more usable SpannableStringBuilder
public class SpannableBuilder {
	private final SpannableStringBuilder mBuilder;

	public SpannableBuilder() {
		mBuilder = new SpannableStringBuilder();
	}

	public void append(CharSequence seq) {
		mBuilder.append(seq);
	}

	// Look MA no Math! Just say what spans for the seq and we take care of the rest
	public void append(CharSequence seq, CharacterStyle... whats) {
		append(seq, 0, whats);
	}

	public void append(CharSequence seq, int flags, CharacterStyle... whats) {
		int start = mBuilder.length();
		int end = start + seq.length();

		mBuilder.append(seq);

		for (Object what : whats) {
			mBuilder.setSpan(what, start, end, flags);
		}
	}

	public SpannableStringBuilder build() {
		return mBuilder;
	}
}
