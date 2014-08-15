package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.BirthDateInvalidDialog;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.enums.TravelerFormState;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.interfaces.IDialogForwardBackwardListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionTravelerInfoTablet;
import com.expedia.bookings.section.TravelerAutoCompleteAdapter;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.TravelerUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutTravelerFormFragment extends TabletCheckoutDataFormFragment implements InputFilter,
	IStateProvider<TravelerFormState>, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	IDialogForwardBackwardListener {

	public static TabletCheckoutTravelerFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutTravelerFormFragment frag = new TabletCheckoutTravelerFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private static final String DL_FETCH_TRAVELER_INFO = "DL_FETCH_TRAVELER_INFO";

	private static final String FTAG_FETCH_TRAVELER_INFO = "FTAG_FETCH_TRAVELER_INFO";
	private static final String FTAG_SAVE_DIALOG = "FTAG_SAVE_DIALOG";
	private static final String FTAG_OVERWRITE_DIALOG = "FTAG_OVERWRITE_DIALOG";
	private static final String FTAG_SAVING = "FTAG_SAVING";
	private static final String FTAG_INVALID_BIRTHDATE_DIALOG = "FTAG_INVALID_BIRTHDATE_DIALOG";

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";
	private static final String STATE_HEADER_STRING = "STATE_HEADER_STRING";
	private static final String STATE_TRAVELER_FORM_STATE = "STATE_TRAVELER_FORM_STATE";
	private static final String STATE_ATTEMPT_TO_LEAVE = "STATE_ATTEMPT_TO_LEAVE";

	private int mTravelerNumber = -1;
	private String mHeaderString;
	private SectionTravelerInfoTablet mSectionTraveler;
	private AutoCompleteTextView mTravelerAutoComplete;
	private TravelerAutoCompleteAdapter mTravelerAdapter;
	private boolean mAttemptToLeaveMade = false;
	private ICheckoutDataListener mListener;


	private StateManager<TravelerFormState> mStateManager = new StateManager<TravelerFormState>(
		TravelerFormState.EDITING, this);


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mStateManager.setDefaultState(TravelerFormState.valueOf(savedInstanceState.getString(
				STATE_TRAVELER_FORM_STATE, TravelerFormState.EDITING.name())));
			mAttemptToLeaveMade = savedInstanceState.getBoolean(STATE_ATTEMPT_TO_LEAVE, mAttemptToLeaveMade);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}


	@Override
	public void onResume() {
		super.onResume();
		bindToDb(mTravelerNumber);

		BackgroundDownloader dl = BackgroundDownloader.getInstance();

		if (isFormOpen()) {
			onFormOpened();

			if (dl.isDownloading(DL_FETCH_TRAVELER_INFO)) {
				dl.registerDownloadCallback(DL_FETCH_TRAVELER_INFO, mTravelerDetailsCallback);
			}
		}
		else {
			onFormClosed();

			if (dl.isDownloading(DL_FETCH_TRAVELER_INFO)) {
				dl.cancelDownload(DL_FETCH_TRAVELER_INFO);
			}

		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			dl.cancelDownload(DL_FETCH_TRAVELER_INFO);
		}
		else {
			dl.unregisterDownloadCallback(DL_FETCH_TRAVELER_INFO, mTravelerDetailsCallback);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTravelerNumber = savedInstanceState.getInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
			mHeaderString = savedInstanceState.getString(STATE_HEADER_STRING, mHeaderString);
			if (Db.getWorkingTravelerManager().getAttemptToLoadFromDisk() && Db.getWorkingTravelerManager()
				.hasTravelerOnDisk(getActivity())) {
				Db.getWorkingTravelerManager().loadWorkingTravelerFromDisk(getActivity());
			}
		}

		mTravelerAdapter = new TravelerAutoCompleteAdapter(getActivity());

		registerStateListener(mStateHelper, false);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
		if (mHeaderString != null) {
			outState.putString(STATE_HEADER_STRING, mHeaderString);
		}
		outState.putString(STATE_TRAVELER_FORM_STATE, mStateManager.getState().name());
		outState.putBoolean(STATE_ATTEMPT_TO_LEAVE, mAttemptToLeaveMade);
	}

	public boolean isFormOpen() {
		return mStateManager.getState() != TravelerFormState.COLLAPSED;
	}

	public void bindToDb(int travelerNumber) {
		if (mTravelerNumber != travelerNumber || Db.getWorkingTravelerManager().getWorkingTraveler() == null) {
			Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(travelerNumber));
		}
		mTravelerNumber = travelerNumber;
		if (mTravelerAdapter != null) {
			mTravelerAdapter.setTravelerNumber(mTravelerNumber);
		}
		if (mSectionTraveler != null && travelerNumber >= 0 && travelerNumber < Db.getTravelers().size()) {
			mSectionTraveler.resetValidation();

			//We only show the email field for the first traveler, if we aren't logged in.
			mSectionTraveler
				.setEmailFieldEnabled(
					TravelerUtils.travelerFormRequiresEmail(mTravelerNumber, getLob(), getActivity()));

			//We only show the passport field if we are buying an international flight.
			mSectionTraveler.setPassportCountryFieldEnabled(TravelerUtils.travelerFormRequiresPassport(getLob()));
			mSectionTraveler.setPhoneFieldsEnabled(mTravelerNumber);

			mSectionTraveler.bind(Db.getWorkingTravelerManager().getWorkingTraveler());
			setHeadingText(mHeaderString);
			setHeadingButtonText(getString(R.string.done));
			setHeadingButtonOnClick(mHeaderButtonClickListener);
		}
	}


	private OnClickListener mHeaderButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			proceed();
		}
	};

	private StateListenerHelper<TravelerFormState> mStateHelper = new StateListenerHelper<TravelerFormState>() {
		@Override
		public void onStateTransitionStart(TravelerFormState stateOne, TravelerFormState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(TravelerFormState stateOne, TravelerFormState stateTwo, float percentage) {

		}

		@Override
		public void onStateTransitionEnd(TravelerFormState stateOne, TravelerFormState stateTwo) {

		}

		@Override
		public void onStateFinalized(TravelerFormState state) {
			setFragmentState(state);
		}
	};

	private void setFragmentState(TravelerFormState state) {
		boolean showSaveDialog = (state == TravelerFormState.SAVE_PROMPT);
		boolean showOverwriteDialog = (state == TravelerFormState.OVERWRITE_PROMPT);

		if (isAdded()) {
			FragmentManager manager = getChildFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			FragmentAvailabilityUtils.setFragmentAvailability(showSaveDialog, FTAG_SAVE_DIALOG, manager, transaction, this,
				FragmentAvailabilityUtils.DIALOG_FRAG, true);
			FragmentAvailabilityUtils
				.setFragmentAvailability(showOverwriteDialog, FTAG_OVERWRITE_DIALOG, manager, transaction, this,
					FragmentAvailabilityUtils.DIALOG_FRAG, true);
			transaction.commitAllowingStateLoss();
		}
	}

	private void proceed() {
		if (getActivity() == null) {
			return;
		}

		mAttemptToLeaveMade = true;
		if (mSectionTraveler != null && mSectionTraveler.performValidation()) {
			Context context = getActivity();
			if (context == null) {
				return;
			}

			if (!User.isLoggedIn(context)) {
				commitAndCloseForm();
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				//Here is our situation: the service that we use to commit traveler changes to the api requires a ton
				//of info e.g. it will bomb if we don't supply gender. Our hotel travelers form does not include some
				//of this required info, so saving is a moot point because it will always silently fail in the bg.
				//TODO: Look into the save traveler service, and see if there is a way around this
				commitAndCloseForm();
			}
			else {
				TravelerFormState state = mStateManager.getState();
				if (state == TravelerFormState.EDITING) {
					if (!Db.getWorkingTravelerManager().getWorkingTraveler()
						.getSaveTravelerToExpediaAccount() && Db.getWorkingTravelerManager()
						.workingTravelerDiffersFromBase()) {
						mStateManager.setState(TravelerFormState.SAVE_PROMPT, false);
					}
					else {
						commitAndCloseForm();
					}
				}
				else if (state == TravelerFormState.SAVE_PROMPT) {
					if (Db.getWorkingTravelerManager().getWorkingTraveler().getSaveTravelerToExpediaAccount()) {
						if (BookingInfoUtils.travelerRequiresOverwritePrompt(context,
							Db.getWorkingTravelerManager().getWorkingTraveler())) {
							mStateManager.setState(TravelerFormState.OVERWRITE_PROMPT, false);
						}
						else {
							commitAndCloseForm();
						}
					}
					else {
						commitAndCloseForm();
					}

				}
				else {
					commitAndCloseForm();
				}
			}
		}
		else if (mSectionTraveler != null && !mSectionTraveler.isBirthdateAligned()) {
			BirthDateInvalidDialog dialog = BirthDateInvalidDialog.newInstance(true);
			dialog.show(getChildFragmentManager(), FTAG_INVALID_BIRTHDATE_DIALOG);
		}
	}

	private void commitAndCloseForm() {

		//Save the traveler to the User account on a background thread
		//We ignore errors and successes (for now) because this is a non-critical operation.
		if (User.isLoggedIn(getActivity()) && Db.getWorkingTravelerManager().getWorkingTraveler()
			.getSaveTravelerToExpediaAccount() && Db.getWorkingTravelerManager().workingTravelerDiffersFromBase()) {

			if (Db.getWorkingTravelerManager().getWorkingTraveler().hasTuid() && Db.getWorkingTravelerManager()
				.workingTravelerNameDiffersFromBase()) {
				//If the name has changed at all, we consider this a new traveler, and thereby remove the existing tuid.
				Db.getWorkingTravelerManager().getWorkingTraveler().resetTuid();
			}

			Db.getWorkingTravelerManager()
				.commitTravelerToAccount(getActivity(), Db.getWorkingTravelerManager().getWorkingTraveler(), false,
					new WorkingTravelerManager.ITravelerUpdateListener() {
						@Override
						public void onTravelerUpdateFinished() {
							//On tablet we are doing the save in the background, and don't reflect success failure on the ui.
						}

						@Override
						public void onTravelerUpdateFailed() {
							//On tablet we are doing the save in the background, and don't reflect success failure on the ui.
						}
					}
				);
		}

		//Commit our changes
		Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerNumber, getActivity());

		mListener.onCheckoutDataUpdated();
		clearForm();

		Ui.hideKeyboard(getActivity(), InputMethodManager.HIDE_NOT_ALWAYS);
		closeForm(true);
	}

	private void clearForm() {
		mAttemptToLeaveMade = false;
		if (mSectionTraveler != null) {
			mSectionTraveler.resetValidation();
		}

		Db.getWorkingTravelerManager().clearWorkingTraveler(getActivity());
		mTravelerNumber = -1;
	}

	@Override
	public void setUpFormContent(ViewGroup formContainer) {
		//This will probably end up having way more moving parts than this...
		formContainer.removeAllViews();
		if (getLob() == LineOfBusiness.HOTELS) {
			mSectionTraveler = Ui.inflate(this, R.layout.section_hotel_edit_traveler, null);
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionTraveler = Ui.inflate(this, R.layout.section_flight_edit_traveler, null);

			//Here we setup our flights specific animations, namely just

			final View seatingPrefBtn = Ui.findView(mSectionTraveler, R.id.seating_pref_btn);
			final View specialAssistanceBtn = Ui.findView(mSectionTraveler, R.id.special_assistance_btn);
			final View redressBtn = Ui.findView(mSectionTraveler, R.id.redress_btn);

			final View seatPrefField = Ui.findView(mSectionTraveler, R.id.edit_seat_preference_spinner);
			final View specialAssistanceField = Ui.findView(mSectionTraveler, R.id.edit_assistance_preference_spinner);
			final View redressField = Ui.findView(mSectionTraveler, R.id.edit_redress_number);

			final View okBtn = Ui.findView(mSectionTraveler, R.id.ok_btn);

			okBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.GONE);
					seatPrefField.setVisibility(View.GONE);
					specialAssistanceField.setVisibility(View.GONE);
					redressField.setVisibility(View.GONE);
					seatingPrefBtn.setVisibility(View.VISIBLE);
					specialAssistanceBtn.setVisibility(View.VISIBLE);
					redressBtn.setVisibility(View.VISIBLE);
				}
			});

			seatingPrefBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.VISIBLE);
					seatPrefField.setVisibility(View.VISIBLE);
					specialAssistanceField.setVisibility(View.GONE);
					redressField.setVisibility(View.GONE);
					seatingPrefBtn.setVisibility(View.GONE);
					specialAssistanceBtn.setVisibility(View.GONE);
					redressBtn.setVisibility(View.GONE);
				}
			});

			specialAssistanceBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.VISIBLE);
					seatPrefField.setVisibility(View.GONE);
					specialAssistanceField.setVisibility(View.VISIBLE);
					redressField.setVisibility(View.GONE);
					seatingPrefBtn.setVisibility(View.GONE);
					specialAssistanceBtn.setVisibility(View.GONE);
					redressBtn.setVisibility(View.GONE);
				}
			});

			redressBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.VISIBLE);
					seatPrefField.setVisibility(View.GONE);
					specialAssistanceField.setVisibility(View.GONE);
					redressField.setVisibility(View.VISIBLE);
					seatingPrefBtn.setVisibility(View.GONE);
					specialAssistanceBtn.setVisibility(View.GONE);
					redressBtn.setVisibility(View.GONE);
				}
			});
		}
		mSectionTraveler.setLob(getLob());

		mSectionTraveler.addChangeListener(new ISectionEditable.SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTraveler.performValidation();
				}

				//We attempt a save on change
				Db.getWorkingTravelerManager().attemptWorkingTravelerSave(getActivity(), false);
			}
		});

		mSectionTraveler.addInvalidCharacterListener(new InvalidCharacterHelper.InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, InvalidCharacterHelper.Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		});

		//We set up the AutoComplete view such that it will be the first name or last name field depending on POS.
		//We leave the remainder of the AutoComplete's setup to onFormOpen and onFocus.
		EditText editName = Ui.findView(mSectionTraveler,
			PointOfSale.getPointOfSale().showLastNameFirst() ? R.id.edit_last_name : R.id.edit_first_name);
		if (editName != null && editName instanceof AutoCompleteTextView) {
			mTravelerAutoComplete = (AutoCompleteTextView) editName;
			//Create a filter that
			InputFilter[] filters = new InputFilter[] { this };
			mTravelerAutoComplete.setFilters(filters);
			mTravelerAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Traveler trav = mTravelerAdapter.getItemFromId(id);
					if (trav != null) {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(trav);

						if (!trav.fromGoogleWallet()) {
							//Cancel previous download
							BackgroundDownloader dl = BackgroundDownloader.getInstance();
							if (dl.isDownloading(DL_FETCH_TRAVELER_INFO)) {
								dl.cancelDownload(DL_FETCH_TRAVELER_INFO);
							}

							// Begin loading flight details in the background, if we haven't already
							// Show a loading dialog
							ThrobberDialog df = ThrobberDialog
								.newInstance(getString(R.string.loading_traveler_info));
							df.show(getChildFragmentManager(), FTAG_FETCH_TRAVELER_INFO);
							dl.startDownload(DL_FETCH_TRAVELER_INFO, mTravelerDetailsDownload,
								mTravelerDetailsCallback);

							//We update the traveler and bind again on the download callback
						}

						//This will refresh the ui with our new traveler (just with the name if we are downoading details).
						bindToDb(mTravelerNumber);
					}
				}
			});
		}

		formContainer.addView(mSectionTraveler);
	}

	public void setHeaderText(String headerString) {
		mHeaderString = headerString;
	}

	@Override
	public void onFormClosed() {
		if (isResumed() && isFormOpen()) {
			mAttemptToLeaveMade = false;
			Db.getWorkingTravelerManager().deleteWorkingTravelerFile(getActivity());
			mTravelerAdapter.clearFilter();
			mTravelerAutoComplete.dismissDropDown();
			mTravelerAutoComplete.setAdapter((ArrayAdapter<Traveler>) null);
			clearForm();
		}
		mStateManager.setState(TravelerFormState.COLLAPSED, false);
	}

	@Override
	public void onFormOpened() {
		if (isResumed() && (!isFormOpen() || mTravelerAutoComplete.hasFocus())) {
			mTravelerAutoComplete.setOnFocusChangeListener(mNameFocusListener);
			mTravelerAutoComplete.requestFocus();//<-- This fires the onFocusChangeListener
			mTravelerAutoComplete.postDelayed(new Runnable() {
				@Override
				public void run() {
					Context context = getActivity();
					if (context != null) {
						InputMethodManager imm = (InputMethodManager) context
							.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(mTravelerAutoComplete, 0);
					}
				}
			}, 250);
		}
		if (!isFormOpen()) {
			mStateManager.setState(TravelerFormState.EDITING, false);
		}
	}

	@Override
	public boolean showBoardingMessage() {
		if (getLob() == LineOfBusiness.FLIGHTS) {
			return true;
		}
		else {
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Focus listener

	private View.OnFocusChangeListener mNameFocusListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (mTravelerAutoComplete != null) {
				if (hasFocus) {
					mTravelerAutoComplete.setAdapter(mTravelerAdapter);
					mTravelerAutoComplete.showDropDown();
				}
				else {
					mTravelerAutoComplete.dismissDropDown();
					mTravelerAutoComplete.setAdapter((ArrayAdapter<Traveler>) null);

				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// InputFilter

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment provider

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		return getChildFragmentManager().findFragmentByTag(tag);
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (FTAG_SAVE_DIALOG.equals(tag)) {
			return FlightTravelerSaveDialogFragment.newInstance();
		}
		else if (FTAG_OVERWRITE_DIALOG.equals(tag)) {
			return OverwriteExistingTravelerDialogFragment.newInstance();
		}
		else if (FTAG_SAVING.equals(tag)) {
			return ThrobberDialog.newInstance(getString(R.string.saving_traveler));
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FTAG_SAVING.equals(tag)) {
			((ThrobberDialog) frag).setCancelable(false);
			((ThrobberDialog) frag).setCancelListener(new ThrobberDialog.CancelListener() {
				@Override
				public void onCancel() {

				}
			});
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// State Provider

	private StateListenerCollection<TravelerFormState> mTravelerFormStateListeners = new StateListenerCollection<TravelerFormState>();

	@Override
	public void startStateTransition(TravelerFormState stateOne, TravelerFormState stateTwo) {
		mTravelerFormStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(TravelerFormState stateOne, TravelerFormState stateTwo, float percentage) {
		mTravelerFormStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(TravelerFormState stateOne, TravelerFormState stateTwo) {
		mTravelerFormStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(TravelerFormState state) {
		mTravelerFormStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<TravelerFormState> listener, boolean fireFinalizeState) {
		mTravelerFormStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<TravelerFormState> listener) {
		mTravelerFormStateListeners.unRegisterStateListener(listener);
	}

	//////////////////////////////////////////////////////////////////////////
	// IForwardBackwardListener for save/overwrite/saving popups

	@Override
	public void onDialogMoveForward() {
		proceed();
	}

	@Override
	public void onDialogMoveBackwards() {
		commitAndCloseForm();
	}


	//////////////////////////////////////////////////////////////////////////
	// Flight details download


	private BackgroundDownloader.Download<SignInResponse> mTravelerDetailsDownload = new BackgroundDownloader.Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(DL_FETCH_TRAVELER_INFO, services);
			return services.updateTraveler(Db.getWorkingTravelerManager().getWorkingTraveler(), 0);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<SignInResponse> mTravelerDetailsCallback = new BackgroundDownloader.OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {

			ThrobberDialog df = (ThrobberDialog) getChildFragmentManager().findFragmentByTag(FTAG_FETCH_TRAVELER_INFO);
			if (df != null) {
				df.dismiss();
			}

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
				PassengerCategory category = Db.getTravelers().get(mTravelerNumber).getPassengerCategory();
				results.getTraveler().setPassengerCategory(category);
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(results.getTraveler());
				bindToDb(mTravelerNumber);
			}
		}
	};
}
