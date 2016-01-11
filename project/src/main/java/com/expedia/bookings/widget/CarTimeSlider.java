package com.expedia.bookings.widget;

import java.util.ArrayList;

import org.joda.time.DateTime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Ui;

/**
 * Created by mohsharma on 2/9/15.
 */
public class CarTimeSlider extends com.expedia.bookings.widget.SeekBar {

	private Drawable mThumb;
	private ArrayList<OnSeekBarChangeListener> onSeekBarChangeListeners = new ArrayList<>();

	private Paint textPaint;
	private Paint canvasPaint;
	private RectF rectF;
	private final float scale = getContext().getResources().getDisplayMetrics().density;
	private final int imageWidth = (int) getContext().getResources().getDimension(
		R.dimen.car_time_slider_rectangle_width);
	private final int imageHeight = (int) getContext().getResources().getDimension(
		R.dimen.car_time_slider_rectangle_height);
	private final int thumbnailRadius = (int) getContext().getResources().getDimension(
		R.dimen.car_time_slider_thumbnail_radius);
	private final int cornerRadius = (int) getContext().getResources().getDimension(
		R.dimen.car_time_slider_corner_radius);

	private Canvas imageCanvas;
	private Bitmap canvasBitmap;
	private int thumbPadding;

	public CarTimeSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		mThumb = thumb;
	}

	public Drawable getThumb() {
		return mThumb;
	}

	public void init() {
		setupThumbnail();
		setThumb(getThumbnail(calculateProgress(getProgress()), false));
		thumbPadding = getThumb().getIntrinsicWidth() / 2;
		setPadding(thumbPadding, 0, thumbPadding, 0);
		setOnSeekBarChangeListener(onSeekBarChangeListener);
	}

	public void addOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
		onSeekBarChangeListeners.add(listener);
	}

	private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			for (OnSeekBarChangeListener listener : onSeekBarChangeListeners) {
				listener.onProgressChanged(seekBar, progress, fromUser);
			}
			if (!fromUser) {
				setThumb(getThumbnail(calculateProgress(getProgress()), false));
			}
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
		textPaint.setColor(getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_carsSecondaryColor)));
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(getContext().getResources().getDimension(R.dimen.car_time_slider_text_size));
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setFakeBoldText(false);
		textPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.car_time_slider_text_container));

		canvasPaint = new Paint();
		canvasPaint.setAntiAlias(true);
		canvasPaint.setColor(Color.WHITE);
		canvasPaint.setStyle(Paint.Style.FILL);

		Rect rect = new Rect(0, 0, imageWidth,
			imageHeight);
		rectF = new RectF(rect);

		canvasBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

		imageCanvas = new Canvas(canvasBitmap);
	}


	public Drawable getThumbnail(String text, boolean isTouching) {
		imageCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		if (isTouching) {
			imageCanvas.drawCircle(imageWidth / 2, imageHeight / 2, thumbnailRadius / 2, canvasPaint);
		}
		else {
			imageCanvas.drawRoundRect(rectF, cornerRadius, cornerRadius, canvasPaint);

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

	public DateTime getDateTime(int progress) {
		DateTime date = new DateTime();
		DateTime startTime = date.withTimeAtStartOfDay();
		DateTime newTime = startTime.plusMinutes(progress * 30);
		return newTime;
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
