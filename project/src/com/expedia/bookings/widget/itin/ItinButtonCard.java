package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AbsPopupMenu;
import com.expedia.bookings.widget.PopupMenu;
import com.mobiata.android.Log;

public class ItinButtonCard<T extends ItinCardData> extends LinearLayout implements
		AbsPopupMenu.OnMenuItemClickListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinButtonContentGenerator mItinContentGenerator;
	private OnClickListener mItinButtonOnClickListener;

	// Views

	private View mButtonActionLayout;
	private ViewGroup mItinButtonLayout;
	private View mDismissImageView;

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
		mItinButtonLayout = Ui.findView(this, R.id.itin_button_layout);
		mDismissImageView = Ui.findView(this, R.id.dismiss_image_view);

		mButtonActionLayout.setOnClickListener(mOnClickListener);
		mDismissImageView.setOnClickListener(mOnClickListener);
	}

	private void showHidePopup() {
		PopupMenu popup = new PopupMenu(getContext(), mDismissImageView);
		popup.setOnMenuItemClickListener(this);

		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_itin_button, popup.getMenu());

		popup.show();
	}

	private void hide() {
		Log.d("Hiding for this trip");
	}

	private void hideForever() {
		Log.d("Hiding for all trips");
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itin_button_hide: {
			hide();
			return true;
		}
		case R.id.itin_button_hide_forever: {
			hideForever();
			return true;
		}
		}
		return false;
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
				showHidePopup();
				break;
			}
			}
		}
	};
}
