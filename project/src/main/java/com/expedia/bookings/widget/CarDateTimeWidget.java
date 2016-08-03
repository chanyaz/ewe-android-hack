package com.expedia.bookings.widget;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.BaseSearchPresenter;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.time.widget.DaysOfWeekView;
import com.mobiata.android.time.widget.MonthView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.expedia.bookings.widget.TimeSlider.convertProgressToMillis;

public class CarDateTimeWidget extends RelativeLayout implements
	CalendarPicker.DateSelectionChangedListener,
	CalendarPicker.YearMonthDisplayedChangedListener,
	SeekBar.OnSeekBarChangeListener {

	ICarDateTimeListener listener;
	CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder();

	//2 hours in millis
	private static final long PICKUP_DROPOFF_MINIMUM_TIME_DIFFERENCE = TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS);
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
	TimeSlider pickupTimeSeekBar;

	@InjectView(R.id.dropoff_time_seek_bar)
	TimeSlider dropoffTimeSeekBar;

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

		LocalDate todayDate = LocalDate.now();
		LocalDate tomorrowDate = LocalDate.now().plusDays(1);
		boolean isEleventhHour = DateTime.now().getHourOfDay() == 23;
		LocalDate startDate = isEleventhHour ? tomorrowDate : todayDate;
		calendar.setSelectableDateRange(startDate, startDate
			.plusDays(getResources().getInteger(R.integer.calendar_max_days_car_search)));
		calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_car_search));
		calendar.setDateChangedListener(this);
		calendar.setYearMonthDisplayedChangedListener(this);

		daysOfWeekView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size));

		Context context = getContext();
		Drawable drawablePopUp = ContextCompat.getDrawable(context, R.drawable.toolbar_bg).mutate();

		int color = ContextCompat.getColor(context, Ui.obtainThemeResID(getContext(), R.attr.skin_carsTooltipColor));
		drawablePopUp.setColorFilter(color, PorterDuff.Mode.SRC_IN);

		pickupTimePopupContainerText.setBackground(drawablePopUp);

		pickupTimeSeekBar.setProgress(new DateTime().withHourOfDay(9).withMinuteOfHour(0));
		pickupTimeSeekBar.addOnSeekBarChangeListener(this);
		dropoffTimeSeekBar.setProgress(new DateTime().withHourOfDay(18).withMinuteOfHour(0));
		dropoffTimeSeekBar.addOnSeekBarChangeListener(this);

		BaseSearchPresenter.styleCalendar(getContext(), calendar, monthView, daysOfWeekView);
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

	/**
	 * Interfaces
	 */

	// Calendar
	@Override
	public void onYearMonthDisplayed(YearMonth yearMonth) {
		pickupTimePopupContainer.setVisibility(View.GONE);
		calendar.hideToolTip();
	}

	public void buildParams(final LocalDate start, final LocalDate end) {
		dateTimeBuilder.startDate(start);
		dateTimeBuilder.endDate(end);
		dateTimeBuilder.startMillis(convertProgressToMillis(pickupTimeSeekBar.getProgress()));
		if (end != null) {
			dateTimeBuilder.endMillis(convertProgressToMillis(dropoffTimeSeekBar.getProgress()));
		}
		listener.onDateTimeChanged(dateTimeBuilder);
	}

	@Override
	public void onDateSelectionChanged(final LocalDate start, final LocalDate end) {

		if (start == null) {
			return;
		}

		// Logic to change the time slider value when user selects current date (only if a valid time ahead in future is not already selected)
		DateTime now = DateTime.now();
		if (start.equals(LocalDate.now()) && now.getHourOfDay() >= 8
			&& pickupTimeSeekBar.getDateTime().getMillis() < now.plusHours(1).getMillis()) {
			pickupTimeSeekBar.setProgress(now.plusHours(1));
		}
		if (end != null && end.equals(LocalDate.now()) && now.getHourOfDay() >= 16
			&& dropoffTimeSeekBar.getDateTime().getMillis() < now.plusHours(3).getMillis()) {
			dropoffTimeSeekBar.setProgress(now.plusHours(3));
		}

		buildParams(start, end);
		drawCalendarTooltip(start, end);
		validateTimes();
	}

	// SeekBar

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar.getId() == R.id.pickup_time_seek_bar) {
			dateTimeBuilder.startMillis(convertProgressToMillis(progress));
			DateTime now = DateTime.now();
			setUpTooltipColor(isStartTimeBeforeCurrentTime(now));
		}
		else if (seekBar.getId() == R.id.dropoff_time_seek_bar) {
			dateTimeBuilder.endMillis(convertProgressToMillis(progress));
			setUpTooltipColor(isEndTimeBeforeStartTime());
		}
		else {
			throw new RuntimeException("You're using our seekbar listener on an unknown view.");
		}
		if (fromUser) {
			drawSliderTooltip((TimeSlider) seekBar);
		}
		listener.onDateTimeChanged(dateTimeBuilder);
	}

	public interface ICarDateTimeListener {
		void onDateTimeChanged(CarSearchParamsBuilder.DateTimeBuilder dtb);
	}

	public void setCarDateTimeListener(ICarDateTimeListener listener) {
		this.listener = listener;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		drawSliderTooltip((TimeSlider) seekBar);
		TimeSlider.animateToolTip(pickupTimePopupContainer);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		validateTimes();
		pickupTimePopupContainer.setVisibility(View.GONE);
	}

	// Reset times if the start is equal to today and the selected time is before the current time
	// or if the end time is earlier or equal to the start time and its the same day.
	public void validateTimes() {
		DateTime now = DateTime.now();

		if (isStartTimeBeforeCurrentTime(now)) {
			pickupTimeSeekBar.setProgress(now.plusHours(1));
		}
		if (isEndTimeBeforeStartTime()) {
			dropoffTimeSeekBar.setProgress(pickupTimeSeekBar.getProgress() + 4);
		}
	}

	private boolean isStartTimeBeforeCurrentTime(DateTime now) {
		return dateTimeBuilder.isStartDateEqualToToday() && dateTimeBuilder.getStartMillis() < now.getMillisOfDay();
	}

	//end time should always be at least 2 hours ahead of start time
	private boolean isEndTimeBeforeStartTime() {
		return dateTimeBuilder.isStartEqualToEnd() &&
			dateTimeBuilder.getEndMillis() < dateTimeBuilder.getStartMillis() + PICKUP_DROPOFF_MINIMUM_TIME_DIFFERENCE;
	}

	private void setUpTooltipColor(boolean isValid) {
		Drawable drawablePopUp = ContextCompat.getDrawable(getContext(), R.drawable.toolbar_bg).mutate();
		int color = isValid ? getResources().getColor(R.color.cars_tooltip_disabled_color)
			: getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_carsTooltipColor));
		drawablePopUp.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		pickupTimePopupContainerText.setBackground(drawablePopUp);
		pickupTimePopupTail.setColorFilter(color, PorterDuff.Mode.SRC_IN);
	}

	public void drawSliderTooltip(TimeSlider seekBar) {
		calendar.hideToolTip();
		String title = seekBar.calculateProgress(seekBar.getProgress());
		String subtitle = seekBar.getId() == R.id.pickup_time_seek_bar ? getContext().getResources()
			.getString(R.string.cars_time_slider_pick_up_label)
			: getContext().getResources().getString(R.string.cars_time_slider_drop_off_label);
		pickupTimePopup.setText(title);
		popupLabel.setText(subtitle);

		Rect thumbRect = seekBar.getThumb().getBounds();
		final int x = thumbRect.centerX() + seekBar.getLeft();
		final int y = sliderContainer.getTop() + seekBar.getTop() - thumbRect.height() / 2;

		pickupTimePopupContainer.setVisibility(View.VISIBLE);
		ViewTreeObserver vto = pickupTimePopupContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				pickupTimePopupContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
		calendar.setToolTipText(title, subtitle, true);
	}

	public void setPickupTime(DateTime startDateTime) {
		pickupTimeSeekBar.setProgress(startDateTime);
	}

	public void setDropoffTime(DateTime endDateTime) {
		dropoffTimeSeekBar.setProgress(endDateTime);
	}

	public void hideToolTip() {
		calendar.hideToolTip();
	}
}
