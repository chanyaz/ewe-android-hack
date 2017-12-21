package com.expedia.account.util;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ResultReceiver;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.expedia.account.input.rules.ExpediaPasswordInputRule;
import com.squareup.phrase.Phrase;

public class Utils {

	public static boolean isAtLeastBarelyPassableEmailAddress(String email) {
		return email != null && email.matches(".+?@.+?\\..+");
	}

	public static boolean passwordIsValidForAccountCreation(String password) {
		return password.length() >= ExpediaPasswordInputRule.MINIMUM_PASSWORD_LENGTH && password.length() <= ExpediaPasswordInputRule.MAXIMUM_PASSWORD_LENGTH;
	}


	public static boolean isOnline(Context context) {
		boolean isOnline = true;
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			isOnline = (ni != null && ni.isAvailable() && ni.isConnected());
		}
		catch (Exception ignore) {
			//We tried, but it's not worth freaking out about.
		}
		return isOnline;
	}

	// Generate the best initials, given prioritized options of name sources
	public static String generateInitials(String firstName, String lastName, String displayName, String email) {
		String initials;

		// First Name, Last Name
		initials = generateInitials(firstName, lastName);
		if (initials != null) {
			return initials;
		}

		// Split up Display Name
		if (displayName != null) {
			initials = generateInitials(displayName.split("\\s+"));
			if (initials != null) {
				return initials;
			}
		}

		// First letter of Email Address
		return generateInitials(email);
	}

	// Returns the first letter of the first string and the first letter of the last string passed
	// (Assuming they both start with ascii characters)
	private static String generateInitials(String... words) {
		if (words == null || words.length == 0) {
			return null;
		}

		String initials = "";
		if (startsWithAcceptableCharacter(words[0])) {
			initials += words[0].substring(0, 1);
		}

		if (words.length > 1 && startsWithAcceptableCharacter(words[words.length - 1])) {
			initials += words[words.length - 1].substring(0, 1);
		}

		if (initials.length() == 0) {
			return null;
		}

		return initials.toUpperCase(Locale.getDefault());
	}

	private static boolean startsWithAcceptableCharacter(String s) {
		if (s == null || s.length() < 1) {
			return false;
		}
		//TODO: Analyze whether or not we even need to check the acceptability of these characters in the avatarview.

		return !InvalidCharacterTextWatcher.INVALID_CHARACTER_PATTERN.matcher(s.substring(0, 1)).find();
	}

	public static void brandHint(TextView v, String brand) {
		v.setHint(Phrase
			.from(v.getHint())
			.putOptional("brand", brand)
			.format());
	}

	public static void brandHint(TextInputLayout v, String brand) {
		v.setHint(Phrase
			.from(v.getHint())
			.putOptional("brand", brand)
			.format());
	}

	public static void brandText(TextView v, String brand) {
		v.setText(Phrase
			.from(v.getText())
			.putOptional("brand", brand)
			.format());
	}

	public static Phrase obtainBrandedPhrase(Context context, int resId, String brand) {
		return Phrase.from(context, resId)
			.putOptional("brand", brand);
	}

	public static CharSequence obtainPasswordErrorMessage(Context context, int resId) {
		return Phrase.from(context, resId)
			.putOptional("minlen", ExpediaPasswordInputRule.MINIMUM_PASSWORD_LENGTH)
			.putOptional("maxlen", ExpediaPasswordInputRule.MAXIMUM_PASSWORD_LENGTH)
			.format();
	}

	public static void showKeyboard(View view, ResultReceiver resultReceiver) {
		Context context = view.getContext();
		Configuration config = context.getResources().getConfiguration();
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			// Show soft keyboard if physical keyboard is not open
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (resultReceiver != null) {
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, resultReceiver);
			}
			else {
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}
	}

	public static void hideKeyboard(View view) {
		Context context = view.getContext();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static int getStatusBarHeight(Context ctx) {
		int result = 0;
		int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = ctx.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}
