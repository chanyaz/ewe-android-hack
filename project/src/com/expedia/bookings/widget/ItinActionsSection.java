package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.itin.SummaryButton;
import com.mobiata.android.util.Ui;

public class ItinActionsSection extends android.widget.LinearLayout {

	private TextView mLeftButton;
	private TextView mRightButton;

	public ItinActionsSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mLeftButton = Ui.findView(this, R.id.summary_left_button);
		mRightButton = Ui.findView(this, R.id.summary_right_button);
	}

	public void bind(SummaryButton leftData, SummaryButton rightData) {
		if (leftData != null) {
			mLeftButton.setCompoundDrawablesWithIntrinsicBounds(leftData.getIconResId(), 0, 0, 0);
			mLeftButton.setText(leftData.getText());
			mLeftButton.setOnClickListener(leftData.getOnClickListener());
		}

		if (rightData != null) {
			mRightButton.setCompoundDrawablesWithIntrinsicBounds(rightData.getIconResId(), 0, 0, 0);
			mRightButton.setText(rightData.getText());
			mRightButton.setOnClickListener(rightData.getOnClickListener());
		}

		mLeftButton.setVisibility(leftData != null ? VISIBLE : GONE);
		mRightButton.setVisibility(rightData != null ? VISIBLE : GONE);
		Ui.findView(this, R.id.action_button_divider).setVisibility(
				(leftData != null && rightData != null) ? VISIBLE : GONE);

	}
}
