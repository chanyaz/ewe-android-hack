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
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.fragment.FlightBookingFragment.FlightBookingState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.squareup.otto.Subscribe;

public class FlightTripPriceFragment extends Fragment {

	private static final String INSTANCE_REQUESTED_DETAILS = "INSTANCE_REQUESTED_DETAILS";
	private static final String KEY_DETAILS = "KEY_DETAILS";
	private static final String INSTANCE_PRICE_CHANGE = "INSTANCE_PRICE_CHANGE";
	private static final String DIALOG_LOADING_DETAILS = "DIALOG_LOADING_DETAILS";

	private boolean mRequestedDetails = false;
	private String mPriceChangeString = "";
	private FlightTrip mTrip;
	private SectionFlightTrip mTripSection;
	private TextView mPriceChangedTv;
	private ViewGroup mPriceChangeContainer;
	private View mFragmentContent;

	private FlightTripPriceFragmentListener mListener;

	private FlightBookingFragment mFlightBookingFragment;

	public interface FlightTripPriceFragmentListener {
		void onCreateTripFinished();
	}

	public static FlightTripPriceFragment newInstance() {
		return new FlightTripPriceFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// If we somehow get back here and a download is already in progress, cancel it so
			// we don't accidentally use the results of the last details query.
			BackgroundDownloader.getInstance().cancelDownload(KEY_DETAILS);
		}
		else {
			mRequestedDetails = savedInstanceState.getBoolean(INSTANCE_REQUESTED_DETAILS, false);
			mPriceChangeString = savedInstanceState.getString(INSTANCE_PRICE_CHANGE);
		}

		mListener = Ui.findFragmentListener(this, FlightTripPriceFragmentListener.class, false);

		mFlightBookingFragment = Ui.findOrAddSupportFragment(getActivity(), View.NO_ID, FlightBookingFragment.class, FlightBookingFragment.TAG);
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

		// See if we have flight details we can use, first.
		if (TextUtils.isEmpty(mTrip.getItineraryNumber())) {

			// Begin loading flight details in the background, if we haven't already
			if (!mRequestedDetails) {
				startCreateTripDownload();
			}
		}
		else {
			//We call this here because though the trip was already created, we want to ensure
			//that we tell the listener, so it can update the state of Google Wallet
			if (mListener != null) {
				mListener.onCreateTripFinished();
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
		if (getActivity().isFinishing()) {
			mFlightBookingFragment.cancelDownload(FlightBookingState.CREATE_TRIP);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_REQUESTED_DETAILS, mRequestedDetails);
		outState.putString(INSTANCE_PRICE_CHANGE, mPriceChangeString);
	}

	//////////////////////////////////////////////////////////////////////////
	// Public methods

	public void hidePriceChange() {
		mPriceChangeString = null;
		mPriceChangedTv.setText("");
		mPriceChangeContainer.setVisibility(View.GONE);
	}

	public void showPriceChange() {
		if (mTrip != null && mTrip.notifyPriceChanged() && !TextUtils.isEmpty(mPriceChangeString)) {
			mPriceChangedTv.setText(mPriceChangeString);
			mPriceChangeContainer.setVisibility(View.VISIBLE);
		}
	}

	public void bind() {
		//The parent activity uses CheckoutDataLoader to load billingInfo, we wait for it to finish.
		if (CheckoutDataLoader.getInstance().isLoading()) {
			CheckoutDataLoader.getInstance().waitForCurrentThreadToFinish();
		}
		mTrip = Db.getTripBucket().getFlight().getFlightTrip();
		mTripSection.bind(mTrip, Db.getBillingInfo());
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight details download

	public void startCreateTripDownload() {
		if (!mFlightBookingFragment.isDownloadingCreateTrip()) {
			mRequestedDetails = false;

			// Show a loading dialog
			ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.loading_flight_details));
			df.show(getFragmentManager(), DIALOG_LOADING_DETAILS);

			mFlightBookingFragment.startDownload(FlightBookingState.CREATE_TRIP);
		}
	}

	private void dismissDialog() {
		ThrobberDialog df = Ui.findSupportFragment(getActivity(), DIALOG_LOADING_DETAILS);
		if (df != null) {
			df.dismiss();
		}
	}

	///////////////////////////////
	/// Otto Subscriptions

	@Subscribe
	public void onCreateTripDownloadSuccess(Events.CreateTripDownloadSuccess event) {
		dismissDialog();
		mRequestedDetails = true;
		mTripSection.bind(mTrip);
		if (mListener != null) {
			mListener.onCreateTripFinished();
		}
	}

	@Subscribe
	public void onFlightPriceChange(Events.FlightPriceChange event) {
		String changeString = getPriceChangeString();
		if (!TextUtils.isEmpty(changeString)) {
			mPriceChangeString = changeString;
			showPriceChange();
		}
		else {
			hidePriceChange();
		}
	}

	private String getPriceChangeString() {
		if (Db.getTripBucket().getFlight().getFlightTrip() != null) {
			FlightTrip flightTrip = Db.getTripBucket().getFlight().getFlightTrip();
			String originalPrice = flightTrip.getOldTotalFare().getFormattedMoney();
			return getString(R.string.price_changed_from_TEMPLATE, originalPrice);
		}

		return null;
	}

	@Subscribe
	public void onCreateTripRetry(Events.CreateTripDownloadRetry event) {
		startCreateTripDownload();
	}

	@Subscribe
	public void onCreateTripRetryCancel(Events.CreateTripDownloadRetryCancel event) {
		getActivity().finish();
	}

	@Subscribe
	public void onCreateTripError(Events.CreateTripDownloadError event) {
		dismissDialog();
		mRequestedDetails = true;
	}

}
