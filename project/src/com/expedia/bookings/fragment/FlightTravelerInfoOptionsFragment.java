package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.model.TravelerFlowState;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class FlightTravelerInfoOptionsFragment extends Fragment {

	private static final String TRAVELER_DETAILS_DOWNLOAD = "TRAVELER_DETAILS_DOWNLOAD";
	private static final String DIALOG_LOADING_TRAVELER = "DIALOG_LOADING_TRAVELER";

	View mEnterManuallyBtn;
	View mInternationalDivider;

	TextView mEditTravelerLabel;
	View mEditTravelerLabelDiv;
	TextView mSelectTravelerLabel;
	View mSelectTravelerLabelDiv;
	ViewGroup mEditTravelerContainer;
	ViewGroup mAssociatedTravelersContainer;
	View mPartialTravelerDivider;

	int mCurrentTravelerIndex;
	Traveler mCurrentTraveler;

	SectionTravelerInfo mTravelerContact;
	SectionTravelerInfo mTravelerPrefs;
	SectionTravelerInfo mTravelerPassportCountry;
	SectionTravelerInfo mPartialTraveler;

	TravelerInfoYoYoListener mListener;

	public static FlightTravelerInfoOptionsFragment newInstance() {
		return new FlightTravelerInfoOptionsFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, TravelerInfoYoYoListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_traveler_info_options, container, false);

		mCurrentTravelerIndex = getActivity().getIntent().getIntExtra(Codes.TRAVELER_INDEX, 0);

		//Selected traveler
		mCurrentTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();

		mEditTravelerContainer = Ui.findView(v, R.id.edit_traveler_container);
		mEditTravelerLabel = Ui.findView(v, R.id.edit_traveler_label);
		mEditTravelerLabelDiv = Ui.findView(v, R.id.edit_traveler_label_div);
		mSelectTravelerLabel = Ui.findView(v, R.id.select_traveler_label);
		mSelectTravelerLabelDiv = Ui.findView(v, R.id.select_traveler_label_div);
		mAssociatedTravelersContainer = Ui.findView(v, R.id.associated_travelers_container);
		mInternationalDivider = Ui.findView(v, R.id.current_traveler_passport_country_divider);
		mPartialTravelerDivider = Ui.findView(v, R.id.new_traveler_partial_divider);

		ViewUtils.setAllCaps(mEditTravelerLabel);
		ViewUtils.setAllCaps(mSelectTravelerLabel);

		mEnterManuallyBtn = Ui.findView(v, R.id.enter_info_manually_button);
		mEnterManuallyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Db.getWorkingTravelerManager().shiftWorkingTraveler(new Traveler());
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();

				OmnitureTracking.trackLinkFlightCheckoutTravelerEnterManually(getActivity());
			}
		});

		//Associated Travelers (From Expedia Account)
		mAssociatedTravelersContainer.removeAllViews();
		List<Traveler> alternativeTravelers = BookingInfoUtils.getAlternativeTravelers(getActivity());
		int numAltTravelers = alternativeTravelers.size();
		Resources res = getResources();
		for (int i = 0; i < numAltTravelers; i++) {
			final Traveler traveler = alternativeTravelers.get(i);
			//We check if this traveler is already in the list of travelers
			boolean alreadyInUse = BookingInfoUtils.travelerInUse(traveler);

			//We inflate the traveler as an option for the user to select
			SectionTravelerInfo travelerInfo = (SectionTravelerInfo) inflater.inflate(
				R.layout.section_flight_display_traveler_info_btn, null);
			travelerInfo.bind(traveler);

			toggleTravelerSection(travelerInfo, !alreadyInUse);

			mAssociatedTravelersContainer.addView(travelerInfo);

			//Add divider
			View divider = new View(getActivity());
			LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
			divLayoutParams.setMargins(0, res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_top), 0,
				res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_bottom));
			divider.setLayoutParams(divLayoutParams);
			divider.setBackgroundColor(res.getColor(R.color.divider_grey));
			mAssociatedTravelersContainer.addView(divider);
		}

		mTravelerContact = Ui.findView(v, R.id.current_traveler_contact);
		mTravelerPrefs = Ui.findView(v, R.id.current_traveler_prefs);
		mTravelerPassportCountry = Ui.findView(v, R.id.current_traveler_passport_country);
		mPartialTraveler = Ui.findView(v, R.id.new_traveler_partial);

		mTravelerContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryOne();
			}
		});

		mTravelerPrefs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryTwo();
			}
		});

		mTravelerPassportCountry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryThree();
			}
		});

		mPartialTraveler.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Traveler tTrav = new Traveler();
				tTrav.fromJson(Db.getWorkingTravelerManager().getWorkingTraveler().toJson());
				Db.getWorkingTravelerManager().shiftWorkingTraveler(tTrav);
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightTravelerSelect(getActivity());
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

	public interface TravelerInfoYoYoListener {
		public void moveForward();

		public void setMode(YoYoMode mode);

		public boolean moveBackwards();

		public void displayOptions();

		public void displayTravelerEntryOne();

		public void displayTravelerEntryTwo();

		public void displayTravelerEntryThree();

		public void displaySaveDialog();

		public void displayCheckout();
	}

	private void toggleTravelerSection(final SectionTravelerInfo section, boolean enable) {
		ImageView pic = Ui.findView(section, R.id.display_picture);

		if (enable) {
			if (section.getTraveler().hasTuid()) {
				pic.setImageResource(Ui.obtainThemeResID(getActivity(), R.attr.travellerInfoPageLogo));
			}
			else {
				pic.setImageResource(R.drawable.ic_traveler_blue_entered);
			}
			section.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
					mCurrentTraveler = section.getTraveler();

					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (mCurrentTraveler.fromGoogleWallet()) {
						onTravelerDetailsReceived(mCurrentTraveler);
					}
					else if (!state.allTravelerInfoValid(mCurrentTraveler, Db.getFlightSearch().getSelectedFlightTrip().isInternational())) {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(mCurrentTraveler);
						mListener.setMode(YoYoMode.EDIT);
						mListener.displayTravelerEntryOne();
					}
					else if (!bd.isDownloading(TRAVELER_DETAILS_DOWNLOAD)) {
						// Begin loading flight details in the background, if we haven't already
						// Show a loading dialog
						ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.loading_traveler_info));
						df.show(getFragmentManager(), DIALOG_LOADING_TRAVELER);
						bd.startDownload(TRAVELER_DETAILS_DOWNLOAD, mTravelerDetailsDownload,
							mTravelerDetailsCallback);

						OmnitureTracking.trackLinkFlightCheckoutTravelerSelectExisting(getActivity());
					}
				}
			});
		}

		else {
			if (section.getTraveler().hasTuid()) {
				pic.setImageResource(Ui.obtainThemeResID(getActivity(), R.attr.travellerInfoPageLogoDisabled));
			}
			else {
				pic.setImageResource(R.drawable.ic_traveler_grey);
				TextView name = Ui.findView(section, R.id.display_full_name);
				TextView phone = Ui.findView(section, R.id.display_phone_number_with_country_code);
				TextView assist = Ui.findView(section, R.id.display_special_assistance);
				Resources res = getActivity().getResources();
				int disabledGrey = res.getColor(R.color.flights_traveler_disabled_grey);
				name.setTextColor(disabledGrey);
				phone.setTextColor(disabledGrey);
				assist.setTextColor(disabledGrey);
			}

		}
	}

	private void onTravelerDetailsReceived(Traveler traveler) {
		if (traveler != null) {
			Db.getWorkingTravelerManager().shiftWorkingTraveler(traveler);
			mCurrentTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
			mCurrentTraveler.setSaveTravelerToExpediaAccount(!mCurrentTraveler.fromGoogleWallet());//We default account travelers to save, unless the user alters the name
			TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
			if (state.allTravelerInfoIsValidForDomesticFlight(mCurrentTraveler)) {
				boolean flightIsInternational = Db.getFlightSearch().getSelectedFlightTrip().isInternational();
				if (!flightIsInternational) {
					mListener.displayCheckout();
				}
				else {
					//Because we know we have valid domestic flight, we only need to check the third screen
					if (state.hasValidTravelerPartThree(mCurrentTraveler)) {
						mListener.displayCheckout();
					}
					else {
						mListener.setMode(YoYoMode.YOYO);
						mListener.displayTravelerEntryThree();
					}
				}
			}
			else {
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();
			}
		}
	}

	private void refreshCurrentTraveler() {
		TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
		boolean international = Db.getFlightSearch().getSelectedFlightTrip().isInternational();
		boolean validDomesticTraveler = (state != null)
			&& state.allTravelerInfoIsValidForDomesticFlight(mCurrentTraveler);
		boolean validInternationalTraveler = validDomesticTraveler && state.hasValidTravelerPartThree(mCurrentTraveler);
		boolean hasName = mCurrentTraveler.hasName();

		if ((international && !validInternationalTraveler) || (!international && !validDomesticTraveler)) {
			//Invalid traveler
			mEditTravelerContainer.setVisibility(View.GONE);
			mEditTravelerLabel.setVisibility(View.GONE);
			mSelectTravelerLabel.setText(getString(R.string.select_a_traveler));
			//If we have a partial traveler, show that guy
			mPartialTraveler.setVisibility(hasName ? View.VISIBLE : View.GONE);
		}
		else {
			//Valid traveler!
			mEditTravelerContainer.setVisibility(View.VISIBLE);
			mEditTravelerLabel.setVisibility(View.VISIBLE);
			mSelectTravelerLabel.setText(getString(R.string.select_a_different_traveler));
			if (international) {
				mInternationalDivider.setVisibility(View.VISIBLE);
				mTravelerPassportCountry.setVisibility(View.VISIBLE);
			}
			mPartialTraveler.setVisibility(View.GONE);
		}

		mEditTravelerLabelDiv.setVisibility(mEditTravelerLabel.getVisibility());
		mSelectTravelerLabelDiv.setVisibility(mSelectTravelerLabel.getVisibility());
		mPartialTravelerDivider.setVisibility(mPartialTraveler.getVisibility());

		mTravelerContact.bind(mCurrentTraveler);
		mTravelerPrefs.bind(mCurrentTraveler);
		mTravelerPassportCountry.bind(mCurrentTraveler);
		mPartialTraveler.bind(mCurrentTraveler);
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight details download

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
			ThrobberDialog df = Ui.findSupportFragment(getCompatibilityActivity(),
				DIALOG_LOADING_TRAVELER);
			df.dismiss();

			if (results == null || results.hasErrors()) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
					getString(R.string.unable_to_load_traveler_message));
				dialogFragment.show(getFragmentManager(), "errorFragment");
				if (results != null && results.hasErrors()) {
					String error = results.getErrors().get(0).getPresentableMessage(getActivity());
					Log.e("Traveler Details Error:" + error);
				}
				else {
					Log.e("Traveler Details Results == null!");
				}
			}
			else {
				onTravelerDetailsReceived(results.getTraveler());
			}
		}
	};

}
