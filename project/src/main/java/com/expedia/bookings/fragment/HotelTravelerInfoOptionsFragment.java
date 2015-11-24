package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.ViewUtils;
import com.squareup.phrase.Phrase;

public class HotelTravelerInfoOptionsFragment extends Fragment {

	private static final String TRAVELER_DETAILS_DOWNLOAD = "TRAVELER_DETAILS_DOWNLOAD";
	private static final String DIALOG_LOADING_TRAVELER = "DIALOG_LOADING_TRAVELER";

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
		return new HotelTravelerInfoOptionsFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, TravelerInfoYoYoListener.class);
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

				OmnitureTracking.trackLinkHotelsCheckoutTravelerEnterManually();
			}
		});

		//Associated Travelers (From Expedia Account)
		mAssociatedTravelersContainer.removeAllViews();
		List<Traveler> alternativeTravelers = new ArrayList<Traveler>();
		if (User.isLoggedIn(getActivity()) && Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null) {
			alternativeTravelers.add(Db.getUser().getPrimaryTraveler());
		}
		alternativeTravelers.addAll(BookingInfoUtils.getAlternativeTravelers(getActivity()));
		int numAltTravelers = alternativeTravelers.size();
		Resources res = getResources();
		for (int i = 0; i < numAltTravelers; i++) {
			final Traveler traveler = alternativeTravelers.get(i);

			//We check if this traveler is already in the list of travelers
			boolean alreadyInUse = BookingInfoUtils.travelerInUse(traveler);

			if (alreadyInUse) {
				continue;
			}

			//We inflate the traveler as an option for the user to select
			SectionTravelerInfo travelerInfo = Ui.inflate(inflater,
				R.layout.section_hotel_display_traveler_info_name, null);
			travelerInfo.bind(traveler);
			travelerInfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mCurrentTraveler = traveler;

					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (mCurrentTraveler.fromGoogleWallet()) {
						onTravelerDetailsReceived(mCurrentTraveler);
					}
					else if (!bd.isDownloading(TRAVELER_DETAILS_DOWNLOAD)) {
						// Begin loading travler details in the background, if we haven't already
						// Show a loading dialog
						ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.loading_traveler_info));
						df.show(getFragmentManager(), DIALOG_LOADING_TRAVELER);
						bd.startDownload(TRAVELER_DETAILS_DOWNLOAD, mTravelerDetailsDownload,
							mTravelerDetailsCallback);

						OmnitureTracking.trackLinkFlightCheckoutTravelerSelectExisting();
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
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsTravelerSelect();
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
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(TRAVELER_DETAILS_DOWNLOAD);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(TRAVELER_DETAILS_DOWNLOAD);
		}
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
		void moveForward();

		void setMode(YoYoMode mode);

		boolean moveBackwards();

		void displayOptions();

		void displayTravelerEntryOne();

		void displayCheckout();
	}

	//////////////////////////////////////////////////////////////////////////
	// Traveler details download

	private Download<SignInResponse> mTravelerDetailsDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(TRAVELER_DETAILS_DOWNLOAD, services);
			return services.travelerDetails(mCurrentTraveler, 0);
		}
	};

	private OnDownloadComplete<SignInResponse> mTravelerDetailsCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			ThrobberDialog df = Ui.findSupportFragment(getActivity(), DIALOG_LOADING_TRAVELER);
			df.dismiss();

			if (results == null) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
					Phrase.from(getActivity(), R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format()
						.toString());
				dialogFragment.show(getFragmentManager(), "errorFragment");
			}
			else if (results.hasErrors()) {
				String error = results.getErrors().get(0).getPresentableMessage(getActivity());
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, error);
				dialogFragment.show(getFragmentManager(), "errorFragment");
			}
			else {
				onTravelerDetailsReceived(results.getTraveler());
			}
		}
	};

	private void onTravelerDetailsReceived(Traveler traveler) {
		if (traveler != null) {
			Db.getWorkingTravelerManager().shiftWorkingTraveler(traveler);
			mCurrentTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
			mCurrentTraveler.setSaveTravelerToExpediaAccount(
				!traveler.fromGoogleWallet());//We default account travelers to save, unless the user alters the name
			HotelTravelerFlowState state = HotelTravelerFlowState.getInstance(getActivity());
			if (state.hasValidTraveler(mCurrentTraveler)) {
				mListener.displayCheckout();
			}
			else {
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();
			}
		}
	}
}
