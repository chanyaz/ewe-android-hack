package com.expedia.bookings.widget;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.time.widget.DaysOfWeekView;
import com.mobiata.android.time.widget.MonthView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarDateTimeWidget extends RelativeLayout implements
		CalendarPicker.DateSelectionChangedListener,
		CalendarPicker.YearMonthDisplayedChangedListener,
		SeekBar.OnSeekBarChangeListener,
		DaysOfWeekView.DayOfWeekRenderer {

	ICarDateTimeListener listener;
	CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder();

	private LocalDate lastStart;
	private LocalDate lastEnd;

	private static final String TOOLTIP_PATTERN = "hh:mm aa";
	private static final String TOOLTIP_DATE_PATTERN = "MMM dd";
	private DateTimeFormatter df = DateTimeFormat.forPattern(TOOLTIP_DATE_PATTERN);

	@InjectView(R.id.calendar)
	CalendarPicker calendar;

	@InjectView(R.id.days_of_week)
	DaysOfWeekView daysOfWeekView;

	@InjectView(R.id.month)
	MonthView monthView;

	@InjectView(R.id.slider_container)
	ViewGroup sliderContainer;

	@InjectView(R.id.pickup_time_seek_bar)
	CarTimeSlider pickupTimeSeekBar;

	@InjectView(R.id.dropoff_time_seek_bar)
	CarTimeSlider dropoffTimeSeekBar;

	@InjectView(R.id.pickup_time_popup)
	android.widget.TextView pickupTimePopup;

	@InjectView(R.id.pop_up_label)
	android.widget.TextView popupLabel;

	@InjectView(R.id.pickup_time_popup_container)
	LinearLayout pickupTimePopupContainer;

	@InjectView(R.id.pickup_time_popup_text_container)
	LinearLayout pickupTimePopupContainerText;

	@InjectView(R.id.pickup_time_popup_tail)
	ImageView pickupTimePopupTail;

	public CarDateTimeWidget(Context context) {
		super(context);
	}

	public CarDateTimeWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		calendar.setSelectableDateRange(LocalDate.now(),
				LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)));
		calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_flight_search));
		calendar.setDateChangedListener(this);
		calendar.setYearMonthDisplayedChangedListener(this);

		daysOfWeekView.setDayOfWeekRenderer(this);
		daysOfWeekView.setTextColor(getContext().getResources().getColor(R.color.cars_calendar_week_color));
		daysOfWeekView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size));
		monthView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size));
		monthView.setTextEqualDatesColor(Color.WHITE);

		NinePatchDrawable drawablePopUp = (NinePatchDrawable) getResources().getDrawable(R.drawable.toolbar_bg);
		drawablePopUp
				.setColorFilter(getResources().getColor(R.color.cars_tooltip_color), PorterDuff.Mode.SRC_IN);

		pickupTimePopupContainerText.setBackground(drawablePopUp);

		pickupTimeSeekBar.setProgress(new DateTime().plusHours(2).getHourOfDay() * 2);
		pickupTimeSeekBar.addOnSeekBarChangeListener(this);
		dropoffTimeSeekBar.setProgress(new DateTime().plusHours(2).getHourOfDay() * 2);
		dropoffTimeSeekBar.addOnSeekBarChangeListener(this);

		calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		daysOfWeekView.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));
		monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		calendar.setDateChangedListener(null);
		super.onDetachedFromWindow();
	}


	@Override
	public String renderDayOfWeek(LocalDate.Property dayOfWeek) {
		if (Build.VERSION.SDK_INT >= 18) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEEEE", Locale.getDefault());
			return sdf.format(dayOfWeek.getLocalDate().toDate());
		}
		else if (Locale.getDefault().getLanguage().equals("en")) {
			return dayOfWeek.getAsShortText().toUpperCase(Locale.getDefault()).substring(0, 1);
		}
		return DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek);
	}

	/**
	 * Interfaces
	 */

	// Calendar
	@Override
	public void onYearMonthDisplayed(YearMonth yearMonth) {
		pickupTimePopupContainer.setVisibility(View.GONE);
	}

	@Override
	public void onDateSelectionChanged(final LocalDate start, final LocalDate end) {
		dateTimeBuilder.startDate(start);
		dateTimeBuilder.endDate(end);
		dateTimeBuilder.startMillis(convertProgressToMillis(pickupTimeSeekBar.getProgress()));
		if (end != null) {
			dateTimeBuilder.endMillis(convertProgressToMillis(dropoffTimeSeekBar.getProgress()));
		}

		listener.onDateTimeChanged(dateTimeBuilder);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				drawCalendarTooltip(start, end);
			}
		}, 50);
	}

	private static int convertProgressToMillis(int progress) {
		return progress * (30 * 60 * 1000);
	}

	// SeekBar

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		drawSliderTooltip(seekBar);
		if (seekBar.getId() == R.id.pickup_time_seek_bar) {
			dateTimeBuilder.startMillis(convertProgressToMillis(progress));
		}
		else if (seekBar.getId() == R.id.dropoff_time_seek_bar) {
			dateTimeBuilder.endMillis(convertProgressToMillis(progress));
		}
		else {
			throw new RuntimeException("You're using our seekbar listener on an unknown view.");
		}

		listener.onDateTimeChanged(dateTimeBuilder);
	}

	public interface ICarDateTimeListener {
		public void onDateTimeChanged(CarSearchParamsBuilder.DateTimeBuilder dtb);
	}

	public void setCarDateTimeListener(ICarDateTimeListener listener) {
		this.listener = listener;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		drawSliderTooltip(seekBar);
		animateToolTip(pickupTimePopupContainer);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		pickupTimePopupContainer.setVisibility(View.GONE);
	}

	public void drawSliderTooltip(SeekBar seekBar) {

		String title = ((CarTimeSlider) seekBar).calculateProgress(seekBar.getProgress(), TOOLTIP_PATTERN);
		String subtitle = seekBar.getId() == R.id.pickup_time_seek_bar ? getContext().getResources()
				.getString(R.string.cars_time_slider_pick_up_label)
				: getContext().getResources().getString(R.string.cars_time_slider_drop_off_label);
		pickupTimePopup.setText(title);
		popupLabel.setText(subtitle);

		Rect thumbRect = seekBar.getThumb().getBounds();
		final int x = thumbRect.centerX() + seekBar.getLeft();
		final int y = sliderContainer.getTop() + seekBar.getTop() - thumbRect.height()/2;

		pickupTimePopupContainer.setVisibility(View.VISIBLE);
		ViewTreeObserver vto = pickupTimePopupContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				pickupTimePopupContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);

				p.setMargins(x - pickupTimePopupContainer.getMeasuredWidth() / 2,
						y - pickupTimePopupContainer.getMeasuredHeight(), 0, 0);
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pickupTimePopupTail.getLayoutParams();
				lp.gravity = Gravity.CENTER;
				pickupTimePopupTail.setLayoutParams(lp);
				pickupTimePopupContainer.setLayoutParams(p);
			}
		});
	}

	public void drawCalendarTooltip(final LocalDate start, final LocalDate end) {
		String title = end == null ? df.print(start) : df.print(start) + " - " + df.print(end);
		String subtitle = end == null ? getContext().getResources().getString(R.string.cars_calendar_start_date_label)
				: getContext().getResources().getString(R.string.cars_calendar_end_date_label);
		pickupTimePopup.setText(title);
		popupLabel.setText(subtitle);

		final boolean animate = pickupTimePopupContainer.getVisibility() == GONE;

		pickupTimePopupContainer.setVisibility(View.INVISIBLE);
		ViewTreeObserver vto = pickupTimePopupContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				pickupTimePopupContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);

				Point point = lastStart != start ? monthView.getStartDayCoordinates()
						: lastEnd != end ? monthView.getEndDayCoordinates() : monthView.getStartDayCoordinates();
				lastStart = start;
				lastEnd = end;

				int min = calendar.getRight() - pickupTimePopupContainer.getMeasuredWidth();
				int max = point.x - (pickupTimePopupContainer.getMeasuredWidth() / 2);

				int x = Math.min(min, Math.max(0, max));
				int y = point.y + calendar.getTop() - (int) monthView.getRadius() - 10;

				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pickupTimePopupTail.getLayoutParams();
				lp.gravity = x == 0 ? Gravity.LEFT : x == min ? Gravity.RIGHT : Gravity.CENTER;
				pickupTimePopupTail.setLayoutParams(lp);

				p.setMargins(x, y, 0, 0);
				pickupTimePopupContainer.setLayoutParams(p);
				pickupTimePopupContainer.setVisibility(View.VISIBLE);
				if (animate) {
					animateToolTip(pickupTimePopupContainer);
				}
			}
		});

	}

	private void animateToolTip(View v) {
		ScaleAnimation animation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		v.startAnimation(animation);
	}
}
