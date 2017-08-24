package com.expedia.bookings.widget;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.squareup.phrase.Phrase;

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

		StringBuilder contDescBuilder = new StringBuilder();
		if (hasCheckedFilters) {
			String announcementString = Phrase
				.from(getContext().getResources().getQuantityString(R.plurals.number_results_announcement_text_TEMPLATE, number))
				.put("number", number)
				.format().toString();
			contDescBuilder.append(announcementString);
			contDescBuilder.append(". ");
		}
		contDescBuilder.append(filterText.getText());
		AccessibilityUtil.appendRoleContDesc(buttonContainer, contDescBuilder.toString(), R.string.accessibility_cont_desc_role_button);
	}

	public void setFilterText(String text) {
		filterText.setText(text);
	}

	public void setButtonBackgroundColor(@ColorRes int backgroundColorId) {
		buttonContainer.setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorId));
	}

	public void setBackground(int resource) {
		buttonContainer.setBackgroundResource(resource);
	}

	public void setTextAndFilterIconColor(@ColorRes int color) {
		int textAndFilterIconColor = ContextCompat.getColor(getContext(), color);
		filterText.setTextColor(textAndFilterIconColor);
		filterNumber.setTextColor(textAndFilterIconColor);
		((ImageView) filterIcon).setColorFilter(textAndFilterIconColor);
	}

	public RecyclerView.OnScrollListener hideShowOnRecyclerViewScrollListener() {
		final LinearLayout self = this;

		return new RecyclerView.OnScrollListener() {
			private int scrolledDistance = 0;
			private final int heightOfButton = (int) getResources().getDimension(R.dimen.flight_sort_filter_container_height);

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					if (scrolledDistance > heightOfButton / 2) {
						self.animate().translationY(heightOfButton)
							.setInterpolator(new DecelerateInterpolator()).start();
					}
					else {
						self.animate().translationY(0f).setInterpolator(new DecelerateInterpolator()).start();
					}
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				if (dy > 0) {
					scrolledDistance = Math.min(heightOfButton, scrolledDistance + dy);
				}
				else {
					scrolledDistance = Math.max(0, scrolledDistance + dy);
				}

				if (scrolledDistance > 0) {
					self.setTranslationY(Math.min(heightOfButton, scrolledDistance));
				}
				else {
					self.setTranslationY(Math.min(scrolledDistance, 0));
				}
			}
		};
	}
}
