package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;

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

	// fields
	private boolean mShouldSetUpStroke = false;
	private int mStrokeColor;
	private int mStrokeWidth;
	private TextPaint mStrokePaint;

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
		final int color = a.getColor(R.styleable.TextView_drawableTintColor, 0);
		a.recycle();

		if (textStyle > 0) {
			setTypefaceByStyle(this, textStyle);
		}
		if (color != 0 && getCompoundDrawables()[0] != null) {
			setTintedDrawable(getCompoundDrawables()[0], color);
		}
	}

	public void setTypefaceByStyle(TextView view, int style) {
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

	public void setTintedDrawable(Drawable drawable, @ColorInt int color) {
		drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
		setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
	}

	// Stroke

	public void setStrokeColor(int color) {
		mStrokeColor = color;
		mShouldSetUpStroke = true;
	}

	public void setStrokeWidth(int width) {
		mStrokeWidth = width;
		mShouldSetUpStroke = true;
	}

	public boolean getShouldStroke() {
		return mShouldSetUpStroke;
	}

	public int getStrokeColor() {
		return mStrokeColor;
	}

	public int getStrokeWidth() {
		return mStrokeWidth;
	}


	@Override
	protected void onDraw(Canvas canvas) {
		// Only if we've set a stroke value should we do this.
		if (mShouldSetUpStroke) {
			if (mStrokePaint == null) {
				mStrokePaint = new TextPaint();
			}

			if (mStrokePaint.getTextSize() != getPaint().getTextSize()) {
				mStrokePaint.setTextSize(getPaint().getTextSize());
			}

			// Get the information we have
			mStrokePaint.setTextSize(getTextSize());
			mStrokePaint.setTypeface(getTypeface());
			mStrokePaint.setFlags(getPaintFlags());

			// Set our new effects
			mStrokePaint.setStyle(Paint.Style.STROKE);
			mStrokePaint.setColor(mStrokeColor);
			mStrokePaint.setStrokeWidth(mStrokeWidth);

			String text = getText().toString();
			canvas.drawText(text, (getWidth() - mStrokePaint.measureText(text)) / 2, getBaseline(), mStrokePaint);
		}
		super.onDraw(canvas);
	}

}
