package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class TravelerIconUtils {

	/**
	 * Generate a circular icon containing the initials of the provided travelerDisplayName
	 *
	 * @param context
	 * @param travelerDisplayName - human readable full name e.g. "John Doe"
	 * @param backgroundColor     - The background color of the initials icon
	 * @return
	 */
	public static Bitmap generateCircularInitialIcon(Context context, String travelerDisplayName, int backgroundColor) {
		String name = getInitialsFromDisplayName(travelerDisplayName);

		float density = context.getResources().getDisplayMetrics().density;
		int size = (int) (62 * density);
		Bitmap iconBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(iconBmp);

		Paint iconBgPaint = new Paint();
		iconBgPaint.setStyle(Paint.Style.FILL);
		iconBgPaint.setAntiAlias(true);

		Paint bgPaintWhite = new Paint();
		bgPaintWhite.setStyle(Paint.Style.FILL);
		bgPaintWhite.setColor(0xffffffff);
		bgPaintWhite.setAntiAlias(true);

		Paint txtPaint = new Paint();
		txtPaint.setStyle(Paint.Style.FILL);
		txtPaint.setTextAlign(Paint.Align.CENTER);
		txtPaint.setAntiAlias(true);
		// Fetch appropriate background color to paint in the icon.
		iconBgPaint.setColor(backgroundColor);
		txtPaint.setColor(0xFFFFFFFF);
		txtPaint.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));
		txtPaint.setTextSize(32 * density);

		float textHeight = txtPaint.descent() - txtPaint.ascent();
		float textOffset = (textHeight / 2) - txtPaint.descent();

		int borderWidth = (int) (2.5 * density);
		canvas.drawCircle(size / 2, size / 2, size / 2, bgPaintWhite);
		canvas.drawCircle(size / 2, size / 2, size / 2 - borderWidth, iconBgPaint);
		canvas.drawText(name, size / 2, (size / 2) + (textOffset), txtPaint);
		return iconBmp;
	}

	/**
	 * @param displayName Full name of the traveler
	 * @return 2 character string, which are the 1st letter of firstname and lastname.
	 * In case where displayName has only one name, then just return 1 character.
	 */
	public static String getInitialsFromDisplayName(String displayName) {
		String[] nameParts = displayName.split(" ");
		if (nameParts.length == 1) {
			return nameParts[0].substring(0, 1).toUpperCase(Locale.getDefault());
		}
		else if (nameParts.length == 2) {
			return (nameParts[0].substring(0, 1) + nameParts[1].substring(0, 1)).toUpperCase(Locale.getDefault());
		}
		else if (nameParts.length == 3) {
			return (nameParts[0].substring(0, 1) + nameParts[2].substring(0, 1)).toUpperCase(Locale.getDefault());
		}

		return null;
	}
}
