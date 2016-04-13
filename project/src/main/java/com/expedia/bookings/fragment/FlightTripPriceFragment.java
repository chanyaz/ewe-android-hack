package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.ShopWithPointsFlightsUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;

public class FlightTripPriceFragment extends Fragment {

	private FlightTrip mTrip;
	private SectionFlightTrip mTripSection;
	private TextView mPriceChangedTv;
	private ViewGroup mPriceChangeContainer;
	private View mFragmentContent;

	public static FlightTripPriceFragment newInstance() {
		return new FlightTripPriceFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}
		mFragmentContent = inflater.inflate(R.layout.fragment_flight_price_bar, container, false);
		mTripSection = Ui.findView(mFragmentContent, R.id.price_section);
		if (Db.getTripBucket().getFlight().getFlightTrip() != null) {
			mTrip = Db.getTripBucket().getFlight().getFlightTrip();
		}

		mPriceChangeContainer = Ui.findView(mFragmentContent, R.id.price_change_notification_container);
		mPriceChangedTv = Ui.findView(mFragmentContent, R.id.price_change_notification_text);

		if (ShopWithPointsFlightsUtil.isShopWithPointsEnabled(getContext())) {
			CharSequence earnInfoTextToDisplay = ShopWithPointsFlightsUtil.getEarnInfoTextToDisplay(getContext(), mTrip);
			if (Strings.isNotEmpty(earnInfoTextToDisplay)) {
				TextView earnMessaging = Ui.findView(mTripSection, R.id.earn_message);
				earnMessaging.setText(earnInfoTextToDisplay);
			}
		}

		View priceSection = Ui.findView(mTripSection, R.id.price_section);
		priceSection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BreakdownDialogFragment dialog = BreakdownDialogFragment.buildFlightBreakdownDialog(getActivity(),
					Db.getTripBucket().getFlight(), Db.getBillingInfo());
				dialog.show(getFragmentManager(), BreakdownDialogFragment.TAG);
			}
		});

		return mFragmentContent;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		if (Db.getTripBucket().getFlight().getFlightTrip() != null) {
			mTrip = Db.getTripBucket().getFlight().getFlightTrip();
			bind();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// Public methods

	public void hidePriceChange() {
		mPriceChangedTv.setText("");
		mPriceChangeContainer.setVisibility(View.GONE);
	}

	public void showPriceChange(String priceChange) {
		if (mTrip != null && mTrip.notifyPriceChanged() && !TextUtils.isEmpty(priceChange)) {
			mPriceChangedTv.setText(priceChange);
			mPriceChangeContainer.setVisibility(View.VISIBLE);
		}
	}

	public void bind() {
		mTrip = Db.getTripBucket().getFlight().getFlightTrip();
		mTripSection.bind(mTrip, Db.getBillingInfo());
	}


}
