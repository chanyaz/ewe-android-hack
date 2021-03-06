package com.expedia.bookings.test.espresso;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.graphics.Rect;
import android.support.test.espresso.PerformException;
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
import android.support.test.espresso.util.HumanReadables;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.StarRatingBar;
import com.mobiata.android.Log;
import com.mobiata.android.widget.CalendarDatePicker;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public final class ViewActions {

	private ViewActions() {
		// ignore
	}

	public static ViewAction swipeDown() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
			GeneralLocation.BOTTOM_CENTER, Press.FINGER);
	}

	public static ViewAction swipeUp() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER,
			GeneralLocation.TOP_CENTER, Press.FINGER);
	}

	public static ViewAction slowSwipeUp() {
		return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
			GeneralLocation.TOP_CENTER, Press.FINGER);
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

	public static ViewAction setSeekBarTo(final int progress) {
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
				seekBar.setProgress(progress);
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

	public static ViewAction getStarRating(final AtomicReference<Float> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(StarRatingBar.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				value.set(((StarRatingBar) view).getRating());
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

	//View Action to get the Up botton in the support v7 toolbar

	public static ViewAction getChildViewButton(final int index) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(ViewGroup.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				View childView = ((Toolbar) view).getChildAt(index);
				childView.performClick();
				uiController.loopMainThreadUntilIdle();
			}

			@Override
			public String getDescription() {
				return "Click on child view";
			}
		};
	}

	// View action for accessing text nested inside two linear layouts

	public final static class NestedTextView implements ViewAction {
		private final int mUpperLayoutIndex;
		private final AtomicReference<String> mValue;

		public NestedTextView(final int upperIndex, final AtomicReference<String> value) {
			mUpperLayoutIndex = upperIndex;
			mValue = value;
		}
		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(isAssignableFrom(ViewGroup.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			View childView = ((ViewGroup) view).getChildAt(mUpperLayoutIndex);
			TextView textView = (TextView) childView.findViewById(R.id.traveler_empty_text_view);
			mValue.set(textView.getText().toString());
		}

		@Override
		public String getDescription() {
			return "Get the empty travelers container text on checkout upperIndex=" + mUpperLayoutIndex;
		}
	}

	// View action to get the name match warning's sibling text view

	public static ViewAction getNameMatchWarningView(final AtomicReference<String> value) {
		return new NestedTextView(2, value);
	}

	// View action to get empty traveler container on checkout

	public static ViewAction getEmptyTravelerViewLayout(final int index, final AtomicReference<String> value) {
		return new NestedTextView(index, value);
	}

	// View action to get traveler container with info entered on checkout

	public static ViewAction getPopulatedTravelerViewLayout(final int index, final AtomicReference<String> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(ViewGroup.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				View travlerAtIndexView = ((ViewGroup) view).getChildAt(index);
				TextView textView = (TextView) travlerAtIndexView.findViewById(R.id.display_full_name);
				value.set(textView.getText().toString());
			}

			@Override
			public String getDescription() {
				return "Get the empty travelers container text on checkout";
			}
		};
	}

	//View Action to get the count of list view items

	public static ViewAction getCount(final AtomicReference<Integer> count) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.anyOf(isAssignableFrom(AdapterView.class), isAssignableFrom(RecyclerView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				int children = 0;
				if (view instanceof AdapterView) {
					children = ((AdapterView) view).getAdapter().getCount();
				}
				else if (view instanceof RecyclerView) {
					children = ((RecyclerView) view).getAdapter().getItemCount();
				}
				count.set(children);
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
				return Matchers.allOf(isAssignableFrom(ViewGroup.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				count.set(((ViewGroup) view).getChildCount());
			}

			@Override
			public String getDescription() {
				return "Get list items per screen height count";
			}
		};
	}


	public static ViewAction clickETPRoomItem(final int position) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(AdapterView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				AdapterView av = (AdapterView) view;
				AdapterView.OnItemClickListener listener = av.getOnItemClickListener();
				if (listener == null) {
					throw new PerformException.Builder()
						.withActionDescription("AdapterView OnItemClickListener was null")
						.withViewDescription(HumanReadables.describe(view))
						.build();
				}
				listener.onItemClick(av, null, position, position);
			}

			@Override
			public String getDescription() {
				return "Click etp room item";
			}
		};
	}

	//View Action to type multibyte characters
	public static ViewAction setText(final String multiByte) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(EditText.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				((EditText) view).setText(multiByte);
				uiController.loopMainThreadUntilIdle();
			}

			@Override
			public String getDescription() {
				return "Type multi byte character";
			}
		};
	}


	public static ViewAction waitFor(final Matcher<View> what, final long howLong, final TimeUnit timeUnit) {
		return new ViewAction() {
			private static final int SLEEP_UI_MS = 100;
			private final long timeout = TimeUnit.MILLISECONDS.convert(howLong, timeUnit);
			private final long timeoutSeconds = TimeUnit.SECONDS.convert(howLong, timeUnit);

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(View.class));
			}

			@Override
			public String getDescription() {
				return String.format("Waiting for view to match given matcher, max wait time is: %d seconds",
					timeoutSeconds);
			}

			@Override
			public void perform(final UiController uiController, final View view) {
				uiController.loopMainThreadUntilIdle();
				uiController.loopMainThreadForAtLeast(SLEEP_UI_MS);

				final long endTime = System.currentTimeMillis() + timeout;
				do {
					Log.v("waitFor", "Waiting for " + SLEEP_UI_MS + "ms");
					uiController.loopMainThreadForAtLeast(SLEEP_UI_MS);

					if (what.matches(view)) {
						Log.v("waitFor", "Matched");
						return;
					}
				}
				while (System.currentTimeMillis() <= endTime);

				throw new PerformException.Builder()
					.withActionDescription(getDescription())
					.withViewDescription(HumanReadables.describe(view))
					.build();
			}
		};
	}

	public static ViewAction waitForViewToDisplay() {
		return waitFor(isDisplayed(), 10, TimeUnit.SECONDS);
	}

	// View action to set visibility of a view

	public static ViewAction setVisibility(final int visibility) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(View.class));
			}

			@Override
			public String getDescription() {
				return "Setting view visibility to = " + visibility;
			}

			@Override
			public void perform(UiController uiController, View view) {
				view.setVisibility(visibility);
			}
		};
	}

	public static ViewAction customScroll() {
		return customScroll(90);
	}

	public static ViewAction customScroll(final int minimumAreaPercentageDisplayedRequired) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(
					ViewMatchers.isDescendantOfA(
						Matchers.anyOf(
							ViewMatchers.isAssignableFrom(ScrollView.class),
							ViewMatchers.isAssignableFrom(HorizontalScrollView.class)
						)
					)
				);
			}

			@Override
			public String getDescription() {
				return "Scroll to";
			}

			@Override
			public void perform(UiController uiController, View view) {
				if (!ViewMatchers.isDisplayingAtLeast(minimumAreaPercentageDisplayedRequired).matches(view)) {
					Rect rect = new Rect();
					view.getDrawingRect(rect);
					if (!view.requestRectangleOnScreen(rect, true)) {
						Log.i("Custom-Scroll", "Scrolling to view was requested, but none of the parents scrolled.");
					}

					uiController.loopMainThreadUntilIdle();
					uiController.loopMainThreadForAtLeast(100);
				}
			}
		};
	}
}
