package com.mobiata.android.time.widget;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mobiata.android.Log;
import com.mobiata.android.graphics.CaretDrawable;
import com.mobiata.android.graphics.CaretDrawable.Direction;
import com.mobiata.android.time.R;
import com.mobiata.android.time.util.JodaUtils;
import com.mobiata.android.util.Ui;

/**
 * A calendar date picker.
 *
 * A quick guide to usage:
 * - Use setDateChangedListener() to listen to date changes.
 * - Use setSelectedDates() to change which dates are currently selected.
 * - Use setSelectableDateRange() to select the minimum/maximum selectable dates.
 * - Use setMaxSelectableDateRange() to select the longest duration one can select.
 * - Use a style derived from "Widget.CalendarPicker" to theme it.
 *
 * Notes:
 * - CalendarPicker does not work with layout_width="wrap_content" because it has no
 *   minimum width.  Either use "match_parent" or specify a pixel width.
 *
 * TODO: Scale all views based on size of CalendarPicker itself
 */
public class CalendarPicker extends LinearLayout {

	// Constants
	// The base value to scale by log4(x)
	private static final int DURATION_WEEK_MULTIPLIER = 300;
	// We use log base 4 because we typically scale by 4 weeks at a time (only sometimes 5)
	private static final double DURATION_WEEK_LOG_BASE = Math.log(4);

	// State
	private CalendarState mState = new CalendarState();

	// Styles - loaded at start, not modifiable
	private int mBaseColor;
	private int mSecondaryColor;
	private int mHighlightColor;
	private int mHighlightInverseColor;
	private int mTodayColor;
	private int mInvalidColor;
	private int mDaysOfWeekColor;
	private int mSelectionDayFillColor;
	private int mSelectionWeekHighlightColor;
	private int mChangeMonthCaretColor;
	private int mHeaderTextColor;
	private boolean mHeaderShowInstructions;
	private float mHeaderTextSize;

	// Subviews
	private ImageView mPreviousMonthView;
	private TextView mCurrentMonthTextView;
	private ImageView mNextMonthView;
	private DaysOfWeekView mDaysOfWeekView;
	private MonthView mMonthView;
	private TextView mInstructions;

	// Drawables
	private CaretDrawable mPreviousMonthCaret;
	private CaretDrawable mNextMonthCaret;
	private boolean mUseCaretDrawables;

	// Listeners
	private DateSelectionChangedListener mDateSelectionChangedListener;
	private YearMonthDisplayedChangedListener mYearMonthDisplayedChangedListener;

	// Animation
	private boolean mAttachedToWindow;
	private float mTranslationWeekTarget = 0;
	private Animator mMonthAnimator;

	//The tooltip popup
	private boolean mTooltipEnabled = false;
	private TooltipPopup mTooltipPopupWindow;
	//coordinates of month view in window
	int[] mMonthViewCoordinates;

	public CalendarPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		// Load attributes
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CalendarPicker);
		mBaseColor = ta.getColor(R.styleable.CalendarPicker_calendarBaseColor, 0);
		mSecondaryColor = ta.getColor(R.styleable.CalendarPicker_calendarSecondaryColor, 0xffcccccc);
		mHighlightColor = ta.getColor(R.styleable.CalendarPicker_calendarHighlightColor, 0xff24beef);
		mSelectionDayFillColor = ta.getColor(R.styleable.CalendarPicker_calendarDaySelectionFillColor, mHighlightColor);
		mSelectionWeekHighlightColor = ta
			.getColor(R.styleable.CalendarPicker_calendarWeekSelectionColor, mHighlightColor);
		mHighlightInverseColor = ta.getColor(R.styleable.CalendarPicker_calendarHighlightInverseColor, 0);
		mTodayColor = ta.getColor(R.styleable.CalendarPicker_calendarTodayColor, 0xff0000aa);
		mInvalidColor = ta.getColor(R.styleable.CalendarPicker_calendarInvalidDaysColor, 0xffaaaaaa);
		mDaysOfWeekColor = ta.getColor(R.styleable.CalendarPicker_calendarDaysOfWeekColor, mBaseColor);
		mChangeMonthCaretColor = ta.getColor(R.styleable.CalendarPicker_calendarChangeMonthCaretColor, mHighlightColor);
		mHeaderTextColor = ta.getColor(R.styleable.CalendarPicker_calendarHeaderTextColor, mBaseColor);
		mHeaderTextSize = ta.getDimension(R.styleable.CalendarPicker_calendarHeaderTextSize,
			12 * getResources().getDisplayMetrics().density);
		mHeaderShowInstructions = ta.getBoolean(R.styleable.CalendarPicker_calendarHeaderShowInstructions, false);
		int layoutResource = ta
			.getResourceId(R.styleable.CalendarPicker_calendarLayout, R.layout.widget_calendar_picker);

		//checking tooltip is enabled or not
		mTooltipEnabled = ta.getBoolean(R.styleable.CalendarPicker_calendarToolTipEnabled, false);
		if (mTooltipEnabled) {
			mTooltipPopupWindow = new TooltipPopup(context);
			mTooltipPopupWindow.setTextColor(mSelectionDayFillColor);

			//setting tooltip background
			if (ta.hasValue(R.styleable.CalendarPicker_calendarToolTipBackground)) {
				NinePatchDrawable drawablePopUp = (NinePatchDrawable) ta.getDrawable(
					R.styleable.CalendarPicker_calendarToolTipBackground).mutate();
				drawablePopUp.setColorFilter(mDaysOfWeekColor, PorterDuff.Mode.SRC_IN);
				mTooltipPopupWindow.setBackgroundDrawable(drawablePopUp);
			}

			//setting tooltip tail drawable
			if (ta.hasValue(R.styleable.CalendarPicker_calendarToolTipTailDrawable)) {
				BitmapDrawable drawablePopUp = (BitmapDrawable) ta.getDrawable(
					R.styleable.CalendarPicker_calendarToolTipTailDrawable).mutate();
				drawablePopUp.setColorFilter(mDaysOfWeekColor, PorterDuff.Mode.SRC_IN);
				mTooltipPopupWindow.setTailDrawable(drawablePopUp);
			}

			mMonthViewCoordinates = new int[2];
		}

		// TODO this logic makes me cry, but so does everything about supporting different methods of drawing the month advance arrows
		mUseCaretDrawables = layoutResource == R.layout.widget_calendar_picker;

		ta.recycle();

		// Configure some layout params (so you don't have to do it in XML)
		setOrientation(LinearLayout.VERTICAL);

		// Inflate the widget
		inflate(context, layoutResource, this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Retrieve Views
		mPreviousMonthView = Ui.findView(this, R.id.previous_month);
		mCurrentMonthTextView = Ui.findView(this, R.id.current_month);
		mNextMonthView = Ui.findView(this, R.id.next_month);
		mDaysOfWeekView = Ui.findView(this, R.id.days_of_week);
		mMonthView = Ui.findView(this, R.id.month);
		mInstructions = Ui.findView(this, R.id.instructions);

		// Configure Views
		mCurrentMonthTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeaderTextSize);
		mCurrentMonthTextView.setTextColor(mHeaderTextColor);

		int caretBackgroundColor = ((mHighlightColor >>> 25) << 24) | (mHighlightColor & 0x00ffffff);
		StateListDrawable previousCaretBackground = new StateListDrawable();
		previousCaretBackground
			.addState(new int[] { android.R.attr.state_pressed }, new ColorDrawable(caretBackgroundColor));
		previousCaretBackground.addState(StateSet.WILD_CARD, new ColorDrawable(0));
		StateListDrawable nextCaretBackground = new StateListDrawable();
		nextCaretBackground
			.addState(new int[] { android.R.attr.state_pressed }, new ColorDrawable(caretBackgroundColor));
		nextCaretBackground.addState(StateSet.WILD_CARD, new ColorDrawable(0));

		if (mUseCaretDrawables) {
			mPreviousMonthCaret = new CaretDrawable(Direction.LEFT, mChangeMonthCaretColor);
			mPreviousMonthView.setImageDrawable(mPreviousMonthCaret);
			mPreviousMonthView.setBackgroundDrawable(previousCaretBackground);
			mPreviousMonthView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					if (mPreviousMonthView.getWidth() == 0) {
						// if the view hasn't be laid out yet, try again later
						return true;
					}

					mPreviousMonthView.getViewTreeObserver().removeOnPreDrawListener(this);

					computePrevMonthCaret();

					return false;
				}
			});

			mNextMonthCaret = new CaretDrawable(Direction.RIGHT, mChangeMonthCaretColor);
			mNextMonthView.setImageDrawable(mNextMonthCaret);
			mNextMonthView.setBackgroundDrawable(nextCaretBackground);
			mNextMonthView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					if (mNextMonthView.getWidth() == 0) {
						// if the view hasn't be laid out yet, try again later
						return true;
					}

					mNextMonthView.getViewTreeObserver().removeOnPreDrawListener(this);

					computeNextMonthCaret();

					return false;
				}
			});
		}

		if (mInstructions != null) {
			if (mHeaderShowInstructions) {
				mInstructions.setVisibility(VISIBLE);
			}
			else {
				mInstructions.setVisibility(GONE);
			}
		}

		mPreviousMonthView.setOnClickListener(mOnClickListener);
		mNextMonthView.setOnClickListener(mOnClickListener);

		mDaysOfWeekView.setTextColor(mDaysOfWeekColor);
		mDaysOfWeekView.setMaxTextSize(mCurrentMonthTextView.getTextSize() * 0.75f);

		mMonthView.setCalendarState(mState);
		mMonthView.setTextColor(mBaseColor);
		mMonthView.setTextSecondaryColor(mSecondaryColor);
		mMonthView.setHighlightColor(mHighlightColor);
		mMonthView.setHighlightInverseColor(mHighlightInverseColor);
		mMonthView.setSelectionDayColor(mSelectionDayFillColor);
		mMonthView.setSelectionWeekHighlightColor(mSelectionWeekHighlightColor);
		mMonthView.setTodayColor(mTodayColor);
		mMonthView.setInvalidDayColor(mInvalidColor);
		mMonthView.setMaxTextSize(mCurrentMonthTextView.getTextSize());
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		// Restore without calling setters, to avoid notifications firing
		mState.mDisplayYearMonth = ss.displayMonthYear;
		mState.mStartDate = ss.startDate;
		mState.mEndDate = ss.endDate;
		mState.mMinSelectableDate = ss.minSelectableDate;
		mState.mMaxSelectableDate = ss.maxSelectableDate;
		mState.mMaxSelectableDateRange = ss.maxSelectableDateRange;
		mState.updateLastState();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (mUseCaretDrawables) {
			computePrevMonthCaret();
			computeNextMonthCaret();
		}

		syncDisplayMonthCarets();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Wait until here to start manipulating sub-Views; that way we can
		// restore the instance state properly first.
		syncViewsWithState();

		mAttachedToWindow = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mAttachedToWindow = false;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.displayMonthYear = mState.mDisplayYearMonth;
		ss.startDate = mState.mStartDate;
		ss.endDate = mState.mEndDate;
		ss.minSelectableDate = mState.mMinSelectableDate;
		ss.maxSelectableDate = mState.mMaxSelectableDate;
		ss.maxSelectableDateRange = mState.mMaxSelectableDateRange;

		return ss;
	}

	//////////////////////////////////////////////////////////////////////////
	// Outside control

	public void setDateChangedListener(DateSelectionChangedListener listener) {
		mDateSelectionChangedListener = listener;
	}

	public void setYearMonthDisplayedChangedListener(YearMonthDisplayedChangedListener listener) {
		mYearMonthDisplayedChangedListener = listener;
	}

	public void setDisplayYearMonth(YearMonth month) {
		mState.setDisplayYearMonth(month);
	}

	public void setSelectedDates(LocalDate startDate, LocalDate endDate) {
		mState.setSelectedDates(startDate, endDate);
		mState.setDisplayYearMonth(new YearMonth(startDate));
	}

	public void setToolTipText(String topText, String bottomText, String contentDescription, boolean useTooltipAnimations) {
		setToolTipText(topText, bottomText, contentDescription, useTooltipAnimations, true);
	}

	public void setInstructionText(CharSequence text) {
		if (mInstructions != null) {
			mInstructions.setText(text);
		}
	}

	public void setToolTipText(final String topText, final String bottomText, final String contentDescription, boolean useTooltipAnimations, boolean show) {
		if (mTooltipPopupWindow != null && this.isShown()) {
			final boolean animateTooltips = !mTooltipPopupWindow.isShowing() && useTooltipAnimations;

			if (mTooltipPopupWindow.isShowing()) {
				mTooltipPopupWindow.dismiss();
			}
			mTooltipPopupWindow.setTopText(topText);
			mTooltipPopupWindow.setBottomText(bottomText);

			if (show) {
				new Handler().postDelayed(new Runnable() {
					public void run() {
						mMonthView.getLocationInWindow(mMonthViewCoordinates);
						if (mState.getEndDate() == null) {
							if (mMonthView.getStartDayCoordinates().x != 0) {
								mTooltipPopupWindow
									.show(mMonthView, mMonthView.getStartDayCoordinates().x + mMonthViewCoordinates[0],
										mMonthView.getStartDayCoordinates().y + mMonthViewCoordinates[1],
										animateTooltips);
							}
						}
						else {
							if (mMonthView.getEndDayCoordinates().x != 0) {
								mTooltipPopupWindow
									.show(mMonthView, mMonthView.getEndDayCoordinates().x + mMonthViewCoordinates[0],
										mMonthView.getEndDayCoordinates().y + mMonthViewCoordinates[1],
										animateTooltips);
							}
						}
						mTooltipPopupWindow.getContentView().announceForAccessibility(contentDescription);
					}
				}, 50);
			}
		}
	}

	public void hideToolTip() {
		if (mTooltipPopupWindow != null && mTooltipPopupWindow.isShowing()) {
			mTooltipPopupWindow.dismiss();
		}
	}

	/**
	 * Defines the selectable date range.
	 *
	 * @param minDate the minimum selectable date or null for no minimum
	 * @param maxDate the maximum selectable date or null for no maximum
	 */
	public void setSelectableDateRange(LocalDate minDate, LocalDate maxDate) {
		mState.setSelectableDateRange(minDate, maxDate);
	}

	public void setMaxSelectableDateRange(int numDays) {
		mState.setMaxSelectableDateRange(numDays);
	}

	public LocalDate getStartDate() {
		return mState.mStartDate;
	}

	public LocalDate getEndDate() {
		return mState.mEndDate;
	}

	//////////////////////////////////////////////////////////////////////////
	// Styling

	public void setMonthHeaderTypeface(Typeface typeface) {
		mCurrentMonthTextView.setTypeface(typeface);
	}

	public void setDaysOfWeekTypeface(Typeface typeface) {
		mDaysOfWeekView.setTypeface(typeface);
	}

	public void setMonthViewTypeface(Typeface typeface) {
		mMonthView.setDaysTypeface(typeface);
	}

	//////////////////////////////////////////////////////////////////////////
	// Display

	// While this is easier, try to avoid calling it unless you really need to
	// sync *all* Views at once
	private void syncViewsWithState() {
		syncDisplayMonthViews();
		syncDateSelectionViews();
	}

	private void syncDisplayMonthViews() {
		// Animate the month view changing if we're attached (that means we're past the setup phase)
		if (mAttachedToWindow) {
			animateMonth(mState.mLastState.mDisplayYearMonth, mState.mDisplayYearMonth);
		}
		else {
			mMonthView.notifyDisplayYearMonthChanged();
		}

		// Update header
		Context context = getContext();

		DateTimeFormatter monthYearFormatter = DateTimeFormat
			.forPattern(getResources().getString(R.string.calendar_current_month_format));

		mPreviousMonthView.setContentDescription(context.getString(R.string.cd_month_previous_TEMPLATE,
			monthYearFormatter.print(mState.mDisplayYearMonth.minusMonths(1))));

		String currMonth = monthYearFormatter.print(mState.mDisplayYearMonth);
		mCurrentMonthTextView.setText(currMonth);
		mCurrentMonthTextView.setContentDescription(context.getString(R.string.cd_month_current_TEMPLATE, currMonth));
		mCurrentMonthTextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
		mNextMonthView.setContentDescription(context.getString(R.string.cd_month_next_TEMPLATE,
			monthYearFormatter.print(mState.mDisplayYearMonth.plusMonths(1))));

		syncDisplayMonthCarets();
	}

	private void syncDisplayMonthCarets() {
		// Show carets based on min/max selectable dates
		if (mPreviousMonthView != null && mNextMonthView != null) {
			mPreviousMonthView.setVisibility(
				mState.canDisplayYearMonth(mState.mDisplayYearMonth.minusMonths(1)) ? VISIBLE : INVISIBLE);
			mNextMonthView.setVisibility(
				mState.canDisplayYearMonth(mState.mDisplayYearMonth.plusMonths(1)) ? VISIBLE : INVISIBLE);
		}
	}

	private void computePrevMonthCaret() {
		computeMonthCaret(mPreviousMonthView, mPreviousMonthCaret);
	}

	private void computeNextMonthCaret() {
		computeMonthCaret(mNextMonthView, mNextMonthCaret);
	}

	private void computeMonthCaret(View monthImageView, CaretDrawable caretDrawable) {
		int caretHeight = (int) mCurrentMonthTextView.getTextSize();
		int caretWidth = (int) Math.floor(caretHeight / 1.5);
		float caretStrokeWidth = caretHeight / 8f;

		caretDrawable.setStrokeWidth(caretStrokeWidth);

		int horizPadding = (monthImageView.getMeasuredWidth() - caretWidth) / 2;
		monthImageView.setPadding(horizPadding, monthImageView.getPaddingTop(), horizPadding,
			monthImageView.getPaddingBottom());
		monthImageView.requestLayout();
	}

	private void syncDateSelectionViews() {
		mMonthView.notifySelectedDatesChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// Month animation

	/**
	 * This animates the MonthView from one month to another.
	 *
	 * It gets a bit more complex if the MonthView is already animating.  In that case,
	 * it stops the current animation and begins a new one *from wherever the MonthView
	 * is* to the next month.  The duration is scaled based on how far it now has to travel.
	 */
	private void animateMonth(YearMonth fromMonth, YearMonth toMonth) {
		if (mMonthAnimator != null && mMonthAnimator.isRunning()) {
			mMonthAnimator.cancel();
		}

		// We need to calculate *how many weeks* are translated going fromMonth --> toMonth
		// This has been determined to be the (number of days / 7) (+ 1 IF days of week swap)
		LocalDate fromMonthFirstDay = fromMonth.toLocalDate(1);
		LocalDate toMonthFirstDay = toMonth.toLocalDate(1);
		int fromDayOfWeek = JodaUtils.getDayOfWeekNormalized(fromMonthFirstDay);
		int toDayOfWeek = JodaUtils.getDayOfWeekNormalized(toMonthFirstDay);
		int translationWeeks = Weeks.weeksBetween(fromMonthFirstDay, toMonthFirstDay).getWeeks();
		if (translationWeeks < 0 && fromDayOfWeek < toDayOfWeek) {
			translationWeeks--;
		}
		else if (translationWeeks > 0 && fromDayOfWeek > toDayOfWeek) {
			translationWeeks++;
		}

		mTranslationWeekTarget += translationWeeks;

		float currentShift = mMonthView.getTranslationWeeks();

		mMonthAnimator = ObjectAnimator.ofFloat(mMonthView, MonthView.TRANSLATE_WEEKS, mTranslationWeekTarget);
		mMonthAnimator.addListener(mMonthAnimatorListener);

		// We use a logarithmic scale so that the further you go, the less duration we add to the total
		double durationBase = Math.log(Math.abs(mTranslationWeekTarget - currentShift) + 1) / DURATION_WEEK_LOG_BASE;
		int duration = (int) Math.round(DURATION_WEEK_MULTIPLIER * durationBase);
		mMonthAnimator.setDuration(duration);

		mMonthAnimator.start();
	}

	// Once the animation is done we want to re-center the MonthView, but only if
	// has actually finished (and the animation wasn't just cancelled midway through)
	private AnimatorListener mMonthAnimatorListener = new AnimatorListenerAdapter() {

		private boolean mActuallyEnding;

		@Override
		public void onAnimationStart(Animator animation) {
			mActuallyEnding = true;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mActuallyEnding) {
				mMonthView.notifyDisplayYearMonthChanged();
				mTranslationWeekTarget = 0;
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mActuallyEnding = false;
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			YearMonth yearMonth = null;
			if (v == mPreviousMonthView) {
				yearMonth = mState.mDisplayYearMonth.minusMonths(1);
			}
			else if (v == mNextMonthView) {
				yearMonth = mState.mDisplayYearMonth.plusMonths(1);
			}

			if (yearMonth != null && mState.canDisplayYearMonth(yearMonth)) {
				mState.setDisplayYearMonth(yearMonth);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface DateSelectionChangedListener {
		void onDateSelectionChanged(LocalDate start, LocalDate end);
	}

	public interface YearMonthDisplayedChangedListener {
		void onYearMonthDisplayed(YearMonth yearMonth);
	}

	// class used to display tooltip
	private class TooltipPopup extends PopupWindow {
		private ViewGroup mTooltipContainer;
		private LinearLayout mTooltipTextContainer;
		private ImageView mTooltipTail;
		private TextView mTopText;
		private TextView mBottomText;

		public TooltipPopup(Context context) {
			super(context);
			super.setBackgroundDrawable(null);
			super.setTouchable(false);
			LayoutInflater inflater = LayoutInflater.from(context);
			mTooltipContainer = (ViewGroup) inflater.inflate(R.layout.widget_calendar_tooltip, null);
			mTooltipTextContainer = Ui.findView(mTooltipContainer, R.id.tooltip_text_container);
			mTopText = Ui.findView(mTooltipContainer, R.id.tooltip_line_one);
			mBottomText = Ui.findView(mTooltipContainer, R.id.tooltip_line_two);
			mTooltipTail = Ui.findView(mTooltipContainer, R.id.tooltip_tail);

			setContentView(mTooltipContainer);

			measureTT();
		}

		private void measureTT() {
			mTooltipContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		}

		public void setTopText(String topText) {
			mTopText.setText(topText);
		}

		public void setBottomText(String bottomText) {
			mBottomText.setText(bottomText);
		}

		/**
		 * Set the background of the text lines.
		 */
		public void setBackgroundDrawable(Drawable drawable) {
			if (mTooltipTextContainer != null) {
				mTooltipTextContainer.setBackgroundDrawable(drawable);
			}
		}

		/**
		 * The tail, the little bugger that points to the correct date
		 */
		public void setTailDrawable(Drawable drawable) {
			mTooltipTail.setImageDrawable(drawable);
		}

		/**
		 * Set the color of both lines of text
		 * @param color
		 */
		public void setTextColor(int color) {
			mTopText.setTextColor(color);
			mBottomText.setTextColor(color);
		}

		/**
		 * Show the popup given that we have valid positions to show.
		 * @param anchor (an anchor view)
		 */
		public void show(final View anchor, final int mPosX, final int mPosY, boolean animateTooltips) {
			ViewTreeObserver vto = mTooltipContainer.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					mTooltipContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

					setTailPosition(mPosX);

					showAtLocation(anchor, Gravity.NO_GRAVITY,
						mPosX - (mTooltipContainer.getMeasuredWidth() / 2),
						mPosY - mTooltipContainer.getMeasuredHeight());
					update(mPosX - (mTooltipContainer.getMeasuredWidth() / 2),
						mPosY - mTooltipContainer.getMeasuredHeight(),
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				}
			});
			this.setAnimationStyle(animateTooltips ? R.style.Widget_CalendarPicker_Animation : 0);
			this.showAtLocation(anchor, Gravity.NO_GRAVITY, mPosX - mTooltipContainer.getMeasuredHeight(),
				mPosY - mTooltipContainer.getMeasuredHeight());
			this.update(mPosX - mTooltipContainer.getMeasuredHeight(),
				mPosY - mTooltipContainer.getMeasuredHeight(), ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		private void setTailPosition(int tailCenterXAbsoluteInWindow) {
			int tooltipMidpoint = mTooltipContainer.getMeasuredWidth() / 2;
			int tooltipTailTranslationX = tooltipMidpoint;

			/* most of the time, tailCenterXAbsoluteInWindow will line up with the center of the tooltip.
				if it's at the edge though, we may have to adjust where the tail is drawn */
			if (tailCenterXAbsoluteInWindow - tooltipMidpoint < 0) {
				tooltipTailTranslationX = tailCenterXAbsoluteInWindow;
			}
			else if (tailCenterXAbsoluteInWindow + tooltipMidpoint >
				(mMonthViewCoordinates[0] + mMonthView.getMeasuredWidth())) {
				int monthViewRight = (mMonthViewCoordinates[0] + mMonthView.getMeasuredWidth());
				tooltipTailTranslationX =
					mTooltipContainer.getMeasuredWidth() - mMonthViewCoordinates[0]
						- (monthViewRight - tailCenterXAbsoluteInWindow);
			}

			tooltipTailTranslationX -= mTooltipTail.getMeasuredWidth() / 2;
			mTooltipTail.setX(tooltipTailTranslationX);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CalendarPicker State class
	//
	// We keep one set of settings; this should be shared between all classes
	// that need it (e.g. the MonthView).
	//
	// It is in charge of notifying all related Views whenever something
	// important changes.

	protected final class CalendarState {

		private YearMonth mDisplayYearMonth;

		private LocalDate mStartDate;
		private LocalDate mEndDate;

		private LocalDate mMinSelectableDate;
		private LocalDate mMaxSelectableDate;

		private int mMaxSelectableDateRange;

		// We keep track of what has changed so that we don't do unnecessary View updates
		private CalendarState mLastState;

		public CalendarState() {
			// Default to displaying current year month
			mDisplayYearMonth = YearMonth.now();
		}

		public void setDisplayYearMonth(YearMonth yearMonth) {
			mDisplayYearMonth = yearMonth;
			validateAndSyncState();
		}

		public YearMonth getDisplayYearMonth() {
			return mDisplayYearMonth;
		}

		public void setSelectedDates(LocalDate startDate, LocalDate endDate) {
			if (startDate == null && endDate != null) {
				throw new IllegalArgumentException("Can't set an end date without a start date!  end=" + endDate);
			}
			else if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
				throw new IllegalArgumentException("Can't set an end date BEFORE a start date!  start=" + startDate
						+ " end=" + endDate);
			}

			if (!JodaUtils.isEqual(startDate, mStartDate) || !JodaUtils.isEqual(endDate, mEndDate)) {
				mStartDate = startDate;
				mEndDate = endDate;
				validateAndSyncState();
			}
		}

		public LocalDate getStartDate() {
			return mStartDate;
		}

		public LocalDate getEndDate() {
			return mEndDate;
		}

		public void setSelectableDateRange(LocalDate minDate, LocalDate maxDate) {
			if (minDate != null && maxDate != null && JodaUtils.daysBetween(minDate, maxDate) <= 1) {
				throw new IllegalArgumentException("Selectable date range must be > 1 day; got " + minDate + " to "
						+ maxDate);
			}

			if (!JodaUtils.isEqual(minDate, mMinSelectableDate) || !JodaUtils.isEqual(maxDate, mMaxSelectableDate)) {
				mMinSelectableDate = minDate;
				mMaxSelectableDate = maxDate;
				validateAndSyncState();
			}
		}

		/**
		 * @return true if the date is selectable (within min/max date ranges)
		 */
		public boolean canSelectDate(LocalDate date) {
			return (mMinSelectableDate == null || !date.isBefore(mMinSelectableDate))
				&& (mMaxSelectableDate == null || !date.isAfter(mMaxSelectableDate));
		}

		/**
		 * @return Whether the YearMonth is within the selectable range (and is therefore viewable)
		 */
		public boolean canDisplayYearMonth(YearMonth yearMonth) {
			Interval interval = yearMonth.toInterval();
			return (mMinSelectableDate == null || !interval.isBefore(mMinSelectableDate.toDateTimeAtStartOfDay()))
					&& (mMaxSelectableDate == null || !interval.isAfter(mMaxSelectableDate.toDateTimeAtStartOfDay()));
		}

		public void setMaxSelectableDateRange(int maxRange) {
			if (maxRange != mMaxSelectableDateRange) {
				mMaxSelectableDateRange = maxRange;
				validateAndSyncState();
			}
		}

		public boolean isSingleDateMode() {
			return (mMaxSelectableDateRange < 1);
		}

		private void validateAndSyncState() {
			// Prevents edge case of endDate from multi touch interactions in SingleDateMode
			if (isSingleDateMode() && mEndDate != null) {
				mEndDate = null;
			}

			// Ensure nothing is set before the min selectable date
			if (mMinSelectableDate != null) {
				if (mDisplayYearMonth.toInterval().isBefore(mMinSelectableDate.toDateTimeAtStartOfDay())) {
					Log.w("Display year month (" + mDisplayYearMonth
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting year month to match min date");
					mDisplayYearMonth = new YearMonth(mMinSelectableDate.getYear(), mMinSelectableDate.getMonthOfYear());
				}

				if (mStartDate != null && mStartDate.isBefore(mMinSelectableDate)) {
					Log.v("Start date (" + mStartDate
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting start date to min date");
					mStartDate = mMinSelectableDate;
				}

				if (mEndDate != null && mEndDate.isBefore(mMinSelectableDate)) {
					Log.w("End date (" + mEndDate
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting end date to one day after start date (" + mStartDate.plusDays(1) + ")");
					mEndDate = mStartDate.plusDays(1);
				}
			}

			// Ensure nothing is set after the max selectable date
			if (mMaxSelectableDate != null) {
				if (mDisplayYearMonth.toInterval().isAfter(mMaxSelectableDate.toDateTimeAtStartOfDay())) {
					Log.w("Display year month (" + mDisplayYearMonth
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); setting year month to match max date");
					mDisplayYearMonth = new YearMonth(mMaxSelectableDate.getYear(), mMaxSelectableDate.getMonthOfYear());
				}

				if (mEndDate != null && mEndDate.isAfter(mMaxSelectableDate)) {
					if (mStartDate != null && mStartDate.isEqual(mMaxSelectableDate)) {
						Log.v("End date (" + mEndDate
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); Start date is at max selectable date; setting end date to max date + 1");
						mEndDate = mMaxSelectableDate.plusDays(1);
					} else {
						Log.v("End date (" + mEndDate
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); setting end date to max date");
						mEndDate = mMaxSelectableDate;
					}
				}

				if (mStartDate != null && mStartDate.isAfter(mMaxSelectableDate)) {
					if (mEndDate != null) {
						Log.w("Start date (" + mStartDate
								+ ") is AFTER max selectable date (" + mMaxSelectableDate
								+ "); setting start date to one day before end date (" + mEndDate.minusDays(1) + ")");
						mStartDate = mEndDate.minusDays(1);
					}
					else {
						Log.w("Start date (" + mStartDate
								+ ") is AFTER max selectable date (" + mMaxSelectableDate
								+ "); setting start date to max date (" + mMaxSelectableDate + ") (no end date)");
						mStartDate = mMaxSelectableDate;
					}
				}
			}

			// Ensure our date range falls within the maximum
			if (mStartDate != null && mEndDate != null
					&& JodaUtils.daysBetween(mStartDate, mEndDate) > mMaxSelectableDateRange) {
				// We need to determine whether to move the START or the END to match the selectable range
				// This is trickier than it sounds; we need to match the expectations of the user.
				if (mLastState.mEndDate == null) {
					// If END added, shift that
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting end date to match range (reason: end was added)");
					mEndDate = mStartDate.plusDays(mMaxSelectableDateRange);
				}
				else if (JodaUtils.isEqual(mLastState.mStartDate, mEndDate)) {
					// If END == last.START, then we've reversed the dates; apply changes to START
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting start date to match range (reason: start/end swapped)");
					mStartDate = mEndDate.minusDays(mMaxSelectableDateRange);
				}
				else if (!JodaUtils.isEqual(mLastState.mEndDate, mEndDate)) {
					// If END has changed, then move that
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting end date to match range (reason: end was changed)");
					mEndDate = mStartDate.plusDays(mMaxSelectableDateRange);
				}
				else {
					// If START has changed, then move that
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting start date to match range (reason: start was changed)");
					mStartDate = mEndDate.minusDays(mMaxSelectableDateRange);
				}
			}

			// Now that we're internally consistent, sync whatever fields may have changed
			if (mLastState == null) {
				mLastState = new CalendarState();
			}

			if (!mLastState.mDisplayYearMonth.equals(mDisplayYearMonth)
					|| !JodaUtils.isEqual(mLastState.mMinSelectableDate, mMinSelectableDate)
					|| !JodaUtils.isEqual(mLastState.mMaxSelectableDate, mMaxSelectableDate)) {
				syncDisplayMonthViews();

				// TODO: Should we always notify, or only when it was changed by user interaction?
				if (mYearMonthDisplayedChangedListener != null) {
					mYearMonthDisplayedChangedListener.onYearMonthDisplayed(mDisplayYearMonth);
				}
			}

			if (!JodaUtils.isEqual(mLastState.mStartDate, mStartDate)
					|| !JodaUtils.isEqual(mLastState.mEndDate, mEndDate)) {
				syncDateSelectionViews();

				// TODO: Should we always notify, or only when it was changed by user interaction?
				if (mDateSelectionChangedListener != null) {
					mDateSelectionChangedListener.onDateSelectionChanged(mStartDate, mEndDate);
				}
			}

			updateLastState();
		}

		protected void updateLastState() {
			if (mLastState == null) {
				mLastState = new CalendarState();
			}

			mLastState.mDisplayYearMonth = mDisplayYearMonth;
			mLastState.mStartDate = mStartDate;
			mLastState.mEndDate = mEndDate;
			mLastState.mMinSelectableDate = mMinSelectableDate;
			mLastState.mMaxSelectableDate = mMaxSelectableDate;
			mLastState.mMaxSelectableDateRange = mMaxSelectableDateRange;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Saved State

	private static class SavedState extends BaseSavedState {
		YearMonth displayMonthYear;
		LocalDate startDate;
		LocalDate endDate;
		LocalDate minSelectableDate;
		LocalDate maxSelectableDate;
		int maxSelectableDateRange;

		private SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			out.writeString(displayMonthYear.toString());
			JodaUtils.writeLocalDate(out, startDate);
			JodaUtils.writeLocalDate(out, endDate);
			JodaUtils.writeLocalDate(out, minSelectableDate);
			JodaUtils.writeLocalDate(out, maxSelectableDate);
			out.writeInt(maxSelectableDateRange);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		private SavedState(Parcel in) {
			super(in);

			displayMonthYear = YearMonth.parse(in.readString());
			startDate = JodaUtils.readLocalDate(in);
			endDate = JodaUtils.readLocalDate(in);
			minSelectableDate = JodaUtils.readLocalDate(in);
			maxSelectableDate = JodaUtils.readLocalDate(in);
			maxSelectableDateRange = in.readInt();
		}
	}

	public ViewGroup getTooltipContainer() {
		return mTooltipPopupWindow.mTooltipContainer;
	}
}
