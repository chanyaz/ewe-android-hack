package com.expedia.bookings.test.espresso;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.widget.accessibility.AccessibleEditText;
import com.expedia.bookings.widget.flights.FlightListAdapter;
import com.mobiata.flightlib.data.Airport;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.equalTo;

public class CustomMatchers {
	public static Matcher<View> withCompoundDrawable(final @DrawableRes int resId) {
		return new BoundedMatcher<View, TextView>(TextView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("has compound drawable resource " + resId);
			}

			@Override
			public boolean matchesSafely(TextView textView) {
				for (Drawable drawable : textView.getCompoundDrawables()) {
					if (sameBitmap(textView.getContext(), drawable, resId)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	public static Matcher<View> withTextColor(final String hexColor) {
		return new BoundedMatcher<View, TextView>(TextView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("TextView has this hex color -> " + hexColor);
			}

			@Override
			public boolean matchesSafely(TextView textView) {
				return Color.parseColor(hexColor) == textView.getCurrentTextColor();
			}
		};
	}

	public static Matcher<View> withImageDrawable(final @DrawableRes int resourceId) {
		return new BoundedMatcher<View, ImageView>(ImageView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("has image drawable resource " + resourceId);
			}

			@Override
			public boolean matchesSafely(ImageView imageView) {
				return sameBitmap(imageView.getContext(), imageView.getDrawable(), resourceId);
			}
		};
	}

	public static boolean sameBitmap(Context context, Drawable drawable, @DrawableRes int resId) {
		Drawable otherDrawable = ContextCompat.getDrawable(context, resId);
		if (drawable == null || otherDrawable == null) {
			return false;
		}
		if (drawable instanceof StateListDrawable) {
			drawable = drawable.getCurrent();

			if (otherDrawable instanceof StateListDrawable) {
				otherDrawable = otherDrawable.getCurrent();
			}
		}
		if (drawable instanceof StateListDrawable && otherDrawable instanceof BitmapDrawable) {
			if (drawable.getCurrent() == null) {
				return false;
			}
			Bitmap bitmap = ((BitmapDrawable) drawable.getCurrent()).getBitmap();
			Bitmap otherBitmap = ((BitmapDrawable) otherDrawable).getBitmap();
			return bitmap.sameAs(otherBitmap);
		}
		if (drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			Bitmap otherBitmap = ((BitmapDrawable) otherDrawable).getBitmap();
			return bitmap.sameAs(otherBitmap);
		}
		if (drawable instanceof VectorDrawable && otherDrawable instanceof VectorDrawable) {
			VectorDrawable bitmap = ((VectorDrawable) drawable);
			VectorDrawable otherBitmap = ((VectorDrawable) otherDrawable);
			return bitmap.getConstantState().equals(otherBitmap.getConstantState());
		}
		if (drawable instanceof VectorDrawableCompat) {
			Bitmap bitmap = getBitmapFromVectorDrawable(drawable);
			Bitmap otherBitmap = getBitmapFromVectorDrawable(otherDrawable);
			return bitmap.sameAs(otherBitmap);
		}
		return false;
	}

	public static Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			drawable = (DrawableCompat.wrap(drawable)).mutate();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
			drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static Matcher<View> withHint(final String str) {
		return new TypeSafeMatcher<View>() {

			@Override
			public boolean matchesSafely(View view) {
				if (!(view instanceof EditText)) {
					return false;
				}
				String hint = ((EditText) view).getHint().toString();
				return str.equals(hint);
			}

			@Override
			public void describeTo(Description description) {
			}
		};
	}

	public static Matcher<View> withRating(final float rating) {
		return new BoundedMatcher<View, RatingBar>(RatingBar.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("has rating " + rating);
			}

			@Override
			public boolean matchesSafely(RatingBar ratingBar) {
				return ratingBar.getRating() == rating;
			}
		};
	}

	public static Matcher<View> isEmpty() {
		return new BoundedMatcher<View, TextView>(TextView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("empty");
			}

			@Override
			public boolean matchesSafely(TextView textview) {
				return TextUtils.isEmpty(textview.getText());
			}
		};
	}

	public static Matcher<View> listLengthGreaterThan(final int count) {
		return new BoundedMatcher<View, AdapterView>(AdapterView.class) {
			@Override
			protected boolean matchesSafely(AdapterView view) {
				return (view.getCount() > count);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("list count greater than " + count);
			}
		};
	}

	public static Matcher<Object> withHotelName(String expectedText) {
		return withHotelName(equalTo(expectedText));
	}

	public static Matcher<Object> withHotelName(final Matcher<String> textMatcher) {
		return new BoundedMatcher<Object, Property>(Property.class) {
			@Override
			public boolean matchesSafely(Property property) {
				return textMatcher.matches(property.getName());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with hotel name : ");
				textMatcher.describeTo(description);
			}
		};
	}

	public static Matcher<View> withChildCount(final int expectedChildCount) {
		return new BoundedMatcher<View, ViewGroup>(ViewGroup.class) {
			@Override
			public boolean matchesSafely(ViewGroup view) {
				return expectedChildCount == view.getChildCount();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The radio group must have " + expectedChildCount + " options");
			}
		};
	}

	public static Matcher<View> withOneEnabled() {
		return new BoundedMatcher<View, LinearLayout>(LinearLayout.class) {
			@Override
			public boolean matchesSafely(LinearLayout view) {
				for (int i = 0; i < view.getChildCount(); i++) {
					Button button = (Button) view.getChildAt(i);
					if (button.isEnabled()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Atleast one radio button must be clickable");
			}
		};
	}

	public static Matcher<View> withDateCaptionAtIndex(final int index, final String weekDay, final String dayOfMonth) {
		return new BoundedMatcher<View, LinearLayout>(LinearLayout.class) {
			@Override
			public boolean matchesSafely(LinearLayout view) {
				Button currentButton = (Button) view.getChildAt(index);
				String text = currentButton.getText().toString();
				return text.contains(weekDay) && text.contains(dayOfMonth);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The offer selection button must have correct date format");
			}
		};
	}

	public static Matcher<View> withAtLeastChildCount(final int leastCount) {
		return new BoundedMatcher<View, ViewGroup>(ViewGroup.class) {
			@Override
			public boolean matchesSafely(ViewGroup view) {
				return view.getChildCount() >= leastCount;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The view must have some children");
			}
		};
	}

	public static Matcher<View> withTotalPrice(final BigDecimal expectedPrice) {
		return new BoundedMatcher<View, Button>(Button.class) {
			@Override
			public boolean matchesSafely(Button view) {
				String displayedText = view.getText().toString().replace(",", "");
				Pattern p = Pattern.compile("([\\d.]+)");
				java.util.regex.Matcher m = p.matcher(displayedText);
				if (m.find()) {
					BigDecimal foundPrice = new BigDecimal(m.group());
					return foundPrice.compareTo(expectedPrice) == 0;
				}
				else {
					return false;
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The total price must match");
			}
		};
	}

	public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
		checkNotNull(itemMatcher);
		return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("has item at position " + position + ": ");
				itemMatcher.describeTo(description);
			}

			@Override
			protected boolean matchesSafely(final RecyclerView view) {
				RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
				if (viewHolder == null) {
					// has no item on such position
					return false;
				}
				return itemMatcher.matches(viewHolder.itemView);
			}
		};
	}

	public static Matcher<Uri> hasPhoneNumber(final String phoneNumber) {
		return new BoundedMatcher<Uri, Uri>(Uri.class) {
			@Override
			protected boolean matchesSafely(Uri intentUri) {
				return intentUri.toString().replaceAll("[^0-9]", "").equals(phoneNumber);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has phone number: " + phoneNumber);
			}

		};
	}

	public static Matcher<View> withContentDescription(final String contentDescription) {
		return new BoundedMatcher<View, View>(View.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("View has this content description -> " + contentDescription);
			}

			@Override
			protected boolean matchesSafely(View item) {
				return item.getContentDescription().toString().equals(contentDescription);
			}
		};
	}

	public static Matcher<View> withNavigationContentDescription(final String contentDescription) {
		return new BoundedMatcher<View, Toolbar>(Toolbar.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("Toolbar navigation has this content description -> " + contentDescription);
			}

			@Override
			public boolean matchesSafely(Toolbar toolbar) {
				return toolbar.getNavigationContentDescription().toString().equals(contentDescription);
			}
		};
	}

	public static Matcher<View> withInfoText(final String infoText) {
		return new BoundedMatcher<View, View>(View.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("View has this info text -> " + infoText);
			}

			@Override
			protected boolean matchesSafely(View item) {
				AccessibilityNodeInfo accessibilityNodeInfo = item.createAccessibilityNodeInfo();
				return accessibilityNodeInfo.getText().equals(infoText);
			}
		};
	}

	public static Matcher<View> atFlightListPosition(final int position, final Matcher<View> itemMatcher) {
		return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("Flight list View holder at position " + position + ", has content description = " + itemMatcher);
				itemMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(final RecyclerView recyclerView) {
				FlightListAdapter.FlightViewHolder viewHolder = (FlightListAdapter.FlightViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
				if (viewHolder == null) {
					// has no item on such position
					return false;
				}
				return itemMatcher.matches(viewHolder.getFlightCell().getCardView());
			}
		};
	}

	public static <T> Matcher<T> airportDropDownEntryWithAirportCode(final String airportCode) {
		return new BaseMatcher<T>() {
			@Override
			public boolean matches(Object item) {
				try {
					Method getAirportMethod = item.getClass().getMethod("getAirport");
					getAirportMethod.setAccessible(true);
					Airport airport = (Airport) getAirportMethod.invoke(item);
					return airportCode.equals(airport.mAirportCode);
				}
				catch (Exception e) {
					return false;
				}
			}

			@Override
			public void describeTo(Description description) {
			}

		};
	}

	public static Matcher<View> hasTextInputLayoutErrorText(final String expectedErrorText) {
		return new TypeSafeMatcher<View>() {

			@Override
			public boolean matchesSafely(View view) {
				if (!(view instanceof TextInputLayout)) {
					return false;
				}
				CharSequence error = ((TextInputLayout) view).getError();

				if (error == null) {
					return expectedErrorText.equalsIgnoreCase("");
				}

				String hint = error.toString();

				return expectedErrorText.equals(hint);
			}

			@Override
			public void describeTo(Description description) {
			}
		};
	}

	public static Matcher<View> hasTextInputLayoutAccesibilityEditText(final String expectedErrorText) {
		return new TypeSafeMatcher<View>() {

			@Override
			public boolean matchesSafely(View view) {
				if (!(view instanceof AccessibleEditText)) {
					return false;
				}
				CharSequence error = ((AccessibleEditText) view).getErrorMessage();
				if (error == null) {
					return expectedErrorText.equalsIgnoreCase("");
				}
				String hint = error.toString();
				return expectedErrorText.equals(hint);
			}

			@Override
			public void describeTo(Description description) {
			}
		};
	}

	public static Matcher<View> withRecyclerViewSize(final int size) {
		return new TypeSafeMatcher<View>() {
			@Override
			protected boolean matchesSafely(final View item) {
				final int actualListSize = ((RecyclerView) item).getAdapter().getItemCount();
				return actualListSize == size;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("RecyclerView should have " + size + " items");
			}
		};
	}

	public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
		return new TypeSafeMatcher<View>() {
			int currentIndex = 0;

			@Override
			public void describeTo(Description description) {
				description.appendText("with index: ");
				description.appendValue(index);
				matcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(View view) {
				return matcher.matches(view) && currentIndex++ == index;
			}
		};
	}
}
