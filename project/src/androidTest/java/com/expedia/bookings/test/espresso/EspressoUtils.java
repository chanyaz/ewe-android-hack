package com.expedia.bookings.test.espresso;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.espresso.ViewActions.getChildCount;
import static com.expedia.bookings.test.espresso.ViewActions.getCount;
import static com.expedia.bookings.test.espresso.ViewActions.getRating;
import static com.expedia.bookings.test.espresso.ViewActions.getStarRating;
import static com.expedia.bookings.test.espresso.ViewActions.getString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class EspressoUtils {

	public static void assertViewWithTextIsDisplayed(String text) {
		onView(withText(text)).check(matches(isDisplayed()));
	}

	public static void assertViewWithTextIsDisplayed(@StringRes int textResId) {
		onView(withText(textResId)).check(matches(isDisplayed()));
	}

	public static void assertViewWithTextIsDisplayed(@IdRes int id, String text) {
		onView(allOf(withId(id), withText(text))).check(matches(isDisplayed()));
	}

	public static void assertViewWithTextIsDisplayed(@IdRes int id, @StringRes int textResId) {
		onView(allOf(withId(id), withText(textResId))).check(matches(isDisplayed()));
	}

	public static void assertViewWithSiblingIsNotDisplayed(@IdRes int viewId, @IdRes int siblingId) {
		onView(allOf(withId(viewId), hasSibling(withId(siblingId)))).check(matches(not(isDisplayed())));
	}

	public static void assertViewWithTextIsNotDisplayed(@IdRes int id, String text) {
		onView(allOf(withId(id), withText(text))).check(matches(not(isDisplayed())));
	}

	public static void assertViewIsDisplayed(@IdRes int id) {
		onView(withId(id)).check(matches(isDisplayed()));
	}

	public static void assertViewIsCompletelyDisplayed(@IdRes int id) {
		onView(withId(id)).check(matches(isCompletelyDisplayed()));
	}

	public static void assertViewIsNotDisplayed(@IdRes int id) {
		onView(withId(id)).check(matches(not(isDisplayed())));
	}

	public static void assertViewIsDisplayed(Matcher<View> viewMatcher) {
		onView(viewMatcher).check(matches(isDisplayed()));
	}

	public static void assertViewIsNotDisplayed(Matcher<View> viewMatcher) {
		onView(viewMatcher).check(matches(not(isDisplayed())));
	}

	public static void assertViewWithContentDescription(ViewInteraction view, String description) {
		view.check(matches(withContentDescription(description)));
	}

	public static void assertViewWithSubstringIsDisplayed(String substring) {
		onView(withText(containsString(substring))).check(matches(isDisplayed()));
	}

	public static void assertViewWithSubstringIsDisplayed(@IdRes int id, String substring) {
		onView(allOf(withId(id), withText(containsString(substring))))
				.check(matches(isDisplayed()));
	}

	public static void assertContains(ViewInteraction view, String str) {
		view.check(matches(withText(containsString(str))));
	}

	public static void assertTextWithChildrenIsDisplayed(@IdRes int id, String text) {
		onView(allOf(withId(id), withChild(withText(text)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(
			matches(isDisplayed()));
	}

	public static void assertViewIsGone(@IdRes int id) {
		onView(withId(id)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
	}

	public static void viewHasDescendantsWithText(@IdRes int id, String text) {
		onView(allOf(withId(id), hasDescendant(withText(text)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()));
	}

	public static String getText(@IdRes int id) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(id)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	// to avoid multiple matches to a view
	public static String getTextWithSibling(@IdRes int id, @IdRes int siblingId) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(allOf(withId(id), hasSibling(withId(siblingId)), isDisplayed())).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static String getListItemValues(DataInteraction row, @IdRes int id) {
		final AtomicReference<String> value = new AtomicReference<String>();
		row.onChildView(withId(id)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static int getListCount(ViewInteraction view) {
		final AtomicReference<Integer> count = new AtomicReference<Integer>();
		view.perform(getCount(count));
		int numberCount = count.get();
		return numberCount;
	}

	public static int getListChildCount(ViewInteraction view) {
		final AtomicReference<Integer> count = new AtomicReference<Integer>();
		view.perform(getChildCount(count));
		int numberCount = count.get();
		return numberCount;
	}

	public static float getRatingValue(ViewInteraction view) {
		final AtomicReference<Float> rating = new AtomicReference<Float>();
		view.perform(getRating(rating));
		float ratingValue = rating.get();
		return ratingValue;
	}

	public static float getStarRatingValue(ViewInteraction view) {
		final AtomicReference<Float> rating = new AtomicReference<Float>();
		view.perform(getStarRating(rating));
		float ratingValue = rating.get();
		return ratingValue;
	}

	public static void assertContainsImageDrawable(@IdRes int viewID, @DrawableRes int imageID) {
		onView(allOf(withId(viewID), isDisplayed())).check(matches(withImageDrawable(imageID)));
	}

	public static void assertContainsImageDrawable(@IdRes int viewID,  @IdRes int parentId, @DrawableRes int imageID) {
		onView(allOf(withId(viewID), withParent(withId(parentId)), isDisplayed())).check(matches(withImageDrawable(imageID)));
	}

	public static <T> void assertIntentFiredToStartActivityWithExtra(Class<?> activityClass, Matcher<String> keyMatcher, Matcher<T> valueMatcher) {
		intended(allOf(
				hasComponent(activityClass.getName()),
				hasExtra(keyMatcher, valueMatcher)
		));
	}

	public static void assertViewWithTextIsDisplayedAtPosition(ViewInteraction viewInteraction, int position, int id, String text) {
		viewInteraction.check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed(), withText(text)))));
	}

	public static void assertViewWithIdIsDisplayedAtPosition(ViewInteraction viewInteraction, int position, int id) {
		viewInteraction.check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed()))));
	}

	public static void assertViewWithIdIsNotDisplayedAtPosition(ViewInteraction viewInteraction, int position, int id) {
		viewInteraction.check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), CoreMatchers.not(isDisplayed())))));
	}

	public static void assertViewHasCompoundDrawable(@IdRes int viewId, @DrawableRes int drawableId) {
		onView(withId(viewId)).check(matches(allOf(withCompoundDrawable(drawableId), isDisplayed())));
	}

	public static void assertViewDoesNotHaveCompoundDrawable(@IdRes int viewId, @DrawableRes int drawableId) {
		onView(withId(viewId)).check(matches(not(withCompoundDrawable(drawableId))));
	}

	public static void waitForViewNotYetInLayoutToDisplay(Matcher<View> matcher, long howLong, TimeUnit timeUnit) {
		Throwable lastException = null;

		long finalTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(howLong, timeUnit);

		while (System.currentTimeMillis() < finalTime) {
			try {
				Espresso.onView(matcher).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
				return;
			}
			catch (Throwable e) {
				lastException = e;
				try {
					Thread.sleep(100);
				}
				catch (Throwable e2) {
					// oh well, keep trying
				}
			}
		}

		if (lastException != null) {
			if (lastException instanceof RuntimeException) {
				throw (RuntimeException) lastException;
			}
			else {
				throw new RuntimeException(lastException);
			}
		}
	}

	public static boolean existsOnScreen(ViewInteraction interaction) {
		try {
			interaction.perform(new ViewAction() {
				@Override
				public Matcher<View> getConstraints() {
					return isCompletelyDisplayed();
				}

				@Override
				public String getDescription() {
					return "check for existence";
				}

				@Override
				public void perform(UiController uiController, View view) {
					// no op, if this is run, then the execution will continue after .perform(...)
				}
			});
			return true;
		}
		catch (AmbiguousViewMatcherException ex) {
			// if there's any interaction later with the same matcher, that'll fail anyway
			return true; // we found more than one
		}
		catch (NoMatchingViewException ex) {
			return false;
		}
		catch (Exception ex) {
			return false;
		}
	}
}
