package com.expedia.account.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * A TextWatcher that removes invalid characters, after they have been entered.
 * Additionally it will notify the provided listener when it removes characters.
 */
public class InvalidCharacterTextWatcher implements TextWatcher {

	// Matches characters other than lowercase, uppercase and <space>
	public static final Pattern INVALID_CHARACTER_PATTERN = Pattern.compile("[^a-zA-ZÀ-ÿ',. -]");

	public interface InvalidCharacterListener {
		void onInvalidCharacterEntered(String text);
	}

	private InvalidCharacterListener listener = null;

	public InvalidCharacterTextWatcher(InvalidCharacterListener listener) {
		this.listener = listener;
	}

	@Override
	public synchronized void afterTextChanged(Editable editable) {
		Matcher matcher = INVALID_CHARACTER_PATTERN.matcher(editable.toString());
		if (matcher.find()) {
			String clean = matcher.replaceAll("");
			editable.replace(0, editable.length(), clean);

			if (listener != null) {
				listener.onInvalidCharacterEntered(editable.toString());
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		//do nothing
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		//do nothing
	}
}
