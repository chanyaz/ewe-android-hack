package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.ItinCardDataAirAttach;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.SettingUtils;

public class ItinAirAttachCard<T extends ItinCardDataAirAttach> extends LinearLayout implements
	PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinButtonContentGenerator mItinContentGenerator;
	private OnClickListener mItinButtonOnClickListener;
	private AirAttach mAirAttach;
	private DateTime mExpirationDate;

	// Views
	private ViewGroup mAirAttachContainerLayout;
	private TextView mExpirationDateTv;
	private View mAirAttachButton;

	private String mTripId;
	private ItinButtonCard.OnHideListener mOnHideListener;
	private ImageView mDismissImageView;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinAirAttachCard(Context context) {
		super(context);
		init(context);
	}

	public ItinAirAttachCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(T itinCardData) {
		mItinContentGenerator = (ItinButtonContentGenerator) ItinContentGenerator.createGenerator(getContext(),
			itinCardData);
		mTripId = itinCardData.getTripComponent().getParentTrip().getTripId();

		mItinButtonOnClickListener = mItinContentGenerator.getOnItemClickListener();

		final String buttonText;
		FlightLeg flightLeg = itinCardData.getFlightLeg();

		if (flightLeg != null && flightLeg.getLastWaypoint() != null
			&& flightLeg.getLastWaypoint().getAirport() != null
			&& !TextUtils.isEmpty(flightLeg.getLastWaypoint().getAirport().mCity)) {
			buttonText = getResources().getString(R.string.add_hotel_TEMPLATE,
				itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity);
		}
		else {
			buttonText = getResources().getString(R.string.add_hotel_air_attach);
		}

		Ui.setText(this, R.id.action_text_view, buttonText);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context) {
		// Initialize air attach data
		mAirAttach = Db.getTripBucket().getAirAttach();
		mExpirationDate = mAirAttach.getExpirationDate();
		DateTime currentDate = new DateTime();
		int daysRemaining = JodaUtils.daysBetween(currentDate, mExpirationDate);

		// Get air attach button layout
		inflate(context, R.layout.itin_air_attach_card, this);
		mAirAttachContainerLayout = Ui.findView(this, R.id.itin_button_layout);
		mAirAttachButton = mAirAttachContainerLayout.findViewById(R.id.button_action_layout);
		mAirAttachButton.setOnClickListener(mOnClickListener);
		mExpirationDateTv = Ui.findView(this, R.id.itin_air_attach_expiration_date_text_view);
		mExpirationDateTv.setText(getResources().getString(R.string.air_attach_expiration_date_TEMPLATE, daysRemaining));
		mDismissImageView = Ui.findView(this, R.id.dismiss_image_view);
		mDismissImageView.setVisibility(View.VISIBLE);
		mDismissImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showHidePopup();
			}
		});
	}

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mItinButtonOnClickListener.onClick(v);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	public void setOnHideListener(ItinButtonCard.OnHideListener onHideListener) {
		mOnHideListener = onHideListener;
	}

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

	@Override
	public void onDismiss(PopupMenu menu) {

	}

	private void showHidePopup() {
		PopupMenu popup = new PopupMenu(getContext(), mDismissImageView);
		popup.setOnMenuItemClickListener(this);
		popup.setOnDismissListener(this);

		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_itin_button, popup.getMenu());

		popup.show();
	}

	private void hide() {
		DismissedItinButton.dismiss(mTripId, ItinButtonCard.ItinButtonType.AIR_ATTACH);

		if (mOnHideListener != null) {
			mOnHideListener.onHide(mTripId, ItinButtonCard.ItinButtonType.AIR_ATTACH);
		}
	}

	private void hideForever() {
		SettingUtils.save(getContext(), R.string.setting_hide_local_expert, true);
		if (mOnHideListener != null) {
			mOnHideListener.onHideAll(null);
		}
	}

	public boolean isTouchOnAirAttachButton(MotionEvent event) {
		if (mAirAttachButton != null && mAirAttachButton.getVisibility() == View.VISIBLE && event != null) {
			int ex = (int) event.getX();
			int ey = (int) event.getY();

			int ax1 = mAirAttachButton.getLeft();
			int ax2 = mAirAttachButton.getRight();
			int ay1 = mAirAttachButton.getTop();
			int ay2 = mAirAttachButton.getBottom();

			if (ax1 <= ex && ax2 >= ex && ay1 <= ey && ay2 >= ey) {
				return true;
			}
		}
		return false;
	}

	public boolean isTouchOnDismissButton(MotionEvent event) {
		if (mDismissImageView != null && mDismissImageView.getVisibility() == View.VISIBLE && event != null) {
			int ex = (int) event.getX();
			int ey = (int) event.getY();

			int ax1 = mDismissImageView.getLeft();
			int ax2 = mDismissImageView.getRight();
			int ay1 = mDismissImageView.getTop();
			int ay2 = mDismissImageView.getBottom();

			if (ax1 <= ex && ax2 >= ex && ay1 <= ey && ay2 >= ey) {
				return true;
			}
		}
		return false;
	}

}
