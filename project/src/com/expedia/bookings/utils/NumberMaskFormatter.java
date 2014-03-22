package com.expedia.bookings.utils;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

/**
 * Applies formatting to a String or Editable, based on a provided format string {@see setMask(String)}.
 *
 * Created by dmelton on 3/19/14.
 */
public class NumberMaskFormatter {

	private static final String TAG = NumberMaskFormatter.class.getSimpleName();

	public static final String NORTH_AMERICAN_PHONE_NUMBER = "+1 NNN NNN NNNN;1 (NNN) NNN-NNNN;NNN-NNNN;(NNN) NNN-NNNN";

	// http://en.wikipedia.org/wiki/Bank_card_number
	public static final String CREDIT_CARD = "34NN NNNNNN NNNNN;37NN NNNNNN NNNNN;NNNN NNNN NNNN NNNN NNN";

	private String mMask;

	public NumberMaskFormatter() {
		mMask = "";
	}

	/**
	 * {@see setMask()}
	 *
	 * @param mask
	 */
	public NumberMaskFormatter(String mask) {
		mMask = mask;
	}

	/**
	 * Sets this formatter's multi-format string. It should be a semicolon separated list of formatting
	 * strings to format. "N" represents any single digit. [1-9()-] represents an exact digit.
	 * These rules will be followed sequentially until one is matched.
	 */
	public void setMask(String mask) {
		mMask = mask;
	}

	/**
	 * Returns this formatter's multi-format string. {@see setMask(String)}
	 * @return
	 */
	public String getMask() {
		return mMask;
	}

	/**
	 * Returns a string representation of the formatted and embellished input string.
	 * @param value
	 * @return
	 */
	public String applyTo(String value) {
		SpannableStringBuilder builder = new SpannableStringBuilder(value);
		applyToEditable(builder, mMask, true, false);
		return builder.toString();
	}

	/**
	 * Applies formatting to the passed Editable (via EmbellishedTextSpan spans).
	 * @param editable
	 */
	public void applyTo(Editable editable) {
		// TODO: odd things happen when we try to just apply to the editable itself.
		// Try to fix that. But for now, just make a new spannable string.
		SpannableStringBuilder builder = new SpannableStringBuilder(editable.toString());
		applyToEditable(builder, mMask, true, true);
		editable.replace(0, editable.length(), builder);
	}

	private static boolean isValidValueCharacter(char c) {
		return c == '+' || c == '#' || c == '*' || c >= '0' && c <= '9';
	}

	/**
	 * Embellishes a number (like a telephone number or credit card number) according to the
	 * rules defined in the passed in format string. See {@code NORTH_AMERICAN_PHONE_NUMBER};
	 *
	 * @param editable    The non-formatted number.
	 * @param format      The desired format.
	 * @param isPartialOk True if the number passed in "number" are just the first few
	 *                    digits of the formatted number. This might be the case on a
	 *                    dialpad where the number is being inputted right now
	 * @param useSpans    If true, embellishing text will be added as EmbellishedTextSpan spans.
	 *                    If false, the embellishing text will be inserted into the editable.
	 * @return
	 */
	private static void applyToEditable(Editable editable, String format, boolean isPartialOk, boolean useSpans) {
		if (editable.length() == 0) {
			return;
		}

		for (String fmt : format.split(";")) {
			editable.clearSpans();

			// fmt = a particular format to match
			// vi = index of pointer in value
			// fi = index of pointer in fmt
			// fd = character at fi
			// vd = character at vi

			int vi = 0;

			boolean isMatch = true;
			String embellishment = "";

			for (int fi = 0; fi < fmt.length() && isMatch; fi++) {
				char fd = fmt.charAt(fi);

				if (vi >= editable.length()) {
					isMatch = isPartialOk;
					break;
				}

				char vd = editable.charAt(vi);

				// Match any number
				// or Match exactly
				if (fd == 'N' || isValidValueCharacter(fd)) {
					if ((fd == 'N' && (vd < '0' || vd > '9'))
						|| (isValidValueCharacter(fd) && vd != fd)) {
						isMatch = false;
						continue;
					}

					if (useSpans) {
						if (!TextUtils.isEmpty(embellishment)) {
							EmbellishedTextSpan span = new EmbellishedTextSpan(embellishment, null);
							editable.setSpan(span, vi, vi + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						vi++;
					}
					else {
						editable.insert(vi, embellishment);
						vi += embellishment.length() + 1;
					}
					embellishment = "";
				}

				// Embellishment character; does not need to match input
				else {
					embellishment += fd;
				}
			}

			// Value has extra digits in it. Too long; didn't match.
			if (vi < editable.length()) {
				isMatch = false;
			}

			if (isMatch) {
				// SUCCESS!
				return;
			}
		}

		editable.clearSpans();
	}
}
