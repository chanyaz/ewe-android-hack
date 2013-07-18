package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;

public abstract class ConfirmationFragment extends Fragment {

	protected abstract int getLayoutId();

	protected abstract int getActionsLayoutId();

	protected abstract String getItinNumber();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(getLayoutId(), container, false);

		Ui.setText(v, R.id.itinerary_text_view,
				getString(R.string.itinerary_confirmation_TEMPLATE, getItinNumber()));

		Ui.setText(v, R.id.email_text_view, Db.getBillingInfo().getEmail());

		// Inflate the custom actions layout id
		ViewGroup actionContainer = Ui.findView(v, R.id.custom_actions_container);
		inflater.inflate(getActionsLayoutId(), actionContainer, true);

		// 1370. VSC Hide the Call to Customer support option
		if (ExpediaBookingApp.IS_VSC) {
			Ui.findView(v, R.id.call_action_text_view).setVisibility(View.GONE);
		}
		else {
			Ui.setOnClickListener(v, R.id.call_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getActivity(), PointOfSale.getPointOfSale().getSupportPhoneNumber());
				}
			});
		}

		return v;
	}
}
