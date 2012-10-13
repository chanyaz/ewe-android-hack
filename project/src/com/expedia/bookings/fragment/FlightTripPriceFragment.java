package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.OverviewPriceChangeFailureDialogFragment.IPriceChangeFailureDialogListener;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class FlightTripPriceFragment extends Fragment implements IPriceChangeFailureDialogListener {

	private static final String TAG_PRICE_BREAKDOWN_DIALOG = "TAG_PRICE_BREAKDOWN_DIALOG";
	private static final String TAG_PRICE_CHECK_FAILURE_DIALOG = "TAG_PRICE_CHECK_FAILURE_DIALOG";
	private static final String INSTANCE_REQUESTED_DETAILS = "INSTANCE_REQUESTED_DETAILS";
	private static final String KEY_DETAILS = "KEY_DETAILS";
	private static final String INSTANCE_PRICE_CHANGE = "INSTANCE_PRICE_CHANGE";
	private static final String INSTANCE_SHOW_FAILURE_DIALOG = "INSTANCE_SHOW_FAILURE_DIALOG";

	private boolean mRequestedDetails = false;
	private boolean mShowingPriceCheckFailureDialog = false;
	private String mPriceChangeString = "";
	private FlightTrip mTrip;
	private SectionFlightTrip mTripSection;
	private TextView mPriceChangedTv;
	private ViewGroup mPriceChangeContainer;
	private View mFragmentContent;

	public static FlightTripPriceFragment newInstance() {
		FlightTripPriceFragment fragment = new FlightTripPriceFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mRequestedDetails = savedInstanceState.getBoolean(INSTANCE_REQUESTED_DETAILS, false);
			mPriceChangeString = savedInstanceState.getString(INSTANCE_PRICE_CHANGE);
			mShowingPriceCheckFailureDialog = savedInstanceState.getBoolean(INSTANCE_SHOW_FAILURE_DIALOG, false);
		}

		mFragmentContent = inflater.inflate(R.layout.fragment_flight_price_bar,
				container, false);

		mTripSection = Ui.findView(mFragmentContent, R.id.price_section);
		if (Db.getFlightSearch().getSelectedFlightTrip() != null) {
			mTrip = Db.getFlightSearch().getSelectedFlightTrip();
		}

		mPriceChangeContainer = Ui.findView(mFragmentContent, R.id.price_change_notification_container);
		mPriceChangedTv = Ui.findView(mFragmentContent, R.id.price_change_notification_text);

		// See if we have flight details we can use, first.
		if (TextUtils.isEmpty(mTrip.getItineraryNumber())) {
			if (mShowingPriceCheckFailureDialog) {
				showPriceCheckFailureDialog();
			}
			else {
				startPriceCheckDownload();
			}
		}

		View infoBtn = Ui.findView(mTripSection, R.id.info_btn);
		infoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = FlightPriceBreakdownDialogFragment.newInstance();
				newFragment.show(getFragmentManager(), TAG_PRICE_BREAKDOWN_DIALOG);
			}
		});

		return mFragmentContent;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (Db.getFlightSearch().getSelectedFlightTrip() != null) {
			mTrip = Db.getFlightSearch().getSelectedFlightTrip();
			mTripSection.bind(mTrip);
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DETAILS)) {
			bd.registerDownloadCallback(KEY_DETAILS, mFlightDetailsCallback);
		}

	}

	@Override
	public void onPause() {
		super.onPause();

		if (mShowingPriceCheckFailureDialog) {
			dismissPriceCheckFailureDialog(mShowingPriceCheckFailureDialog);
		}

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
		outState.putString(INSTANCE_PRICE_CHANGE, mPriceChangeString);
		outState.putBoolean(INSTANCE_SHOW_FAILURE_DIALOG, mShowingPriceCheckFailureDialog);
	}

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
			getFragmentManager().beginTransaction().remove(df).commit();

			if (results == null) {
				Log.e("results for price check are null");
				showPriceCheckFailureDialog();
			}
			else if (results.hasErrors()) {
				String error = results.getErrors().get(0).getPresentableMessage(getActivity());
				Log.e(error);
				showPriceCheckFailureDialog();
			}
			else {
				Db.addItinerary(results.getItinerary());
				Money originalPrice = mTrip.getTotalFare();

				mTrip.updateFrom(results.getOffer());
				mTripSection.bind(mTrip);//rebind to update price

				mRequestedDetails = true;

				Db.kickOffBackgroundSave(getActivity());

				if (mTrip.notifyPriceChanged()) {
					String priceChangeTemplate = getResources().getString(R.string.price_changed_from_TEMPLATE);
					mPriceChangeString = String.format(priceChangeTemplate, originalPrice.getFormattedMoney());
					showPriceChange();
				}
				else {
					hidePriceChange();
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Error dialog

	private void showPriceCheckFailureDialog() {
		mShowingPriceCheckFailureDialog = true;
		OverviewPriceChangeFailureDialogFragment dialogFragment = (OverviewPriceChangeFailureDialogFragment) getFragmentManager()
				.findFragmentByTag(TAG_PRICE_CHECK_FAILURE_DIALOG);
		if (dialogFragment == null) {
			dialogFragment = OverviewPriceChangeFailureDialogFragment.newInstance(FlightTripPriceFragment.this);
		}
		dialogFragment.setListener(this);
		dialogFragment.show(getFragmentManager(), TAG_PRICE_CHECK_FAILURE_DIALOG);
	}

	private void dismissPriceCheckFailureDialog(boolean showState) {
		mShowingPriceCheckFailureDialog = showState;
		OverviewPriceChangeFailureDialogFragment dialogFragment = (OverviewPriceChangeFailureDialogFragment) getFragmentManager()
				.findFragmentByTag(TAG_PRICE_CHECK_FAILURE_DIALOG);
		if (dialogFragment != null) {
			dialogFragment.dismissAllowingStateLoss();
		}
	}

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

	//////////////////////////////////////////////
	// IPriceChangeFailureDialogListener

	@Override
	public void priceChangeFailureCancel() {
		mShowingPriceCheckFailureDialog = false;
		if (getActivity() != null) {
			//We get out of here

			getActivity().finish();
		}

	}

	@Override
	public void priceChangeFailureRetry() {
		mShowingPriceCheckFailureDialog = false;
		startPriceCheckDownload();
	}

	private void startPriceCheckDownload() {
		// Begin loading flight details in the background, if we haven't already
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(KEY_DETAILS) && !mRequestedDetails) {
			// Show a loading dialog
			LoadingDetailsDialogFragment df = new LoadingDetailsDialogFragment();
			df.show(getFragmentManager(), LoadingDetailsDialogFragment.TAG);

			bd.startDownload(KEY_DETAILS, mFlightDetailsDownload, mFlightDetailsCallback);
		}
	}

}
