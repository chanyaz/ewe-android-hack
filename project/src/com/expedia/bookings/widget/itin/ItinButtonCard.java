package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.LocalExpertSite.Destination;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.data.trips.ItinCardDataLocalExpert;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AbsPopupMenu;
import com.expedia.bookings.widget.AbsPopupMenu.OnDismissListener;
import com.expedia.bookings.widget.PopupMenu;
import com.mobiata.android.util.SettingUtils;

public class ItinButtonCard<T extends ItinCardData> extends LinearLayout implements
		AbsPopupMenu.OnMenuItemClickListener, OnDismissListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnHideListener {
		public void onHide(String tripId, ItinButtonType itinButtonType);

		public void onHideAll(ItinButtonType itinButtonType);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC ENUMERATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	public enum ItinButtonType {
		HOTEL_ATTACH,
		LOCAL_EXPERT;

		public static ItinButtonType fromClass(Class<? extends ItinCardData> clazz) {
			if (clazz.equals(ItinCardDataHotelAttach.class)) {
				return HOTEL_ATTACH;
			}
			else if (clazz.equals(ItinCardDataLocalExpert.class)) {
				return LOCAL_EXPERT;
			}

			return null;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private String mTripId;
	private ItinButtonType mItinButtonType;
	private Destination mDestination; // Just for Omniture

	private ItinButtonContentGenerator mItinContentGenerator;
	private OnClickListener mItinButtonOnClickListener;
	private OnHideListener mOnHideListener;

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
		if (mItinContentGenerator != null && mItinContentGenerator.getType() != itinCardData.getTripComponentType()) {
			throw new RuntimeException("Attempted to reuse an ItinCard for two different types of cards!"
					+ "  Previously used " + mItinContentGenerator.getType() + ", reused with"
					+ itinCardData.getTripComponentType());
		}

		mTripId = itinCardData.getTripComponent().getParentTrip().getTripId();
		mItinButtonType = ItinButtonType.fromClass(itinCardData.getClass());

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

		if (itinCardData instanceof ItinCardDataLocalExpert) {
			mDestination = ((ItinCardDataLocalExpert) itinCardData).getSiteDestination();
		}
	}

	public void setOnHideListener(OnHideListener onHideListener) {
		mOnHideListener = onHideListener;
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
		popup.setOnDismissListener(this);

		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_itin_button, popup.getMenu());

		popup.show();
	}

	private void hide() {
		DismissedItinButton.dismiss(mTripId, mItinButtonType);

		if (mOnHideListener != null) {
			mOnHideListener.onHide(mTripId, mItinButtonType);
		}

		if (mDestination != null) {
			OmnitureTracking.trackItinLocalExpertHide(getContext(), mDestination);
			mDestination = null; // Null out, so we don't detect a cancel as well
		}
	}

	private void hideForever() {
		switch (mItinButtonType) {
		case HOTEL_ATTACH: {
			SettingUtils.save(getContext(), R.string.setting_hide_hotel_attach, true);
			break;
		}
		case LOCAL_EXPERT: {
			SettingUtils.save(getContext(), R.string.setting_hide_local_expert, true);
			break;
		}
		}

		if (mOnHideListener != null) {
			mOnHideListener.onHideAll(mItinButtonType);
		}

		if (mDestination != null) {
			OmnitureTracking.trackItinLocalExpertHideForever(getContext(), mDestination);
			mDestination = null; // Null out, so we don't detect a cancel as well
		}
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

	@Override
	public void onDismiss(AbsPopupMenu menu) {
		if (mDestination != null) {
			OmnitureTracking.trackItinLocalExpertHideCancel(getContext(), mDestination);
		}
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
