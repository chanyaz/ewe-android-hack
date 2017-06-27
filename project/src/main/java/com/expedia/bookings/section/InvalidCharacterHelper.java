package com.expedia.bookings.section;

import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.dialog.TextViewDialog;
import com.expedia.bookings.dialog.TextViewDialog.OnDismissListener;
import com.mobiata.android.Log;

public class InvalidCharacterHelper {

	private static final Pattern SUPPORTED_CHARACTER_PATTERN_ASCII = Pattern.compile("^(\\p{ASCII})*$");
	//Matches ascii characters that are only letters, latin accents, apostrophe, dash(es), periods, commas or spaces.
	private static final Pattern SUPPORTED_CHARACTER_PATTERN_NAMES = Pattern.compile("^[a-zA-ZÀ-ÿ',. -]*$");
	//Matches ascii characters that are only letters, numbers.
	private static final Pattern SUPPORTED_CHARACTER_PATTERN_ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]*$");

	private static final String INVALID_CHARACTER_POPUP_TAG = "INVALID_CHARACTER_POPUP_TAG";

	//Used to only display one instance of the popup. This is useful because sometimes our TextWatchers
	//change too fast, and try to open the dialog twice in a row, causing a fragment.isAdded exception.
	private static final Semaphore sDialogSemaphore = new Semaphore(1);

	public interface InvalidCharacterListener {
		void onInvalidCharacterEntered(CharSequence text, Mode mode);
	}

	public enum Mode {
		ASCII,
		NAME,
		EMAIL,
		ADDRESS,
		ALPHANUMERIC
	}

	//Don't instantiate this class
	private InvalidCharacterHelper() {
	}

	/**
	 * Get the pattern used to determine if characters are valid
	 *
	 * @return
	 */
	public static Pattern getSupportedCharacterPattern(Mode mode) {
		switch (mode) {
		case NAME: {
			return SUPPORTED_CHARACTER_PATTERN_NAMES;
		}
		case ALPHANUMERIC: {
			return SUPPORTED_CHARACTER_PATTERN_ALPHANUMERIC;
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
	 *
	 * @param chainListener - listener to fire if invalid characters are present
	 */
	public static void generateInvalidCharacterTextWatcher(final EditText field,
		final InvalidCharacterListener chainListener,
		final Mode mode) {
		field.addTextChangedListener(
			new AfterChangeTextWatcher() {
				@Override
				public synchronized void afterTextChanged(Editable s) {
					field.removeTextChangedListener(this);
					Pattern p = getSupportedCharacterPattern(mode);
					if (!p.matcher(s).matches()) {
						for (int i = s.length() - 1; i >= 0; i--) {
							CharSequence currentChar = s.subSequence(i, i + 1);
							if (!p.matcher(currentChar).matches()) {
								s.delete(i, i + 1);
							}
						}
						field.setText(s);
						field.setSelection(s.length());
						if (chainListener != null) {
							chainListener.onInvalidCharacterEntered(s.toString(), mode);
						}
					}
					field.addTextChangedListener(this);
				}
			});
	}

	/**
	 * This will show the invalid characters dialog fragment if it is  not already showing.
	 *
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
