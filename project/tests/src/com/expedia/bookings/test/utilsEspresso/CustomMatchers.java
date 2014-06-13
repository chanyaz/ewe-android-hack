package com.expedia.bookings.test.utilsEspresso;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;

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
}
