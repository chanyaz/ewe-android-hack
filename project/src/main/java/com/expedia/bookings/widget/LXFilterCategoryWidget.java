package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.otto.Events;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXFilterCategoryWidget extends LinearLayout implements View.OnClickListener {
	private LXCategoryMetadata category;
	private String categoryKey;

	public LXFilterCategoryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//@InjectView(R.id.category)
	TextView categoryTitle;

	//@InjectView(R.id.category_check_box)
	CheckBox categoryCheckBox;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
		setOnClickListener(this);
	}

	public void bind(LXCategoryMetadata category, String categoryKey) {
		this.category = category;
		this.categoryKey = categoryKey;
		categoryTitle.setText(category.displayValue);
		categoryCheckBox.setChecked(category.checked);
		categoryCheckBox.setContentDescription(category.displayValue);
	}

	@Override
	public void onClick(View v) {
		category.checked = !category.checked;
		categoryCheckBox.setChecked(category.checked);
		Events.post(new Events.LXFilterCategoryCheckedChanged(category, categoryKey));
	}
}
