package com.expedia.bookings.test.ui.espresso;

import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.PrecisionDescriber;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Swiper;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.widget.CalendarDatePicker;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

public final class ViewActions {

	private ViewActions() {
		// ignore
	}

	public static ViewAction swipeUp() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER,
			GeneralLocation.TOP_CENTER, Press.FINGER);
	}

	public static ViewAction slowSwipeUp() {
		return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
			GeneralLocation.TOP_CENTER, Press.FINGER);
	}

	public static ViewAction swipeDown() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER,
			GeneralLocation.BOTTOM_CENTER, Press.FINGER);
	}

	public static ViewAction swipeRight() {
		return new LaxSwipeAction(Swipe.FAST,
			GeneralLocation.CENTER_RIGHT, Press.FINGER);
	}

	public static class LaxSwipeAction implements ViewAction {
		private GeneralSwipeAction mAction;

		public LaxSwipeAction(Swiper swiper, CoordinatesProvider endCoordinatesProvider, PrecisionDescriber precisionDescriber) {
			CoordinatesProvider startCoordinates = new CoordinatesProvider() {
				@Override
				public float[] calculateCoordinates(View view) {
					View v = view.findViewById(R.id.touch_target);
					final int[] screenPos = new int[2];
					v.getLocationOnScreen(screenPos);
					final float startX = screenPos[0];
					final float startY = screenPos[1];
					return new float[] {startX, startY};
				}
			};
			mAction = new GeneralSwipeAction(swiper, startCoordinates, endCoordinatesProvider, precisionDescriber);
		}

		@Override
		public Matcher<View> getConstraints() {
			return ViewMatchers.isDisplayingAtLeast(70);
		}

		@Override
		public void perform(UiController uiController, View view) {
			mAction.perform(uiController, view);
		}

		@Override
		public String getDescription() {
			return mAction.getDescription();
		}
	}

	// View Action for Calender Utils

	public static ViewAction clickDate(LocalDate start) {
		return new CalendarPickerClickDatesAction(start);
	}

	public static ViewAction clickDates(LocalDate start, LocalDate end) {
		return new CalendarPickerClickDatesAction(start, end);
	}

	public final static class CalendarPickerClickDatesAction implements ViewAction {
		private LocalDate mStartDate = null;
		private LocalDate mEndDate = null;

		public CalendarPickerClickDatesAction(LocalDate start) {
			mStartDate = start;
		}

		public CalendarPickerClickDatesAction(LocalDate start, LocalDate end) {
			mStartDate = start;
			mEndDate = end;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), isAssignableFrom(CalendarDatePicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			CalendarDatePicker cp = (CalendarDatePicker) view;
			//since updateStartDate has months [0-11] we need to pass mStartDate.getMonthOfYear()-1
			cp.updateStartDate(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth());
			if (mEndDate != null) {
				cp.updateEndDate(mEndDate.getYear(), mEndDate.getMonthOfYear() - 1, mEndDate.getDayOfMonth());
			}
		}

		@Override
		public String getDescription() {
			return "selects dates";
		}
	}

	// View Action for manipulating a seek bar

	public static ViewAction setSeekbarTo(final int progress) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(SeekBar.class));
			}

			@Override
			public String getDescription() {
				return "set Seekbar progress";
			}

			@Override
			public void perform(UiController uiController, View view) {
				SeekBar seekBar = (SeekBar) view;
				((SeekBar) view).setProgress(progress);
			}
		};
	}

	//View Action to get the values for a view

	public static ViewAction getString(final AtomicReference<String> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(TextView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {

				value.set(((TextView) view).getText().toString());
			}

			@Override
			public String getDescription() {
				return "get text from Text View";
			}
		};
	}

	//View Action to get the rating for RatingBar

	public static ViewAction getRating(final AtomicReference<Float> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(RatingBar.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				value.set(((RatingBar) view).getRating());
			}

			@Override
			public String getDescription() {
				return "get ratings from RatingBar widget";
			}
		};
	}

	//View Action to get the airline checkbox text and count in Tablet flight results filter

	public static ViewAction getChildViewText(final int index, final AtomicReference<String> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(ViewGroup.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				if (index == -1) {
					value.set(Integer.toString(((LinearLayout) view).getChildCount()));
				}
				else {
					View childView = ((LinearLayout) view).getChildAt(index);
					value.set(((CheckBox) childView.findViewById(R.id.filter_refinement_checkbox)).getText().toString());
				}
			}

			@Override
			public String getDescription() {
				return "get the  airline checkbox text";
			}
		};
	}

	//View Action to get the count of list view items

	public static ViewAction getCount(final AtomicReference<Integer> count) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(AdapterView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				count.set(((AdapterView) view).getCount());
			}

			@Override
			public String getDescription() {
				return "Get total list items count";
			}
		};
	}

	//View Action to get the child count of list view items

	public static ViewAction getChildCount(final AtomicReference<Integer> count) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(AdapterView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				count.set(((AdapterView) view).getChildCount());
			}

			@Override
			public String getDescription() {
				return "Get list items per screen height count";
			}
		};
	}
}


