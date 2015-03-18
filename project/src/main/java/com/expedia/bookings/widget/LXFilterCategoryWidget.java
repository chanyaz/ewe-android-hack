package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.otto.Events;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;

public class LXFilterCategoryWidget extends LinearLayout {
	private LXCategoryMetadata category;
	private String categoryKey;

	public LXFilterCategoryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.category)
	TextView categoryTitle;

	@InjectView(R.id.category_check_box)
	CheckBox categoryCheckBox;

	@OnCheckedChanged(R.id.category_check_box)
	public void onCategoryCheckedChanged(boolean checked) {
		category.checked = checked;
		Events.post(new Events.LXFilterCategoryCheckedChanged(category, categoryKey));

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
	}

	public void bind(LXCategoryMetadata category, String categoryKey) {
		this.category = category;
		this.categoryKey = categoryKey;
		categoryTitle.setText(category.displayValue);
		categoryCheckBox.setChecked(category.checked);
	}

}
