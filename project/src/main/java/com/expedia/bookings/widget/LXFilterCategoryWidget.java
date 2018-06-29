package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.otto.Events;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.BindView;

public class LXFilterCategoryWidget extends LinearLayout implements View.OnClickListener {
	private LXCategoryMetadata category;
	private String categoryKey;

	public LXFilterCategoryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@BindView(R2.id.category)
	TextView categoryTitle;

	@BindView(R2.id.category_check_box)
	CheckBox categoryCheckBox;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
		Events.register(this);
		setOnClickListener(this);
	}

	public void bind(LXCategoryMetadata category, String categoryKey) {
		this.category = category;
		this.categoryKey = categoryKey;
		categoryTitle.setText(category.displayValue);
		categoryCheckBox.setChecked(category.checked);
		setContentDescription();
		categoryTitle.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
	}

	@Override
	public void onClick(View v) {
		category.checked = !category.checked;
		categoryCheckBox.setChecked(category.checked);
		setContentDescription();
		Events.post(new Events.LXFilterCategoryCheckedChanged(category, categoryKey));
	}

	private void setContentDescription() {
		if (category.checked) {
			categoryTitle.setContentDescription(
				Phrase.from(getContext(), R.string.filter_selected_TEMPLATE)
					.put("filter", category.displayValue)
					.format()
					.toString()
			);
		}
		else {
			categoryTitle.setContentDescription(
				Phrase.from(getContext(),  R.string.filter_not_selected_TEMPLATE)
					.put("filter", category.displayValue)
					.format()
					.toString()
			);
		}
	}
}
