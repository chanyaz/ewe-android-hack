package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.itin.SummaryButton;
import com.mobiata.android.util.Ui;

public class ItinActionsSection extends android.widget.LinearLayout {

	private View mLeftLayout;
	private View mRightLayout;

	private TextView mLeftButton;
	private TextView mRightButton;

	public ItinActionsSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mLeftLayout = Ui.findView(this, R.id.summary_left_layout);
		mRightLayout = Ui.findView(this, R.id.summary_right_layout);

		mLeftButton = Ui.findView(this, R.id.summary_left_button);
		mRightButton = Ui.findView(this, R.id.summary_right_button);
	}

	public void bind(SummaryButton leftData, SummaryButton rightData) {
		if (leftData != null) {
			setSummaryButton(mLeftButton, leftData);
		}
		if (rightData != null) {
			setSummaryButton(mRightButton, rightData);
		}

		mLeftLayout.setVisibility(leftData != null ? VISIBLE : GONE);
		mRightLayout.setVisibility(rightData != null ? VISIBLE : GONE);
		Ui.findView(this, R.id.action_button_divider).setVisibility(
				(leftData != null && rightData != null) ? VISIBLE : GONE);

	}

	private void setSummaryButton(final TextView textView, final SummaryButton summaryButton) {
		textView.setCompoundDrawablesWithIntrinsicBounds(summaryButton.getIconResId(), 0, 0, 0);
		textView.setText(summaryButton.getText());
		textView.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				if (!summaryButton.getShouldShowPopup()) {
					summaryButton.getOnClickListener().onClick(v);
				}
				else {
					final View contentView = summaryButton.getPopupContentView();
					final PopupWindow popup = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT, true);

					popup.setBackgroundDrawable(new BitmapDrawable());
					popup.setOutsideTouchable(true);
					popup.setTouchable(true);

					// Some hackery is necessary here: in order to layout the content so we can get its
					// height, we show it off-screen, then we post a runnable to set it's position relative
					// too the textview. We don't use showAsDropDown because it moved off-screen due
					// to some listview weirdness (I'm assuming) and the hacky stuff we're doing to implement
					// the expand/collapse animations.
					popup.showAtLocation(textView, Gravity.NO_GRAVITY, -1000, -1000);
					contentView.post(new Runnable() {
						@Override
						public void run() {
							final int[] location = new int[2];
							textView.getLocationOnScreen(location);

							popup.update(location[0], location[1] - contentView.getHeight(), -1, -1);
							contentView.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									if (summaryButton.getPopupOnClickListener() != null) {
										summaryButton.getPopupOnClickListener().onClick(v);
									}
									popup.dismiss();
								}
							});
						}
					});
				}
			}
		});
	}
}
