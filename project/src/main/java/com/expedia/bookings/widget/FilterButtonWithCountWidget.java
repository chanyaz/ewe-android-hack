package com.expedia.bookings.widget;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FilterButtonWithCountWidget extends LinearLayout {

	public FilterButtonWithCountWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		inflate(context, R.layout.widget_filter_button_with_count, this);
	}

	@InjectView(R.id.sort_filter_button)
	LinearLayout buttonContainer;

	@InjectView(R.id.filter_number_text)
	TextView filterNumber;

	@InjectView(R.id.filter_icon)
	View filterIcon;

	@InjectView(R.id.filter_text)
	TextView filterText;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		filterNumber.setVisibility(GONE);
	}

	public void showNumberOfFilters(int number) {
		filterNumber.setText(String.valueOf(number));
		boolean hasCheckedFilters = number > 0;
		filterNumber.setVisibility(hasCheckedFilters ? VISIBLE : GONE);
		filterIcon.setVisibility(hasCheckedFilters ? GONE : VISIBLE);
	}

	public void setFilterText(String text) {
		filterText.setText(text);
	}

	public void setButtonBackgroundColor(@ColorRes int backgroundColorId) {
		buttonContainer.setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorId));
	}

	public void setTextAndFilterIconColor(@ColorRes int color) {
		int textAndFilterIconColor = ContextCompat.getColor(getContext(), color);
		filterText.setTextColor(textAndFilterIconColor);
		filterNumber.setTextColor(textAndFilterIconColor);
	}

	private int recyclerViewScrolledDistance = 0;

	public RecyclerView.OnScrollListener hideShowOnRecyclerViewScrollListener() {
		final LinearLayout self = this;

		return new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);

				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					final int heightOfButton = self.getHeight();
					float yTranslation = 0f;
					if (recyclerViewScrolledDistance > heightOfButton / 2) {
						// hide button
						yTranslation = heightOfButton;
					}
					self.animate().translationY(yTranslation).setInterpolator(new DecelerateInterpolator()).start();
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				if (dy > 0) {
					recyclerViewScrolledDistance = Math.min(self.getHeight(), recyclerViewScrolledDistance + dy);
				}
				else {
					recyclerViewScrolledDistance = Math.max(0, recyclerViewScrolledDistance + dy);
				}
			}
		};
	}
}
