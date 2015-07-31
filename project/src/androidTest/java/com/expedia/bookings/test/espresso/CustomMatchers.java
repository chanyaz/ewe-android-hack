package com.expedia.bookings.test.espresso;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.data.Property;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by dmadan on 5/13/14.
 */
public class CustomMatchers {
	public static Matcher<View> withCompoundDrawable(final int resId) {
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

	public static Matcher<View> withImageDrawable(final int resourceId) {
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

	@SuppressLint("NewApi")
	private static boolean sameBitmap(Context context, Drawable drawable, int resId) {
		Drawable otherDrawable = context.getResources().getDrawable(resId);
		if (drawable == null || otherDrawable == null) {
			return false;
		}
		if (drawable instanceof StateListDrawable && otherDrawable instanceof StateListDrawable) {
			drawable = drawable.getCurrent();
			otherDrawable = otherDrawable.getCurrent();
		}
		if (drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			Bitmap otherBitmap = ((BitmapDrawable) otherDrawable).getBitmap();
			return bitmap.sameAs(otherBitmap);
		}
		return false;
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
		return new BoundedMatcher<View, RadioGroup>(RadioGroup.class) {
			@Override
			public boolean matchesSafely(RadioGroup view) {
				for (int i = 0; i < view.getChildCount(); i++) {
					RadioButton button = (RadioButton) view.getChildAt(i);
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
		return new BoundedMatcher<View, RadioGroup>(RadioGroup.class) {
			@Override
			public boolean matchesSafely(RadioGroup view) {
				RadioButton currentButton = (RadioButton) view.getChildAt(index);
				String text = currentButton.getText().toString();
				return text.contains(weekDay) && text.contains(dayOfMonth);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The offer selection button must have correct date format");
			}
		};
	}

	public static Matcher<View> withAtleastChildCount(final int leastCount) {
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
				m.find();
				BigDecimal foundPrice = new BigDecimal(m.group());
				if (foundPrice.compareTo(expectedPrice) == 0) {
					return true;
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The total price must match");
			}
		};
	}
}
