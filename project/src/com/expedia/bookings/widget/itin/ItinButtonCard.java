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

	// Views

	private ViewGroup mItinButtonLayout;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinButtonCard(Context context) {
		super(context);
		init(context, null);
	}

	public ItinButtonCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(T itinCardData) {
		mItinContentGenerator = (ItinButtonContentGenerator) ItinContentGenerator.createGenerator(getContext(),
				itinCardData);

		View buttonView = mItinContentGenerator.getDetailsView(mItinButtonLayout);
		if (buttonView != null) {
			mItinButtonLayout.removeAllViews();
			mItinButtonLayout.addView(buttonView);
		}
	}

	public void onItemClick() {
		Runnable runnable = mItinContentGenerator.getOnItemClickRunnable(getContext());
		if (runnable != null) {
			runnable.run();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context, AttributeSet attrs) {
		inflate(context, R.layout.widget_attach_card, this);
		mItinButtonLayout = Ui.findView(this, R.id.itin_button_layout);
	}
}
