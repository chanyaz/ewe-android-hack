package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutFormsFragment extends Fragment implements AccountButtonClickListener,
		ConfirmLogoutDialogFragment.DoLogoutListener, IBackManageable {

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	private ViewGroup mRootC;
	private LinearLayout mCheckoutFormsC;
	private ViewGroup mOverlayC;
	private ViewGroup mOverlayContentC;
	private View mOverlayShade;
	private AccountButton mAccountButton;
	private View mPaymentView;

	private LineOfBusiness mLob;

	//When we last refreshed user data.
	private long mRefreshedUserTime = 0L;

	public static TabletCheckoutFormsFragment newInstance() {
		TabletCheckoutFormsFragment frag = new TabletCheckoutFormsFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_forms, container, false);
		mCheckoutFormsC = Ui.findView(mRootC, R.id.checkout_forms_container);
		mOverlayC = Ui.findView(mRootC, R.id.overlay_container);
		mOverlayContentC = Ui.findView(mRootC, R.id.overlay_content_container);
		mOverlayShade = Ui.findView(mRootC, R.id.overlay_shade);

		if (mLob != null) {
			buildCheckoutForm();
		}

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}

		//We disable this for sign in, but when the user comes back it should be enabled.
		mAccountButton.setEnabled(true);

		mBackManager.registerWithParent(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_REFRESH_USER);
		}
		else {
			bd.unregisterDownloadCallback(KEY_REFRESH_USER);
		}

		if (Db.getTravelersAreDirty()) {
			Db.kickOffBackgroundTravelerSave(getActivity());
		}

		if (Db.getBillingInfoIsDirty()) {
			Db.kickOffBackgroundBillingInfoSave(getActivity());
		}

		mBackManager.unregisterWithParent(this);
	}

	/*
	 * BINDING
	 */

	public void bindAll() {
		refreshAccountButtonState();
		bindTravelers();
	}

	/*
	 * GETTERS / SETTERS
	 */

	public void setLob(LineOfBusiness lob) {
		boolean wasNull = false;
		if (mLob == null) {
			wasNull = true;
		}
		mLob = lob;
		if (wasNull && mRootC != null) {
			buildCheckoutForm();
		}
	}

	public LineOfBusiness getLob() {
		return mLob;
	}

	/*
	 * CHECKOUT FORM BUILDING METHODS
	 */

	private ArrayList<View> mViews = new ArrayList<View>();

	protected void buildCheckoutForm() {

		//CLEAR THE CONTAINER
		mCheckoutFormsC.removeAllViews();

		//FIRST HEADING
		String headingArg = "";
		if (getLob() == LineOfBusiness.FLIGHTS) {
			headingArg = "FLIGHT";
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			headingArg = "HOTEL";
		}
		addGroupHeading(getString(R.string.now_booking_TEMPLATE, headingArg));

		//LOGIN STUFF
		mAccountButton = Ui.inflate(R.layout.include_account_button, mCheckoutFormsC, false);
		mAccountButton.setListener(this);
		add(mAccountButton);

		//TRAVELERS
		populateTravelerData();
		addGroupHeading(R.string.traveler_information);
		for (int i = 0; i < Db.getTravelers().size(); i++) {
			addTravelerView(i);
		}

		//PAYMENT
		addGroupHeading(R.string.payment_method);
		mPaymentView = Ui.inflate(R.layout.section_display_creditcard_btn, mCheckoutFormsC, false);
		dressCheckoutView(mPaymentView, 0);
		addActionable(mPaymentView, new Runnable() {
			@Override
			public void run() {
				openPaymentForm();
			}
		});

		bindAll();

	}

	protected View addGroupHeading(int resId) {
		CharSequence seq = getString(resId);
		return addGroupHeading(seq);
	}

	protected View addGroupHeading(CharSequence headingText) {
		TextView tv = Ui.inflate(R.layout.checkout_form_tablet_heading, mCheckoutFormsC, false);
		tv.setText(Html.fromHtml(headingText.toString()));
		return add(tv);
	}

	protected View addActionable(int resId, final Runnable action) {
		View view = Ui.inflate(resId, mCheckoutFormsC, false);
		return addActionable(view, action);
	}

	protected View addActionable(View view, final Runnable action) {
		if (action != null) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					arg0.post(action);
				}
			});
		}
		return add(view);
	}

	public View add(View view) {
		mViews.add(view);
		mCheckoutFormsC.addView(view);
		return view;
	}

	private void dressCheckoutView(View dressableView, int groupIndex) {
		if (groupIndex == 0) {
			dressableView.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
		}
		else {
			dressableView.setBackgroundResource(R.drawable.bg_checkout_information_middle_tab);
		}
		int padding = getResources().getDimensionPixelSize(R.dimen.traveler_button_padding);
		dressableView.setPadding(padding, padding, padding, padding);
	}

	/*
	 * TRAVELER FORM STUFF
	 */

	private ArrayList<SectionTravelerInfo> mTravelerViews = new ArrayList<SectionTravelerInfo>();

	protected void addTravelerView(final int travelerNumber) {
		SectionTravelerInfo travelerSection = Ui.inflate(R.layout.section_display_traveler_info_btn, mCheckoutFormsC,
				false);
		dressCheckoutView(travelerSection, travelerNumber);
		addActionable(travelerSection, new Runnable() {

			@Override
			public void run() {
				openTravelerEntry(travelerNumber);
			}

		});
		mTravelerViews.add(travelerSection);
	}

	protected void openTravelerEntry(int travelerNumber) {
		//finding index
		View travSection = mTravelerViews.get(travelerNumber);
		int viewNumber = -1;
		for (int i = 0; i < mViews.size(); i++) {
			if (mViews.get(i) == travSection) {
				viewNumber = i;
				break;
			}
		}
		if (viewNumber >= 0) {
			setFormShowing(!mFormShowing, true, viewNumber);
		}
	}

	private boolean mFormShowing = false;
	private int mShowingViewIndex = -1;

	protected void setFormShowing(boolean show, boolean animate, int viewIndex) {
		float startVal = show ? 0f : 1f;
		float endVal = show ? 1f : 0f;

		mFormShowing = show;
		mShowingViewIndex = show ? viewIndex : -1;
		mOverlayContentC.setAlpha(startVal);
		mOverlayShade.setAlpha(startVal);
		mOverlayC.setVisibility(View.VISIBLE);

		int dist = mViews.get(viewIndex).getTop();

		ValueAnimator anim = ValueAnimator.ofFloat(startVal, endVal);
		anim.setDuration(1000);
		anim.addUpdateListener(generateViewMoveListener(viewIndex, dist));
		anim.addUpdateListener(entryFormFadeInListener);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animator) {
				mOverlayC.setVisibility(mFormShowing ? View.VISIBLE : View.GONE);
			}
		});
		anim.start();

	}

	protected AnimatorUpdateListener entryFormFadeInListener = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float val = (Float) animation.getAnimatedValue();
			mOverlayContentC.setAlpha(val);
			mOverlayShade.setAlpha(val);

		}

	};

	protected AnimatorUpdateListener generateViewMoveListener(final int viewIndex, final int totalDistance) {
		return new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float val = (Float) arg0.getAnimatedValue();
				float activeScaleY = 1f + val
						* ((mOverlayContentC.getHeight() / mViews.get(viewIndex).getHeight()) - 1f);
				float aboveViewsTransY = val * totalDistance;
				float activeViewTransY = val
						* (mViews.get(viewIndex).getTop() / 2f - mViews.get(viewIndex).getHeight() / 2f);
				float belowViewsTransY = val * (mOverlayContentC.getBottom() - mViews.get(viewIndex).getBottom());
				for (int i = 0; i < viewIndex; i++) {
					mViews.get(i).setTranslationY(-aboveViewsTransY);
				}
				mViews.get(viewIndex).setTranslationY(-activeViewTransY);
				mViews.get(viewIndex).setAlpha(1f - val);
				for (int i = viewIndex + 1; i < mViews.size(); i++) {
					mViews.get(i).setTranslationY(belowViewsTransY);
				}

			}
		};
	}

	protected void bindTravelers() {
		for (int i = 0; i < mTravelerViews.size() && i < Db.getTravelers().size(); i++) {
			mTravelerViews.get(i).bind(Db.getTravelers().get(i));
		}
	}

	private void populateTravelerData() {
		List<Traveler> travelers = Db.getTravelers();
		if (travelers == null) {
			travelers = new ArrayList<Traveler>();
			Db.setTravelers(travelers);
		}

		// If there are more numAdults from HotelSearchParams, add empty Travelers to the Db to anticipate the addition of
		// new Travelers in order for check out
		final int numTravelers = travelers.size();
		int numAdults = travelers.size();
		if (getLob() == LineOfBusiness.FLIGHTS) {
			numAdults = Db.getFlightSearch().getSearchParams().getNumAdults();
		}
		else {
			//Hotels currently always just has one traveler object
			numAdults = 1;
		}
		if (numTravelers < numAdults) {
			for (int i = numTravelers; i < numAdults; i++) {
				travelers.add(new Traveler());
			}
		}

		// If there are more Travelers than number of adults required by the HotelSearchParams, remove the extra Travelers,
		// although, keep the first numAdults Travelers.
		else if (numTravelers > numAdults) {
			for (int i = numTravelers - 1; i >= numAdults; i--) {
				travelers.remove(i);
			}
		}
	}

	/*
	 * PAYMENT FORM STUFF
	 */

	protected void openPaymentForm() {
		//finding index
		int viewNumber = -1;
		for (int i = 0; i < mViews.size(); i++) {
			if (mViews.get(i) == mPaymentView) {
				viewNumber = i;
				break;
			}
		}
		if (viewNumber >= 0) {
			//moveViewsUp(viewNumber, travSection.getTop());
			setFormShowing(!mFormShowing, true, viewNumber);
		}
	}

	/*
	 * ACCOUNT BUTTON
	 */

	private void refreshAccountButtonState() {
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
					&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
				//We have a user (either from memory, or loaded from disk)
				int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval);
				if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
					Log.d("Refreshing user profile...");

					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(KEY_REFRESH_USER)) {
						bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
					}
				}
				mAccountButton.bind(false, true, Db.getUser(), true);
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, true);
			}
		}
		else {
			mAccountButton.bind(false, false, null, true);
		}
	}

	@Override
	public void accountLoginClicked() {
		if (mAccountButton.isEnabled()) {
			mAccountButton.setEnabled(false);

			Bundle args = null;
			if (getLob() == LineOfBusiness.FLIGHTS) {
				String itinNum = Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber();
				String tripId = Db.getItinerary(itinNum).getTripId();
				args = LoginActivity.createArgumentsBundle(mLob, new UserToTripAssocLoginExtender(
						tripId));
				OmnitureTracking.trackPageLoadFlightLogin(getActivity());
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				args = LoginActivity.createArgumentsBundle(LineOfBusiness.HOTELS, null);
				OmnitureTracking.trackPageLoadHotelsLogin(getActivity());
			}

			User.signIn(getActivity(), args);
		}
	}

	@Override
	public void accountLogoutClicked() {
		ConfirmLogoutDialogFragment df = new ConfirmLogoutDialogFragment();
		df.show(this.getFragmentManager(), ConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUserTime = 0L;

		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null, true);
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);
		mRefreshedUserTime = System.currentTimeMillis();
	}

	/*
	 * ACCOUNT REFRESH DOWNLOAD
	 */

	private final Download<SignInResponse> mRefreshUserDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REFRESH_USER, services);
			//Why flights AND hotels? Because the api will return blank for loyaltyMembershipNumber on flights
			return services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mRefreshUserCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			if (results == null || results.hasErrors()) {
				//The refresh failed, so we just log them out. They can always try to login again.
				doLogout();
			}
			else {
				// Update our existing saved data
				User user = results.getUser();
				user.save(getActivity());
				Db.setUser(user);

				// Act as if a login just occurred
				onLoginCompleted();
			}
		}
	};

	/*
	 * BACKMANAGEABLE
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			if (mFormShowing) {
				setFormShowing(!mFormShowing, true, mShowingViewIndex);
				return true;
			}

			return false;
		}

	};
}
