package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightCheckoutActivity;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.section.SectionGeneralFlightInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";

	private static final String KEY_DETAILS = "KEY_DETAILS";

	private static final String INSTANCE_REQUESTED_DETAILS = "INSTANCE_REQUESTED_DETAILS";

	private static final int FLIGHT_LEG_BOTTOM_MARGIN = 20;

	private FlightTrip mTrip;

	private ArrayList<SectionFlightLeg> mFlights;
	private ViewGroup mFlightContainer;
	private Button mCheckoutBtn;
	private SectionFlightTrip mFlightTripSectionPriceBar;
	private SectionGeneralFlightInfo mFlightDateAndTravCount;

	private boolean mRequestedDetails = false;

	public static FlightTripOverviewFragment newInstance(String tripKey) {
		FlightTripOverviewFragment fragment = new FlightTripOverviewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TRIP_KEY, tripKey);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRequestedDetails = savedInstanceState.getBoolean(INSTANCE_REQUESTED_DETAILS, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_flight_trip_overview, container, false);

		mFlights = new ArrayList<SectionFlightLeg>();
		mFlightContainer = Ui.findView(v, R.id.flight_legs_container);
		mCheckoutBtn = Ui.findView(v, R.id.checkout_btn);
		mFlightTripSectionPriceBar = Ui.findView(v, R.id.price_bar);
		mFlightDateAndTravCount = Ui.findView(v, R.id.date_and_travlers);

		mCheckoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FlightCheckoutActivity.class);
				startActivity(intent);
			}
		});

		String tripKey = getArguments().getString(ARG_TRIP_KEY);
		mTrip = Db.getFlightSearch().getFlightTrip(tripKey);

		// See if we have flight details we can use, first.
		if (TextUtils.isEmpty(mTrip.getItineraryNumber())) {
			mCheckoutBtn.setEnabled(false);

			// Begin loading flight details in the background, if we haven't already
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (!bd.isDownloading(KEY_DETAILS) && !mRequestedDetails) {
				// Show a loading dialog
				LoadingDetailsDialogFragment df = new LoadingDetailsDialogFragment();
				df.show(getFragmentManager(), LoadingDetailsDialogFragment.TAG);

				bd.startDownload(KEY_DETAILS, mFlightDetailsDownload, mFlightDetailsCallback);
			}
		}

		mFlightDateAndTravCount.bind(mTrip,
				(Db.getTravelers() != null && Db.getTravelers().size() != 0) ? Db.getTravelers()
						.size() : 1);

		//Inflate and store the sections
		SectionFlightLeg tempFlight;
		float density = getActivity().getResources().getDisplayMetrics().density;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
			if (i < mTrip.getLegCount() - 1 || mTrip.getLegCount() == 1) {
				LinearLayout.LayoutParams tempFlightLayoutParams = (LayoutParams) tempFlight.getLayoutParams();
				if (tempFlightLayoutParams == null) {
					tempFlightLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				}
				tempFlightLayoutParams.bottomMargin = (int) Math.floor(FLIGHT_LEG_BOTTOM_MARGIN * density);
				tempFlight.setLayoutParams(tempFlightLayoutParams);
			}

			tempFlight.bind(new FlightTripLeg(mTrip, mTrip.getLeg(i)), false);

			mFlights.add(tempFlight);
			mFlightContainer.addView(tempFlight);
		}

		return v;
	}

	public void bindAll() {
		mFlightTripSectionPriceBar.bind(mTrip);
	}

	@Override
	public void onResume() {
		super.onResume();

		bindAll();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DETAILS)) {
			bd.registerDownloadCallback(KEY_DETAILS, mFlightDetailsCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_DETAILS);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_DETAILS);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_REQUESTED_DETAILS, mRequestedDetails);
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight details download

	private Download<CreateItineraryResponse> mFlightDetailsDownload = new Download<CreateItineraryResponse>() {
		@Override
		public CreateItineraryResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DETAILS, services);
			return services.createItinerary(mTrip.getProductKey(), 0);
		}
	};

	private OnDownloadComplete<CreateItineraryResponse> mFlightDetailsCallback = new OnDownloadComplete<CreateItineraryResponse>() {
		@Override
		public void onDownload(CreateItineraryResponse results) {
			LoadingDetailsDialogFragment df = Ui.findSupportFragment(getCompatibilityActivity(),
					LoadingDetailsDialogFragment.TAG);
			df.dismiss();

			if (results == null) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
						getString(R.string.error_server));
				dialogFragment.show(getFragmentManager(), "errorFragment");
			}
			else if (results.hasErrors()) {
				String error = results.getErrors().get(0).getPresentableMessage(getActivity());
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, error);
				dialogFragment.show(getFragmentManager(), "errorFragment");
			}
			else {
				Db.addItinerary(results.getItinerary());
				mTrip.updateFrom(results.getOffer());
				mRequestedDetails = true;

				Db.kickOffBackgroundSave(getActivity());

				mCheckoutBtn.setEnabled(true);

				if (mTrip.notifyPriceChanged()) {
					String newFare = mTrip.getTotalFare().getFormattedMoney();
					Money oldAmount = new Money(mTrip.getTotalFare());
					oldAmount.subtract(mTrip.getPriceChangeAmount());
					String oldFare = oldAmount.getFormattedMoney();
					String msg = getString(R.string.price_change_alert_TEMPLATE, oldFare, newFare);

					DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, msg);
					dialogFragment.show(getFragmentManager(), "noticeFragment");
				}

				bindAll();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Progress dialog

	public static class LoadingDetailsDialogFragment extends DialogFragment {

		public static final String TAG = LoadingDetailsDialogFragment.class.getName();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setCancelable(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog pd = new ProgressDialog(getActivity());
			pd.setMessage(getString(R.string.loading_flight_details));
			pd.setCanceledOnTouchOutside(false);
			return pd;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);

			// If the dialog is canceled without finishing loading, don't show this page.
			getActivity().finish();
		}
	}
}
