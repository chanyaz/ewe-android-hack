package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightBookingActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;

public class FlightSlideToPurchaseFragment extends Fragment {

	private static final String HAS_ACCEPTED_TOS = "HAS_ACCEPTED_TOS";

	private SlideToWidget mSlider;
	private boolean mHasAcceptedTOS;

	public static FlightSlideToPurchaseFragment newInstance() {
		FlightSlideToPurchaseFragment fragment = new FlightSlideToPurchaseFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_slide_to_purchase, container, false);

		// Click to accept TOS
		if (savedInstanceState != null && savedInstanceState.containsKey(HAS_ACCEPTED_TOS)) {
			mHasAcceptedTOS = savedInstanceState.getBoolean(HAS_ACCEPTED_TOS);
		}
		else {
			mHasAcceptedTOS = !(PointOfSale.getPointOfSale().requiresRulesRestrictionsCheckbox());
		}
		showHideAcceptTOS(v, false);

		// Slide To Purchase
		TextView price = Ui.findView(v, R.id.trip_price);
		String template = getResources().getString(R.string.your_card_will_be_charged_TEMPLATE);
		String text = String.format(template, Db.getFlightSearch().getSelectedFlightTrip().getTotalFare()
				.getFormattedMoney());
		price.setText(text);

		mSlider = Ui.findView(v, R.id.slide_to_wid);
		mSlider.addSlideToListener(new ISlideToListener() {

			@Override
			public void onSlideStart() {
			}

			@Override
			public void onSlideAllTheWay() {

				//Ensure proper email address
				if (User.isLoggedIn(getActivity()) && !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
					//This should always be a valid email because it is the account email
					Db.getBillingInfo().setEmail(Db.getUser().getPrimaryTraveler().getEmail());
				}

				Db.getBillingInfo().save(getActivity());

				Intent intent = new Intent(getActivity(), FlightBookingActivity.class);
				startActivity(intent);

			}

			@Override
			public void onSlideAbort() {
			}

		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mSlider != null) {
			mSlider.resetSlider();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ACCEPTED_TOS, mHasAcceptedTOS);
	}

	private void showHideAcceptTOS(final View view, final boolean animated) {
		ViewGroup layoutConfirmTOS = Ui.findView(view, R.id.layout_confirm_tos);
		ViewGroup layoutSlideToPurchase = Ui.findView(view, R.id.layout_slide_to_purchase);

		if (mHasAcceptedTOS) {
			layoutConfirmTOS.setVisibility(View.INVISIBLE);
			layoutSlideToPurchase.setVisibility(View.VISIBLE);
		}
		else {
			layoutConfirmTOS.setVisibility(View.VISIBLE);
			layoutSlideToPurchase.setVisibility(View.INVISIBLE);

			Button buttonIAccept = Ui.findView(layoutConfirmTOS, R.id.button_i_accept);
			buttonIAccept.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mHasAcceptedTOS = true;
					showHideAcceptTOS(view, true);
				}
			});
		}

	}
}
