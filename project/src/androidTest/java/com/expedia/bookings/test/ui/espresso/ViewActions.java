package com.expedia.bookings.test.ui.espresso;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.widget.CalendarDatePicker;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static junit.framework.Assert.assertTrue;

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
		private final int mLowerLayoutIndex;
		private final AtomicReference<String> mValue;

		public NestedTextView(final int upperIndex, final int lowerIndex, final AtomicReference<String> value) {
			mUpperLayoutIndex = upperIndex;
			mLowerLayoutIndex = lowerIndex;
			mValue = value;
		}
			@SuppressWarnings("unchecked")
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(ViewGroup.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				View childView = ((LinearLayout) view).getChildAt(mUpperLayoutIndex);
				View textView = ((LinearLayout) childView).getChildAt(mLowerLayoutIndex);
				mValue.set(((TextView) textView).getText().toString());
			}

			@Override
			public String getDescription() {
				return "Get the empty travelers container text on checkout";
			}
	}

	// View action to get the name match warning's sibling text view

	public static ViewAction getNameMatchWarningView(final AtomicReference<String> value) {
		return new NestedTextView(2, 0, value);
	}

	// View action to get empty traveler container on checkout

	public static ViewAction getEmptyTravelerViewLayout(final int index, final AtomicReference<String> value) {
		return new NestedTextView(index, 1, value);
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
				View upperChildView = ((LinearLayout) view).getChildAt(index);
				View lowerChildView = ((LinearLayout) upperChildView).getChildAt(1);
				View textView = ((LinearLayout) lowerChildView).getChildAt(0);
				value.set(((TextView) textView).getText().toString());
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
					children = ((AdapterView) view).getCount();
				}
				else if (view instanceof RecyclerView) {
					children = ((RecyclerView) view).getChildCount();
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


	public static ViewAction waitFor(final long timeOutSource, final TimeUnit timeUnit, final Class className) {
		return new ViewAction() {
			final static int WAIT_ON_UI_THREAD = 50;
			final long mWaitTimeMillis = TimeUnit.MILLISECONDS.convert(timeOutSource, timeUnit);

			@Override
			public Matcher<View> getConstraints() {
				return isAssignableFrom(className);
			}

			@Override
			public String getDescription() {
				return "Wait for the view to disappear, max wait time is : " + TimeUnit.SECONDS
					.convert(timeOutSource, TimeUnit.SECONDS)
					+ " seconds.";
			}

			@Override
			public void perform(final UiController uiController, final View myView) {
				uiController.loopMainThreadUntilIdle();
				long startTime = System.currentTimeMillis();
				final long endTime = startTime + mWaitTimeMillis;
				do {
					if (myView.getVisibility() == View.GONE || myView.getVisibility() == View.INVISIBLE) {
						// we are done waiting
						return;
					}
					// otherwise idle wait for defined time
					uiController.loopMainThreadForAtLeast(WAIT_ON_UI_THREAD);
					startTime = System.currentTimeMillis();
				}
				while (startTime <= endTime);
				//	Last try,if fail throw the exception
				assertTrue("The View must disappear after " + TimeUnit.SECONDS.convert(timeOutSource, TimeUnit.SECONDS)
						+ " seconds",
					myView.getVisibility() == View.GONE || myView.getVisibility() == View.INVISIBLE);
			}
		};

	}


	public static ViewAction getDataFromTheTileView(
		final AtomicReference<HashMap<String, String>> srpTileDataContainerHolder) {
		return new ViewAction() {
			final static String ACTIVITY_TITLE = "Activity Title";
			final static String ACTIVITY_CATEGORIES = "Activity Categories";
			final static String ACTIVITY_PRICE = "Activity Price";
			final static String ACTIVITY_PRICE_TICKET_TYPE = "Activity Price Ticket Type";

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(TextView.class));
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void perform(UiController uiController, View view) {
				HashMap<String, String> dataContainer = srpTileDataContainerHolder.get();
				TextView activityTitle = (TextView) view.findViewById(R.id.activity_title);
				dataContainer.put(ACTIVITY_TITLE, activityTitle.getText().toString());
				TextView activityCategory = (TextView) view.findViewById(R.id.activity_categories);
				dataContainer.put(ACTIVITY_CATEGORIES, activityCategory.getText().toString());
				TextView activityPrice = (TextView) view.findViewById(R.id.activity_price);
				dataContainer.put(ACTIVITY_PRICE, activityPrice.getText().toString());
				TextView activityPriceTicketType = (TextView) view.findViewById(R.id.activity_from_price_ticket_type);
				dataContainer.put(ACTIVITY_PRICE_TICKET_TYPE, activityPriceTicketType.getText().toString());
				srpTileDataContainerHolder.set(dataContainer);
			}
		};
	}


	public static ViewAction validateDateButtonAtIndex(final int index, final String weekDay, final String dayOfMonth) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return isAssignableFrom(RadioGroup.class);
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void perform(UiController uiController, View view) {
				RadioGroup group = (RadioGroup) view;
				RadioButton currentButton = (RadioButton) group.getChildAt(index);
				String text = currentButton.getText().toString();
				assertTrue("The button at index" + index + " must have the weekday as " + weekDay,
					text.startsWith(weekDay));
				assertTrue("The button at index" + index + " must have the day of the month as " + dayOfMonth,
					text.endsWith(
						dayOfMonth));
			}
		};
	}

	public static ViewAction isEnabled(final AtomicReference<Boolean> enabledContainer) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return isAssignableFrom(RadioButton.class);
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void perform(UiController uiController, View view) {
				RadioButton button = (RadioButton) view;
				enabledContainer.set(button.isEnabled());
			}
		};
	}

}


