package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.ViewUtils;

public class HotelTravelerInfoOptionsFragment extends Fragment {
	private static final String TRAVELER_DETAILS_DOWNLOAD = "TRAVELER_DETAILS_DOWNLOAD";

	View mOverviewBtn;
	View mEnterManuallyBtn;

	TextView mEditTravelerLabel;
	View mEditTravelerLabelDiv;
	TextView mSelectTravelerLabel;
	View mSelectTravelerLabelDiv;
	ViewGroup mEditTravelerContainer;
	ViewGroup mAssociatedTravelersContainer;

	int mCurrentTravelerIndex;
	Traveler mCurrentTraveler;

	SectionTravelerInfo mTravelerContact;

	TravelerInfoYoYoListener mListener;

	public static HotelTravelerInfoOptionsFragment newInstance() {
		HotelTravelerInfoOptionsFragment fragment = new HotelTravelerInfoOptionsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsTravelerSelect(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_hotel_traveler_info_options, container, false);

		mCurrentTravelerIndex = getActivity().getIntent().getIntExtra(Codes.TRAVELER_INDEX, 0);

		//Selected traveler
		mCurrentTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();

		mEditTravelerContainer = Ui.findView(v, R.id.edit_traveler_container);
		mEditTravelerLabel = Ui.findView(v, R.id.edit_traveler_label);
		mEditTravelerLabelDiv = Ui.findView(v, R.id.edit_traveler_label_div);
		mSelectTravelerLabel = Ui.findView(v, R.id.select_traveler_label);
		mSelectTravelerLabelDiv = Ui.findView(v, R.id.select_traveler_label_div);
		mAssociatedTravelersContainer = Ui.findView(v, R.id.associated_travelers_container);

		ViewUtils.setAllCaps(mEditTravelerLabel);
		ViewUtils.setAllCaps(mSelectTravelerLabel);

		mEnterManuallyBtn = Ui.findView(v, R.id.enter_info_manually_button);
		mEnterManuallyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Db.getWorkingTravelerManager().shiftWorkingTraveler(new Traveler());
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();

				OmnitureTracking.trackLinkHotelsCheckoutTravelerEnterManually(getActivity());
			}
		});

		//Associated Travelers (From Expedia Account)
		mAssociatedTravelersContainer.removeAllViews();
		if (User.isLoggedIn(getActivity())) {
			Resources res = getResources();
			for (int i = 0; i < Db.getUser().getAssociatedTravelers().size(); i++) {
				final Traveler traveler = Db.getUser().getAssociatedTravelers().get(i);

				//We check if this traveler is already in the list of travelers
				boolean alreadyInUse = false;
				for (int j = 0; j < Db.getTravelers().size(); j++) {
					if (traveler.hasTuid() && Db.getTravelers().get(j).hasTuid()
							&& traveler.getTuid().compareTo(Db.getTravelers().get(j).getTuid()) == 0) {
						alreadyInUse = true;
						break;
					}
				}
				if (alreadyInUse) {
					continue;
				}

				//We inflate the traveler as an option for the user to select
				SectionTravelerInfo travelerInfo = (SectionTravelerInfo) inflater.inflate(
						R.layout.section_hotel_display_traveler_info_name, null);
				travelerInfo.bind(traveler);
				travelerInfo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCurrentTraveler = traveler;

						// Begin loading travler details in the background, if we haven't already
						BackgroundDownloader bd = BackgroundDownloader.getInstance();
						if (!bd.isDownloading(TRAVELER_DETAILS_DOWNLOAD)) {
							// Show a loading dialog
							LoadingTravelerDialogFragment df = new LoadingTravelerDialogFragment();
							df.show(getFragmentManager(), LoadingTravelerDialogFragment.TAG);
							bd.startDownload(TRAVELER_DETAILS_DOWNLOAD, mTravelerDetailsDownload,
									mTravelerDetailsCallback);

							OmnitureTracking.trackLinkFlightCheckoutTravelerSelectExisting(getActivity());
						}
					}
				});

				mAssociatedTravelersContainer.addView(travelerInfo);

				//Add divider
				View divider = new View(getActivity());
				LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
				divider.setLayoutParams(divLayoutParams);
				divider.setBackgroundColor(0x69FFFFFF);
				mAssociatedTravelersContainer.addView(divider);
			}
		}

		mTravelerContact = Ui.findView(v, R.id.current_traveler_contact);

		mTravelerContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryOne();
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof TravelerInfoYoYoListener)) {
			throw new RuntimeException(
					"HotelTravelerInfoOptiosnFragment activity must implement TravelerInfoYoYoListener!");
		}

		mListener = (TravelerInfoYoYoListener) activity;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(TRAVELER_DETAILS_DOWNLOAD);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(TRAVELER_DETAILS_DOWNLOAD);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshCurrentTraveler();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(TRAVELER_DETAILS_DOWNLOAD)) {
			bd.registerDownloadCallback(TRAVELER_DETAILS_DOWNLOAD, mTravelerDetailsCallback);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void refreshCurrentTraveler() {
		if (!mCurrentTraveler.hasName()) {
			mEditTravelerContainer.setVisibility(View.GONE);
			mEditTravelerLabel.setVisibility(View.GONE);
			mSelectTravelerLabel.setText(getString(R.string.select_a_traveler));
		}
		else {
			mEditTravelerContainer.setVisibility(View.VISIBLE);
			mEditTravelerLabel.setVisibility(View.VISIBLE);
			mSelectTravelerLabel.setText(getString(R.string.select_a_different_traveler));
		}

		mEditTravelerLabelDiv.setVisibility(mEditTravelerLabel.getVisibility());
		mSelectTravelerLabelDiv.setVisibility(mSelectTravelerLabel.getVisibility());

		mTravelerContact.bind(mCurrentTraveler);
	}

	public interface TravelerInfoYoYoListener {
		public void moveForward();

		public void setMode(YoYoMode mode);

		public boolean moveBackwards();

		public void displayOptions();

		public void displayTravelerEntryOne();

		public void displayCheckout();
	}

	//////////////////////////////////////////////////////////////////////////
	// Traveler details download

	private Download<SignInResponse> mTravelerDetailsDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(TRAVELER_DETAILS_DOWNLOAD, services);
			return services.updateTraveler(mCurrentTraveler, 0);
		}
	};

	private OnDownloadComplete<SignInResponse> mTravelerDetailsCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			LoadingTravelerDialogFragment df = Ui.findSupportFragment(getCompatibilityActivity(),
					LoadingTravelerDialogFragment.TAG);
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
				Traveler traveler = results.getTraveler();
				if (traveler != null) {
					Db.getWorkingTravelerManager().shiftWorkingTraveler(traveler);
					mCurrentTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
					mCurrentTraveler.setSaveTravelerToExpediaAccount(true);//We default account travelers to save, unless the user alters the name
					mListener.displayCheckout();
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Loading traveler info Progress dialog

	public static class LoadingTravelerDialogFragment extends DialogFragment {

		public static final String TAG = LoadingTravelerDialogFragment.class.getName();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setCancelable(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog pd = new ProgressDialog(getActivity());
			pd.setMessage(getString(R.string.loading_traveler_info));
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
