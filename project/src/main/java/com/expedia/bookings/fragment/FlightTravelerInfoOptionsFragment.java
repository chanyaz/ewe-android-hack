package com.expedia.bookings.fragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.model.FlightTravelerFlowState;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.TravelerUtils;
import com.expedia.bookings.utils.Ui;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class FlightTravelerInfoOptionsFragment extends Fragment {

	private static final String INSTANCE_TRAV_CURRENT_DLS = "INSTANCE_TRAV_CURRENT_DLS";
	private static final String INSTANCE_TRAV_UPDATE_TIMES = "INSTANCE_TRAV_UPDATE_TIMES";

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
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, TravelerInfoYoYoListener.class);

		mTuidDownloadTimesType = new TypeToken<HashMap<Long, Long>>() {
		}.getType();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Restore state
		if (savedInstanceState != null) {
			mCurrentTravelerDownloads = savedInstanceState.getStringArrayList(INSTANCE_TRAV_CURRENT_DLS);

			Gson gson = new GsonBuilder().create();
			mTuidDownloadTimes = gson
				.fromJson(savedInstanceState.getString(INSTANCE_TRAV_UPDATE_TIMES), mTuidDownloadTimesType);
		}

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
				FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
				PassengerCategory category = Db.getTravelers().get(mCurrentTravelerIndex).getPassengerCategory(searchParams);
				Traveler trav = new Traveler();
				trav.setPassengerCategory(category);
				Db.getWorkingTravelerManager().shiftWorkingTraveler(trav);
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();

				OmnitureTracking.trackLinkFlightCheckoutTravelerEnterManually();
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

			//We inflate the traveler as an option for the user to select
			SectionTravelerInfo travelerInfo = Ui.inflate(inflater,
				R.layout.section_flight_display_traveler_info_btn, null);
			travelerInfo.bind(traveler);

			toggleTravelerSection(travelerInfo, !alreadyInUse);
			TravelerUtils.setPhoneTextViewVisibility(travelerInfo, mCurrentTravelerIndex);
			mAssociatedTravelersContainer.addView(travelerInfo);

			//After we add the view, lets try to update the traveler (to fetch things like phone #)
			startBackgroundTravelerUpdate(traveler, false);

			//Add divider
			View divider = new View(getActivity());
			LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_gray_divider_height));
			divLayoutParams.setMargins(0, res.getDimensionPixelSize(R.dimen.simple_gray_divider_margin_top), 0,
				res.getDimensionPixelSize(R.dimen.simple_gray_divider_margin_bottom));
			divider.setLayoutParams(divLayoutParams);
			divider.setBackgroundColor(res.getColor(R.color.divider_gray));
			mAssociatedTravelersContainer.addView(divider);
		}

		mTravelerContact = Ui.findView(v, R.id.current_traveler_contact);
		TravelerUtils.setPhoneTextViewVisibility(mTravelerContact, mCurrentTravelerIndex);
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
		OmnitureTracking.trackPageLoadFlightTravelerSelect();
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshCurrentTraveler();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(TRAVELER_DETAILS_DOWNLOAD)) {
			bd.registerDownloadCallback(TRAVELER_DETAILS_DOWNLOAD, mTravelerDetailsCallback);
		}

		reRegisterAllBgTravelerDownloads();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(TRAVELER_DETAILS_DOWNLOAD);
			cancelAllBgTravelerDownloads();
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(TRAVELER_DETAILS_DOWNLOAD);
			unRegisterAllBgTravelerDownloads();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(INSTANCE_TRAV_CURRENT_DLS, mCurrentTravelerDownloads);

		Gson gson = new GsonBuilder().create();
		String times = gson.toJson(mTuidDownloadTimes, mTuidDownloadTimesType);
		outState.putString(INSTANCE_TRAV_UPDATE_TIMES, times);
	}

	public interface TravelerInfoYoYoListener {
		void moveForward();

		void setMode(YoYoMode mode);

		boolean moveBackwards();

		void displayOptions();

		void displayTravelerEntryOne();

		void displayTravelerEntryTwo();

		void displayTravelerEntryThree();

		void displaySaveDialog();

		void displayCheckout();
	}

	private void toggleTravelerSection(final SectionTravelerInfo section, boolean enable) {
		ImageView pic = Ui.findView(section, R.id.display_picture);

		if (enable) {
			if (section.getTraveler().hasTuid()) {
				pic.setImageResource(Ui.obtainThemeResID(getActivity(), R.attr.skin_travellerInfoPageLogo));
			}
			else {
				pic.setImageResource(R.drawable.ic_traveler_blue_entered);
			}
			section.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FlightTravelerFlowState state = FlightTravelerFlowState.getInstance(getActivity());
					mCurrentTraveler = section.getTraveler();
					FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
					PassengerCategory category = Db.getTravelers().get(mCurrentTravelerIndex).getPassengerCategory(searchParams);
					mCurrentTraveler.setPassengerCategory(category);
					BackgroundDownloader bd = BackgroundDownloader.getInstance();

					if (!state.allTravelerInfoValid(mCurrentTraveler,
						Db.getTripBucket().getFlight().getFlightTrip().isInternational())) {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(mCurrentTraveler);
						// force customer through flow when they don't have a passport
						YoYoMode yoYoMode =
							(mCurrentTraveler.getPrimaryPassportCountry() == null) ? YoYoMode.YOYO : YoYoMode.EDIT;
						mListener.setMode(yoYoMode);
						mListener.displayTravelerEntryOne();
					}
					else if (!bd.isDownloading(TRAVELER_DETAILS_DOWNLOAD)) {
						if (travelerIsFresh(mCurrentTraveler)) {
							onTravelerDetailsReceived(mCurrentTraveler);
						}
						else {
							// Begin loading flight details in the background, if we haven't already
							// Show a loading dialog
							ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.loading_traveler_info));
							df.show(getFragmentManager(), DIALOG_LOADING_TRAVELER);
							bd.startDownload(TRAVELER_DETAILS_DOWNLOAD, mTravelerDetailsDownload,
								mTravelerDetailsCallback);
						}

						OmnitureTracking.trackLinkFlightCheckoutTravelerSelectExisting();
					}
				}
			});
		}

		else {
			if (section.getTraveler().hasTuid()) {
				pic.setImageResource(Ui.obtainThemeResID(getActivity(), R.attr.skin_travellerInfoPageLogoDisabled));
			}
			else {
				pic.setImageResource(R.drawable.ic_traveler_grey);
				TextView name = Ui.findView(section, R.id.display_full_name);
				TextView phone = Ui.findView(section, R.id.display_phone_number_with_country_code);
				TextView assist = Ui.findView(section, R.id.display_special_assistance);
				Resources res = getActivity().getResources();
				int disabledGrey = res.getColor(R.color.flights_traveler_disabled_gray);
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
			// We default account travelers to save, unless the user alters the name, or
			// they have more than one passport on their account and are required to manually choose one (#4832)
			boolean isAutoSaveTraveler = traveler.getPassportCountries().size() <= 1;
			mCurrentTraveler.setSaveTravelerToExpediaAccount(isAutoSaveTraveler);
			FlightTravelerFlowState state = FlightTravelerFlowState.getInstance(getActivity());
			if (state.allTravelerInfoIsValidForDomesticFlight(mCurrentTraveler)) {
				boolean flightIsInternational = Db.getTripBucket().getFlight().getFlightTrip().isInternational();
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

	public void updateTravelerInUi(Traveler traveler) {
		if (mAssociatedTravelersContainer != null) {
			for (int i = 0; i < mAssociatedTravelersContainer.getChildCount(); i++) {
				View v = mAssociatedTravelersContainer.getChildAt(i);
				if (v instanceof SectionTravelerInfo) {
					SectionTravelerInfo section = (SectionTravelerInfo) v;
					Traveler sectionTrav = section.getTraveler();
					if (section.getTraveler() != null && section.getTraveler().hasTuid()
						&& section.getTraveler().getTuid().equals(traveler.getTuid())) {
						section.bind(traveler);
						return;
					}
				}
			}
		}
	}

	private void refreshCurrentTraveler() {
		FlightTravelerFlowState state = FlightTravelerFlowState.getInstance(getActivity());
		boolean international = Db.getTripBucket().getFlight().getFlightTrip().isInternational();
		boolean isPassportNeeded = Db.getTripBucket().getFlight().getFlightTrip().isPassportNeeded();
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
			if (international || isPassportNeeded) {
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
	// Traveler details in the background download

	//This stores the last successful update time for tuids - this way if we rotate or something we dont kick off fresh dls.
	private HashMap<Long, Long> mTuidDownloadTimes = new HashMap<Long, Long>();
	private Type mTuidDownloadTimesType;
	//This list contains the keys of our downloads, since we are generating our download keys we need to keep track.
	private ArrayList<String> mCurrentTravelerDownloads = new ArrayList<String>();
	//If a traveler's last update was older than this, don't hesitate to refresh again (this would be an app backgrounded case)
	private static final long TRAVELER_EXPIRATION_MS = 1000 * 60 * 5;

	private String genDlTag(long tuid) {
		return "TRAV_DL_TUID_" + tuid;
	}

	private boolean travelerIsFresh(Traveler trav) {
		if (trav.hasTuid()) {
			if (mTuidDownloadTimes.containsKey(trav.getTuid())
				&& mTuidDownloadTimes.get(trav.getTuid()) + TRAVELER_EXPIRATION_MS > System.currentTimeMillis()) {
				return true;
			}
			return false;
		}
		return true;
	}

	private void startBackgroundTravelerUpdate(Traveler traveler, boolean force) {
		Context context = getActivity();
		if (context != null && traveler != null && traveler.hasTuid()) {
			if (force || !travelerIsFresh(traveler)) {
				BackgroundDownloader dl = BackgroundDownloader.getInstance();
				String dlTag = genDlTag(traveler.getTuid());

				if (mCurrentTravelerDownloads.contains(dlTag) || dl.isDownloading(dlTag)) {
					dl.cancelDownload(dlTag);
				}
				else {
					mCurrentTravelerDownloads.add(dlTag);
				}

				dl.startDownload(dlTag, new TravelerDownload(traveler), new OnTravelerDownloadComplete(dlTag));
			}
		}
	}

	private void cancelAllBgTravelerDownloads() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		for (String tag : mCurrentTravelerDownloads) {
			dl.cancelDownload(tag);
		}
		mCurrentTravelerDownloads.clear();
	}

	private void unRegisterAllBgTravelerDownloads() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		for (String tag : mCurrentTravelerDownloads) {
			dl.unregisterDownloadCallback(tag);
		}
	}

	private void reRegisterAllBgTravelerDownloads() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		ArrayList<String> currDownloads = new ArrayList<>(mCurrentTravelerDownloads);
		for (String tag : currDownloads) {
			dl.registerDownloadCallback(tag, new OnTravelerDownloadComplete(tag));
		}
	}

	private class TravelerDownload implements Download<SignInResponse> {

		private Traveler mTrav;

		public TravelerDownload(Traveler trav) {
			mTrav = trav;
		}

		@Override
		public SignInResponse doDownload() {
			// 3485 timing issue - downloads get kicked off in onCreateView, but we detach quickly sometimes
			if (getActivity() == null) {
				return null;
			}

			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(genDlTag(mTrav.getTuid()), services);
			return services.travelerDetails(mTrav, 0);
		}
	}

	private class OnTravelerDownloadComplete implements OnDownloadComplete<SignInResponse> {

		String mDownloadTag;

		public OnTravelerDownloadComplete(String downloadTag) {
			mDownloadTag = downloadTag;
		}

		@Override
		public void onDownload(SignInResponse results) {
			Context context = getActivity();
			if (context != null && User.isLoggedIn(context)) {
				if (results == null) {
					Log.e("Traveler download fail. results == null");
				}
				else if (results.hasErrors()) {
					Log.e("Traveler download fail. results.hasErrors():" + results.gatherErrorMessage(context));
				}
				else if (results.getTraveler() == null) {
					Log.e("Traveler download fail. results.getTraveler() == null");
				}
				else {
					Traveler updatedTraveler = results.getTraveler();

					//Update traveler in the associated travelers list
					for (int i = 0; i < Db.getUser().getAssociatedTravelers().size(); i++) {
						Traveler trav = Db.getUser().getAssociatedTravelers().get(i);
						if (trav.hasTuid() && trav.getTuid().equals(updatedTraveler.getTuid())) {
							Db.getUser().getAssociatedTravelers().set(i, updatedTraveler);
							break;
						}
					}

					//Update the ui with the new traveler info.
					updateTravelerInUi(updatedTraveler);

					//Update our download stats
					mTuidDownloadTimes.put(updatedTraveler.getTuid(), System.currentTimeMillis());
				}
			}
			mCurrentTravelerDownloads.remove(mDownloadTag);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Traveler selection details download

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
			ThrobberDialog df = Ui.findSupportFragment(getActivity(),
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
