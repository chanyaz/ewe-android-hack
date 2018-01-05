package com.mobiata.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;

public class SocialUtils {
	public static boolean canHandleIntentOfTypeXandUriY(Context context, String intentType, String url) {
		final Intent intent = new Intent(intentType);
		intent.setData(Uri.parse(url));

		PackageManager pm = (PackageManager) context.getPackageManager();
		try {
			if (pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
				return false;
			}
		}
		catch (Exception e) {
			Log.w("Could not detect if we have an app that handles this request", e);
		}

		return true;
	}

	public static void email(Context context, int subjectResId, int bodyResId) {
		final String subject = context.getString(subjectResId);
		final String body = context.getString(bodyResId);

		email(context, null, subject, body);
	}

	public static void email(Context context, int toResId, int subjectResId, int bodyResId) {
		final String to = context.getString(toResId);
		final String subject = context.getString(subjectResId);
		final String body = context.getString(bodyResId);

		email(context, to, subject, body);
	}

	public static void email(Context context, String subject, CharSequence body) {
		email(context, "", subject, body);
	}

	public static Intent getEmailIntent(Context context, String subject, CharSequence body) {
		return getEmailIntent(context, "", subject, body);
	}

	public static void email(Context context, String to, String subject, CharSequence body) {
		context.startActivity(getEmailIntent(context, to, subject, body));
	}

	public static Intent getEmailIntent(Context context, String to, String subject, CharSequence body) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		String[] toArr = new String[] { to };
		intent.putExtra(Intent.EXTRA_EMAIL, toArr);

		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, body);

		// Check that there is a program that can handle this; if not, open up
		// the share to any application, regardless of whether it specifically handles
		// emails
		PackageManager pm = (PackageManager) context.getPackageManager();
		try {
			if (pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
				intent.setType("text/plain");
			}
		}
		catch (Exception e) {
			Log.w("Could not detect if we have an app that handles emails", e);

			// Assume we're okay if we catch an exception (PackageManger is tricky)
		}

		return intent;
	}

	public static void openSite(Context context, String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}

	public static Intent getShareIntent(String subject, String text) {
		return getShareIntent(subject, text, true);
	}

	public static Intent getShareIntent(String subject, String text, boolean withChooser) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		return withChooser ? Intent.createChooser(intent, null) : intent;
	}

	public static void share(Context context, String subject, String text) {
		final Intent mailer = getShareIntent(subject, text);
		context.startActivity(mailer);
	}

	public static Intent getCallIntent(Context context, String phoneNumber) {
		Intent intent = new Intent(Intent.ACTION_VIEW);

		Uri.Builder builder = new Uri.Builder();
		builder.scheme("tel");
		phoneNumber = PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber);
		phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
		builder.opaquePart(phoneNumber);

		intent.setData(builder.build());
		return intent;
	}

	public static void call(Context context, String phoneNumber) {
		// Remove all whitespace because some dialers phreak out
		// and format them poorly
		phoneNumber = phoneNumber.replaceAll("\\s","");
		final Intent call = getCallIntent(context, phoneNumber);
		context.startActivity(call);
	}
}
