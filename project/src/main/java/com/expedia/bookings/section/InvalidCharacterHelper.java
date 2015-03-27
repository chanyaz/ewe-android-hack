package com.expedia.bookings.section;

import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;

import com.expedia.bookings.R;
import com.expedia.bookings.dialog.TextViewDialog;
import com.expedia.bookings.dialog.TextViewDialog.OnDismissListener;
import com.mobiata.android.Log;

public class InvalidCharacterHelper {

	//Matches only ascii characters (thus disallowing multibyte characters)
	private static final Pattern SUPPORTED_CHARACTER_PATTERN_ASCII = Pattern.compile("^(\\p{ASCII})*$");
	//Matches ascii characters that are only letters, latin accents, apostrophe, dash(es), periods, commas or spaces.
	private static final Pattern SUPPORTED_CHARACTER_PATTERN_NAMES = Pattern.compile("^[a-zA-ZÀ-ÿ',. -]*$");

	private static final String INVALID_CHARACTER_POPUP_TAG = "INVALID_CHARACTER_POPUP_TAG";

	//Used to only display one instance of the popup. This is useful because sometimes our TextWatchers
	//change too fast, and try to open the dialog twice in a row, causing a fragment.isAdded exception.
	private static final Semaphore sDialogSemaphore = new Semaphore(1);

	public interface InvalidCharacterListener {
		public void onInvalidCharacterEntered(CharSequence text, Mode mode);
	}

	public enum Mode {
		ASCII,
		NAME,
		EMAIL,
		ADDRESS
	}

	//Don't instantiate this class
	private InvalidCharacterHelper() {
	}

	/**
	 * Get the pattern used to determine if characters are valid
	 * @return
	 */
	public static Pattern getSupportedCharacterPattern(Mode mode) {
		switch (mode) {
		case NAME: {
			return SUPPORTED_CHARACTER_PATTERN_NAMES;
		}
		default: {
			return SUPPORTED_CHARACTER_PATTERN_ASCII;
		}
		}

	}

	/**
	 * This will generate a TextWatcher that removes invalid characters, after they have been entered.
	 * Additionally it supports chaining, so that if the chainListener argument is not null, its callback
	 * will be fired in the event of invalid characters.
	 * @param chainListener - listener to fire if invalid characters are present
	 * @return TextWatcher that removes invalid characters
	 */
	public static TextWatcher generateInvalidCharacterTextWatcher(final InvalidCharacterListener chainListener,
			final Mode mode) {
		return new AfterChangeTextWatcher() {
			@Override
			public synchronized void afterTextChanged(Editable s) {
				Pattern p = getSupportedCharacterPattern(mode);
				String str = s.toString();
				if (!p.matcher(str).matches()) {
					//This is a bit strange, but using the filter allows us to not duplicate code
					//and it gives us a bit of flexibility for how we block the questionable characters
					InputFilter[] origFilters = s.getFilters();
					CharSequence origStr = s.toString();
					s.setFilters(new InputFilter[] { generateValidCharacterInputFilter(mode) });
					s.clear();
					s.append(origStr);
					s.setFilters(origFilters);

					if (chainListener != null) {
						chainListener.onInvalidCharacterEntered(s.toString(), mode);
					}

				}
			}
		};
	}

	/**
	 * Generate a new InputFilter that only allows for valid characters
	 * @return
	 */
	public static InputFilter generateValidCharacterInputFilter(final Mode mode) {
		return new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {

				Pattern pattern = getSupportedCharacterPattern(mode);
				if (!pattern.matcher(source).matches()) {
					if (source instanceof SpannableStringBuilder) {
						SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
						for (int i = end - 1; i >= start; i--) {
							CharSequence currentChar = source.subSequence(i, i + 1);
							if (!pattern.matcher(currentChar).matches()) {
								sourceAsSpannableBuilder.delete(i, i + 1);
							}
						}
						return source;
					}
					else {
						StringBuilder filteredStringBuilder = new StringBuilder();
						for (int i = 0; i < end; i++) {
							CharSequence currentChar = source.subSequence(i, i + 1);
							if (pattern.matcher(currentChar).matches()) {
								filteredStringBuilder.append(currentChar);
							}
						}
						return filteredStringBuilder.toString();
					}
				}
				return source;
			}
		};
	}

	/**
	 * This will show the invalid characters dialog fragment if it is  not already showing.
	 * @param fm - The FragmentManager to attach the dialog to.
	 */
	public static void showInvalidCharacterPopup(FragmentManager fm, Mode mode) {
		boolean semGot = false;
		boolean releaseSet = false;
		try {
			if (sDialogSemaphore.tryAcquire()) {
				semGot = true;
				TextViewDialog mDialog = (TextViewDialog) fm.findFragmentByTag(INVALID_CHARACTER_POPUP_TAG);
				if (mDialog == null) {
					//Create the dialog
					mDialog = new TextViewDialog();
					mDialog.setCancelable(false);
					mDialog.setCanceledOnTouchOutside(false);
					mDialog.setMessage(R.string.please_use_the_roman_alphabet);
				}
				if (!mDialog.isShowing()) {
					//Show the dialog
					mDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismissed() {
							sDialogSemaphore.release();
						}

					});
					releaseSet = true;
					mDialog.show(fm, INVALID_CHARACTER_POPUP_TAG);
				}
			}
		}
		catch (Exception ex) {
			Log.d("Exception showingInvalidCharacterPopup", ex);
		}
		finally {
			if (semGot && !releaseSet) {
				sDialogSemaphore.release();
			}
		}
	}

}
