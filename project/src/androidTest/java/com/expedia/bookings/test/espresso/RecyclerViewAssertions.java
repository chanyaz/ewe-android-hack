package com.expedia.bookings.test.espresso;

import javax.annotation.Nullable;

import org.hamcrest.Matcher;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.util.HumanReadables;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import static org.hamcrest.MatcherAssert.assertThat;

public class RecyclerViewAssertions {

	public static ViewAssertion assertionOnItemAtPosition(final int position,
		final Matcher<View> dataMatcher) {
		return new AssertionOnItemAtPosition(position, dataMatcher);
	}

	private static final class AssertionOnItemAtPosition implements ViewAssertion {

		private final int position;
		private final Matcher<View> matcher;

		private AssertionOnItemAtPosition(int position, Matcher<View> matcher) {
			this.position = position;
			this.matcher = matcher;
		}

		@Override
		public void check(@Nullable View view, @Nullable NoMatchingViewException e) {

			RecyclerView recyclerView = (RecyclerView) view;

			@SuppressWarnings("unchecked")
			ViewHolder viewHolder = recyclerView.findViewHolderForPosition(position);
			if (null == viewHolder) {
				throw new PerformException.Builder().withActionDescription(this.toString())
					.withViewDescription(HumanReadables.describe(view))
					.withCause(new IllegalStateException("No view holder at position: " + position))
					.build();
			}
			View viewAtPosition = viewHolder.itemView;
			if (null == viewAtPosition) {
				throw new PerformException.Builder().withActionDescription(this.toString())
					.withViewDescription(HumanReadables.describe(viewAtPosition))
					.withCause(new IllegalStateException("No view at position: " + position)).build();
			}
			assertThat(viewAtPosition, matcher);
		}
	}
}
