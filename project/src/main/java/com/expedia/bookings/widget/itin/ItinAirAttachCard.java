package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.trips.ItinCardDataAirAttach;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;

public class ItinAirAttachCard<T extends ItinCardDataAirAttach> extends LinearLayout {

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
		mItinButtonOnClickListener = mItinContentGenerator.getOnItemClickListener();
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
		mAirAttachContainerLayout = Ui.findView(this, R.id.air_attach_button_layout);
		mAirAttachButton = mAirAttachContainerLayout.findViewById(R.id.action_text_button);
		mAirAttachButton.setOnClickListener(mOnClickListener);
		mExpirationDateTv = Ui.findView(this, R.id.itin_air_attach_expiration_date_text_view);
		mExpirationDateTv.setText(getResources().getString(R.string.air_attach_expiration_date_TEMPLATE, daysRemaining));
	}

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mItinButtonOnClickListener.onClick(v);
		}
	};

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
}
