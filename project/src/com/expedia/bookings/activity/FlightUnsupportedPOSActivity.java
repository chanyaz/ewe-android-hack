package com.expedia.bookings.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.Ui;

public class FlightUnsupportedPOSActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.taking_off_soon);

		StatusFragment fragment = Ui.findSupportFragment(this, StatusFragment.TAG);

		if (fragment == null) {
			fragment = new StatusFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, StatusFragment.TAG)
					.commit();
		}

		fragment.showGrounded(Html.fromHtml(getString(R.string.invalid_flights_pos)));
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackErrorPageLoadFlightUnsupportedPOS(this);
	}

	public static boolean isSupportedPOS(Context context) {
		String pos = LocaleUtils.getPointOfSale(context);
		String usPos = context.getString(R.string.point_of_sale_us);
		return usPos.equals(pos);
	}
}
