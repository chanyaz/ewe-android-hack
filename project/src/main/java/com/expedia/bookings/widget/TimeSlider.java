package com.expedia.bookings.widget;

import java.util.ArrayList;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.SeekBar;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.DateFormatUtils;

public class TimeSlider extends SeekBar {

	@ColorInt
	private final int textColor;

	@ColorInt
	private final int trackColor;

	@ColorInt
	private final int thumbColor;

	private Drawable thumb;
	private final ArrayList<OnSeekBarChangeListener> onSeekBarChangeListeners = new ArrayList<>();

	private Paint textPaint;
	private Paint thumbPaint;
	private RectF rectF;

	private final int thumbnailRadius =
		(int) getContext().getResources().getDimension(R.dimen.time_slider_thumbnail_radius);
	private final int imageHeight =
		(int) getContext().getResources().getDimension(R.dimen.time_slider_rectangle_height);
	private final int imageWidth = (int) getContext().getResources().getDimension(R.dimen.time_slider_rectangle_width);
	private final int cornerRadius = (int) getContext().getResources().getDimension(R.dimen.time_slider_corner_radius);

	private Canvas imageCanvas;
	private Bitmap canvasBitmap;

	public static int convertProgressToMillis(int progress) {
		return progress * (30 * 60 * 1000);
	}

	public static int convertMillisToProgress(int millis) {
		return millis / (30 * 60 * 1000);
	}

	public static void animateToolTip(View toolTipView) {
		ScaleAnimation animation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
			Animation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		toolTipView.startAnimation(animation);
	}

	public TimeSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimeSlider);
		int defaultColor = ContextCompat.getColor(getContext(), R.color.white);
		textColor = a.getColor(R.styleable.TimeSlider_slider_text_color, defaultColor);
		trackColor = a.getColor(R.styleable.TimeSlider_slider_track_color, defaultColor);
		thumbColor = a.getColor(R.styleable.TimeSlider_slider_color, defaultColor);
		a.recycle();

		setupThumbnail();
		setThumb(getThumbnail(calculateProgress(getProgress()), false));
		int thumbPadding = getThumb().getIntrinsicWidth() / 2;
		setPadding(thumbPadding, 0, thumbPadding, 0);
		setOnSeekBarChangeListener(onSeekBarChangeListener);

		Drawable progressDrawable = getProgressDrawable().mutate();
		progressDrawable.setColorFilter(trackColor, PorterDuff.Mode.SRC_IN);
		setProgressDrawable(progressDrawable);
	}

	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		this.thumb = thumb;
	}

	public Drawable getThumb() {
		return thumb;
	}

	public void addOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
		onSeekBarChangeListeners.add(listener);
	}

	private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		int previousProgress = 0;
		@Override
		public void onProgressChanged(SeekBar seekBar, int requestedProgress, boolean fromUser) {

			int adjustedProgress = requestedProgress;
			if (fromUser && AccessibilityUtil.isTalkBackEnabled(getContext())) {
				if (requestedProgress > previousProgress) {
					adjustedProgress = previousProgress + 1;
				}
				else if (requestedProgress < previousProgress) {
					adjustedProgress = previousProgress - 1;
				}
			}

			for (OnSeekBarChangeListener listener : onSeekBarChangeListeners) {
				listener.onProgressChanged(seekBar, adjustedProgress, fromUser);
			}

			if ((!fromUser) || AccessibilityUtil.isTalkBackEnabled(getContext())) {
				setThumb(getThumbnail(calculateProgress(getProgress()), false));
			}

			previousProgress = adjustedProgress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			setThumb(getThumbnail(calculateProgress(getProgress()), true));
			for (OnSeekBarChangeListener listener : onSeekBarChangeListeners) {
				listener.onStartTrackingTouch(seekBar);
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			setThumb(getThumbnail(calculateProgress(getProgress()), false));
			for (OnSeekBarChangeListener listener : onSeekBarChangeListeners) {
				listener.onStopTrackingTouch(seekBar);
			}
		}
	};

	public void setupThumbnail() {
		textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(textColor);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(getContext().getResources().getDimension(R.dimen.time_slider_text_size));
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setFakeBoldText(false);
		textPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.time_slider_text_container));

		thumbPaint = new Paint();
		thumbPaint.setAntiAlias(true);
		thumbPaint.setColor(thumbColor);
		thumbPaint.setStyle(Paint.Style.FILL);

		Rect rect = new Rect(0, 0, imageWidth, imageHeight);
		rectF = new RectF(rect);

		canvasBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

		imageCanvas = new Canvas(canvasBitmap);
	}

	public Drawable getThumbnail(String text, boolean isTouching) {
		imageCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		if (isTouching) {
			imageCanvas.drawCircle(imageWidth / 2, imageHeight / 2, thumbnailRadius / 2, thumbPaint);
		}
		else {
			imageCanvas.drawRoundRect(rectF, cornerRadius, cornerRadius, thumbPaint);

			int xPos = (imageWidth / 2);
			int yPos = (int) (imageHeight / 2 - ((textPaint.descent() + textPaint.ascent()) / 2));

			imageCanvas.drawText(text, xPos, yPos, textPaint);
		}

		return new BitmapDrawable(getResources(), canvasBitmap);
	}

	public String calculateProgress(int progress) {
		return DateUtils
			.formatDateTime(getContext(), getDateTime(progress).getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
	}

	public static DateTime getDateTime(int progress) {
		DateTime date = new DateTime();
		DateTime startTime = date.withTimeAtStartOfDay();
		return startTime.plusMinutes(progress * 30);
	}

	public DateTime getDateTime() {
		return getDateTime(getProgress());
	}

	public void setProgress(DateTime time) {
		// DateTime from external search will be in this format 2015-06-25T11:30:00.
		int progress = (time.getHourOfDay() * 2) + (time.getMinuteOfHour() > 29 ? 1 : 0);
		setProgress(progress);
	}
}
