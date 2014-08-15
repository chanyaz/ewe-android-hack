package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.ViewUtils;

public class TextView extends android.widget.TextView {
	private static final int NORMAL = 0;
	private static final int BOLD = 1;
	private static final int ITALIC = 2;
	private static final int BLACK = 8;
	private static final int CONDENSED = 16;
	private static final int LIGHT = 32;
	private static final int MEDIUM = 64;
	private static final int THIN = 128;
	private static final int CONDENSED_BOLD = 256;
	private static final int CONDENSED_LIGHT = 512;
	private static final int EXPEDIASANS_LIGHT = 1024;

	private boolean mAllCaps = false;

	public TextView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public TextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public TextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextView, defStyle, 0);
		final int textStyle = a.getInt(R.styleable.TextView_textStyle, 0);
		mAllCaps = a.getBoolean(R.styleable.TextView_textAllCaps, false);
		a.recycle();

		if (textStyle > 0) {
			setTypefaceByStyle(this, textStyle);
		}

		if (mAllCaps) {
			ViewUtils.setAllCaps(this);
		}
	}

	public boolean isAllCaps() {
		return mAllCaps;
	}

	private void setTypefaceByStyle(TextView view, int style) {
		switch (style) {
		//		case BLACK | ITALIC: {
		//			FontCache.setTypeface(view, FontCache.ROBOTO_BLACK_ITALIC);
		//			break;
		//		}
		//		case BLACK: {
		//			FontCache.setTypeface(view, FontCache.ROBOTO_BLACK);
		//			break;
		//		}
		//		case BOLD | CONDENSED | ITALIC: {
		//			FontCache.setTypeface(view, FontCache.ROBOTO_BOLD_CONDENSED_ITALIC);
		//			break;
		//		}
		//		case BOLD | CONDENSED: {
		//			FontCache.setTypeface(view, FontCache.ROBOTO_BOLD_CONDENSED);
		//			break;
		//		}
		case BOLD: {
			FontCache.setTypeface(view, Font.ROBOTO_BOLD);
			break;
		}
		//		case CONDENSED | ITALIC: {
		//			FontCache.setTypeface(view, Font.ROBOTO_CONDENSED_ITALIC);
		//			break;
		//		}
		//		case CONDENSED: {
		//			FontCache.setTypeface(view, Font.ROBOTO_CONDENSED);
		//			break;
		//		}
		//		case LIGHT | ITALIC: {
		//			FontCache.setTypeface(view, Font.ROBOTO_LIGHT_ITALIC);
		//			break;
		//		}
		case LIGHT: {
			FontCache.setTypeface(view, Font.ROBOTO_LIGHT);
			break;
		}
		//		case THIN | ITALIC: {
		//			FontCache.setTypeface(view, Font.ROBOTO_THIN_ITALIC);
		//			break;
		//		}
		//		case THIN: {
		//			FontCache.setTypeface(view, Font.ROBOTO_THIN);
		//			break;
		//		}
		//		case MEDIUM | ITALIC: {
		//			FontCache.setTypeface(view, Font.ROBOTO_MEDIUM_ITALIC);
		//			break;
		//		}
		case MEDIUM: {
			FontCache.setTypeface(view, Font.ROBOTO_MEDIUM);
			break;
		}
		//		case ITALIC: {
		//			FontCache.setTypeface(view, Font.ROBOTO_ITALIC);
		//			break;
		//		}
		case NORMAL: {
			FontCache.setTypeface(view, Font.ROBOTO_REGULAR);
			break;
		}
		case CONDENSED: {
			FontCache.setTypeface(view, Font.ROBOTO_CONDENSED_REGULAR);
			break;
		}
		case CONDENSED_BOLD: {
			FontCache.setTypeface(view, Font.ROBOTO_CONDENSED_BOLD);
			break;
		}
		case CONDENSED_LIGHT: {
			FontCache.setTypeface(view, Font.ROBOTO_CONDENSED_LIGHT);
			break;
		}
		case EXPEDIASANS_LIGHT: {
			FontCache.setTypeface(view, Font.EXPEDIASANS_LIGHT);
			break;
		}
		}
	}
}
