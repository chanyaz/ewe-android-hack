package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CarsSupplierFilterWidget extends LinearLayout {

	public CarsSupplierFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.vendor)
	public TextView vendorTitle;

	@InjectView(R.id.vendor_check_box)
	public CheckBox vendorCheckBox;

	@OnClick(R.id.filter_categories_widget)
	public void onCategoryClick() {
		vendorCheckBox.setChecked(!vendorCheckBox.isChecked());
	}

	@OnCheckedChanged(R.id.vendor_check_box)
	public void onCategoryCheckedChanged(boolean checked) {
		Events.post(new Events.CarsSupplierFilterCheckChanged(vendorTitle.getText().toString(), checked));
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bind(String category) {
		vendorTitle.setText(category);
		vendorCheckBox.setChecked(false);
	}

}
