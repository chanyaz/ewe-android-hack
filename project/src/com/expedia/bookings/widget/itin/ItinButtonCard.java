package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.utils.Ui;

public class ItinButtonCard<T extends ItinCardData> extends LinearLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinButtonContentGenerator mItinContentGenerator;
	private OnClickListener mItinButtonOnClickListener;

	// Views

	private View mButtonActionLayout;
	private View mDismissActionLayout;

	private ViewGroup mItinButtonLayout;

	private View mDismissImageView;
	private View mCancelDismissImageView;
	private View mDismissTripTextView;
	private View mDismissAllTextView;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinButtonCard(Context context) {
		super(context);
		init(context);
	}

	public ItinButtonCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(T itinCardData) {
		// Create content generator
		mItinContentGenerator = (ItinButtonContentGenerator) ItinContentGenerator.createGenerator(getContext(),
				itinCardData);

		// Get click listener
		mItinButtonOnClickListener = mItinContentGenerator.getOnItemClickListener();

		// Get button detail view
		View buttonView = mItinContentGenerator.getDetailsView(mItinButtonLayout);
		if (buttonView != null) {
			mItinButtonLayout.removeAllViews();
			mItinButtonLayout.addView(buttonView);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context) {

		inflate(context, R.layout.widget_itin_button_card, this);

		mButtonActionLayout = Ui.findView(this, R.id.button_action_layout);
		mDismissActionLayout = Ui.findView(this, R.id.dismiss_action_layout);
		mItinButtonLayout = Ui.findView(this, R.id.itin_button_layout);
		mDismissImageView = Ui.findView(this, R.id.dismiss_image_view);
		mCancelDismissImageView = Ui.findView(this, R.id.cancel_dismiss_image_view);
		mDismissTripTextView = Ui.findView(this, R.id.dismiss_trip_text_view);
		mDismissAllTextView = Ui.findView(this, R.id.dismiss_all_text_view);

		mButtonActionLayout.setOnClickListener(mOnClickListener);
		mDismissImageView.setOnClickListener(mOnClickListener);
		mCancelDismissImageView.setOnClickListener(mOnClickListener);
		mDismissTripTextView.setOnClickListener(mOnClickListener);
		mDismissAllTextView.setOnClickListener(mOnClickListener);
	}

	private void showDismissLayout() {
        // TODO: animation
		mButtonActionLayout.setVisibility(View.GONE);
		mDismissActionLayout.setVisibility(View.VISIBLE);
	}

	private void showButtonLayout() {
		// TODO: animation
		mButtonActionLayout.setVisibility(View.VISIBLE);
		mDismissActionLayout.setVisibility(View.GONE);
	}

	private void dismissTrip() {

	}

	private void dismissAll() {

	}

	//////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS
	//////////////////////////////////////////////////////////////////////////////////////

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_action_layout: {
				if (mItinButtonOnClickListener != null) {
					mItinButtonOnClickListener.onClick(v);
				}
				break;
			}
			case R.id.dismiss_image_view: {
				showDismissLayout();
				break;
			}
			case R.id.cancel_dismiss_image_view: {
				showButtonLayout();
				break;
			}
			case R.id.dismiss_trip_text_view: {
				dismissTrip();
				break;
			}
			case R.id.dismiss_all_text_view: {
				dismissAll();
				break;
			}
			}
		}
	};
}
