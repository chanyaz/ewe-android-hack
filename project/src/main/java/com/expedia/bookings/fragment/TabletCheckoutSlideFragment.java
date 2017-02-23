package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.widget.SlideToWidgetJB;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

public class TabletCheckoutSlideFragment extends LobableFragment implements ICheckoutDataListener,
	CheckoutLoginButtonsFragment.ILoginStateChangedListener {

	private static final String HAS_ACCEPTED_TOS = "HAS_ACCEPTED_TOS";
	private static final String ARG_TOTAL_PRICE_STRING = "ARG_TOTAL_PRICE";

	private ViewGroup mRootC;
	private ViewGroup mAcceptContainer;
	private ViewGroup mSlideContainer;
	private ViewGroup mBookContainer;
	private SlideToWidgetJB mSlideToWidget;

	private boolean mHasAcceptedTOS;
	private String mTotalPriceString;

	public static TabletCheckoutSlideFragment newInstance() {
		return new TabletCheckoutSlideFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		boolean isCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
		if (isCheckout) {
			TabletCheckoutControllerFragment frag = (TabletCheckoutControllerFragment) getParentFragment();
			frag.registerStateListener(mStateHelper, true);
		}
	}

	private StateListenerHelper<CheckoutState> mStateHelper = new StateListenerHelper<CheckoutState>() {

		@Override
		public void onStateTransitionStart(CheckoutState stateOne, CheckoutState stateTwo) {
		}

		@Override
		public void onStateTransitionUpdate(CheckoutState stateOne, CheckoutState stateTwo, float percentage) {
		}

		@Override
		public void onStateTransitionEnd(CheckoutState stateOne, CheckoutState stateTwo) {
		}

		@Override
		public void onStateFinalized(CheckoutState state) {
			resetSlider();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_slide_to_purchase, container, false);

		mRootC.setBackgroundColor(Db.getFullscreenAverageColor());

		if (savedInstanceState != null && savedInstanceState.containsKey(HAS_ACCEPTED_TOS)) {
			mHasAcceptedTOS = savedInstanceState.getBoolean(HAS_ACCEPTED_TOS);
		}
		else {
			mHasAcceptedTOS = !(PointOfSale.getPointOfSale().requiresRulesRestrictionsCheckbox());
		}

		// Total price string
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_TOTAL_PRICE_STRING)) {
			mTotalPriceString = savedInstanceState.getString(ARG_TOTAL_PRICE_STRING);
		}
		TextView price = Ui.findView(mRootC, R.id.purchase_total_text_view);
		price.setText(mTotalPriceString);

		mAcceptContainer = Ui.findView(mRootC, R.id.accept_tos_container);
		mSlideContainer = Ui.findView(mRootC, R.id.slide_container);
		mBookContainer = Ui.findView(mRootC, R.id.book_container);

		Ui.findView(mAcceptContainer, R.id.layout_i_accept).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHasAcceptedTOS = true;
				hideAcceptTOS(true);
			}
		});

		mSlideToWidget = Ui.findView(mRootC, R.id.slide_to_purchase_widget);
		if (getParentFragment() instanceof SlideToWidgetJB.ISlideToListener) {
			mSlideToWidget.addSlideToListener((SlideToWidgetJB.ISlideToListener) getParentFragment());
		}

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();

		Events.register(this);

		bindAll();
	}

	@Override
	public void onPause() {
		super.onPause();

		Events.unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ACCEPTED_TOS, mHasAcceptedTOS);
		outState.putString(ARG_TOTAL_PRICE_STRING, mTotalPriceString);
	}

	/*
	 * LobableFragment
	 */

	@Override
	public void onLobSet(LineOfBusiness lob) {
		bindAll();
	}

	/*
	 * BINDING
	 */

	public void bindAll() {
		if (mRootC == null) {
			return;
		}

		mSlideToWidget.resetSlider();
		if (mHasAcceptedTOS) {
			hideAcceptTOS(false);
		}
		else {
			showAcceptTOS();
		}

		LineOfBusiness lob = getLob();
		if (lob != null) {
			switch (lob) {
			case FLIGHTS:
				mSlideToWidget.setText(R.string.slide_to_book_flight);
				break;
			case HOTELS:
				mSlideToWidget.setText(R.string.slide_to_book_hotel);
				break;
			default:
				//should not get here
			}
		}
	}

	public void setPriceFromTripBucket() {
		if (getActivity() == null) {
			return;
		}
		switch (getLob()) {
		case FLIGHTS: {
			TripBucketItemFlight item = Db.getTripBucket().getFlight();
			setTotalPriceString(FlightUtils.getSlideToPurchaseString(getActivity(), item));
			break;
		}
		case HOTELS: {
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			Property property = hotel.getProperty();
			Rate rate = hotel.getRate();
			setTotalPriceString(HotelUtils.getSlideToPurchaseString(getActivity(), property, rate,
				ExpediaBookingApp.useTabletInterface()));
			break;
		}
		}
	}

	public void setTotalPriceString(String totalPriceString) {
		mTotalPriceString = totalPriceString;

		if (mRootC != null) {
			TextView price = Ui.findView(mRootC, R.id.purchase_total_text_view);
			price.setText(mTotalPriceString);
		}
	}

	public void resetSlider() {
		if (mSlideToWidget != null) {
			mSlideToWidget.resetSlider();
		}
	}

	/*
	 * ICheckoutDataListener
	 */

	@Override
	public void onCheckoutDataUpdated() {
		bindAll();
	}

	/*
	 * CheckoutLoginButtonsFragment.ILoginStateChangedListener
	 */

	@Override
	public void onLoginStateChanged() {
		bindAll();
	}

	/*
	 * Show/hide "I accept TOS" button, smoothly
	 */

	private void hideAcceptTOS(final boolean animated) {

		// Short circuit if it's already hidden
		if (mAcceptContainer.getVisibility() == View.INVISIBLE) {
			mSlideToWidget.resetSlider();
			return;
		}

		// Skip animation if requested
		if (!animated) {
			mAcceptContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.VISIBLE);
			mSlideToWidget.resetSlider();
			mBookContainer.setVisibility(View.INVISIBLE);
			return;
		}

		View iAccept = Ui.findView(mAcceptContainer, R.id.layout_i_accept);
		View iAcceptLeft = Ui.findView(mAcceptContainer, R.id.i_accept_left_image);
		View iAcceptCenter = Ui.findView(mAcceptContainer, R.id.i_accept_center_text);
		View iAcceptRight = Ui.findView(mAcceptContainer, R.id.i_accept_right_image);
		View labelDoYouAccept = Ui.findView(mAcceptContainer, R.id.do_you_accept_label);
		View sliderImage = Ui.findView(mSlideContainer, R.id.touch_target);

		List<Animator> iAcceptList = new ArrayList<Animator>();

		// Fade out the "do you accept" label
		iAcceptList.add(ObjectAnimator.ofFloat(labelDoYouAccept, "alpha", 1f, 0f));

		// Gracefully morph the "I accept" button into the slide to accept circle
		Rect sliderRect = new Rect();
		sliderImage.getGlobalVisibleRect(sliderRect);

		// I accept layout should move itself and its children over
		// to fit on top of the slide to purchase button.
		Rect iAcceptRect = new Rect();
		iAcceptLeft.getGlobalVisibleRect(iAcceptRect);
		float translateX = (float) sliderRect.left - iAcceptRect.left + 68;
		float translateY = (float) sliderRect.top - iAcceptRect.top + 32;
		iAcceptList.add(ObjectAnimator.ofPropertyValuesHolder(iAccept,
			PropertyValuesHolder.ofFloat("translationX", 0f, translateX),
			PropertyValuesHolder.ofFloat("translationY", 0f, translateY),
			PropertyValuesHolder.ofFloat("scaleX", 1f, 1.34f),
			PropertyValuesHolder.ofFloat("scaleY", 1f, 1.34f)
		));

		// Right half of the I accept button
		// should slide over to butt up against the left half
		translateX = iAcceptLeft.getRight() - iAcceptRight.getLeft();
		iAcceptList.add(ObjectAnimator.ofPropertyValuesHolder(iAcceptRight,
			PropertyValuesHolder.ofFloat("translationX", 0f, translateX)
		));

		// Middle of the I accept button should shrink down to nothing
		translateX = -iAcceptCenter.getWidth() / 2.0f;
		iAcceptList.add(ObjectAnimator.ofPropertyValuesHolder(iAcceptCenter,
			PropertyValuesHolder.ofFloat("translationX", 0f, translateX),
			PropertyValuesHolder.ofFloat("scaleX", 1f, 0f)
		));

		// All of the "I accept" animators put together
		AnimatorSet iAcceptAnim = new AnimatorSet();
		iAcceptAnim.playTogether(iAcceptList);
		iAcceptAnim.setDuration(250);

		// Fade in the "slide to purchase"
		Animator slideToAnim = ObjectAnimator.ofFloat(mSlideContainer, "alpha", 0f, 1f);
		slideToAnim.setDuration(100);

		AnimatorSet allAnim = new AnimatorSet();
		allAnim.playSequentially(iAcceptAnim, slideToAnim);
		allAnim.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationCancel(Animator arg0) {
				onAnimationEnd(arg0);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAcceptContainer.setVisibility(View.INVISIBLE);
				mSlideContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				mAcceptContainer.setVisibility(View.VISIBLE);
				mSlideContainer.setAlpha(0f);
				mSlideContainer.setVisibility(View.VISIBLE);
			}
		});
		allAnim.start();
	}

	private void showAcceptTOS() {
		mAcceptContainer.setVisibility(View.VISIBLE);
		mSlideContainer.setVisibility(View.INVISIBLE);
		mBookContainer.setVisibility(View.INVISIBLE);
		mSlideToWidget.resetSlider();

		View iAccept = Ui.findView(mAcceptContainer, R.id.layout_i_accept);
		View iAcceptCenter = Ui.findView(mAcceptContainer, R.id.i_accept_center_text);
		View iAcceptRight = Ui.findView(mAcceptContainer, R.id.i_accept_right_image);
		View labelDoYouAccept = Ui.findView(mAcceptContainer, R.id.do_you_accept_label);
		labelDoYouAccept.setAlpha(1f);
		iAccept.setTranslationX(0f);
		iAccept.setTranslationY(0f);
		iAccept.setScaleX(1f);
		iAccept.setScaleY(1f);
		iAcceptRight.setTranslationX(0f);
		iAcceptCenter.setScaleX(1f);
		iAcceptCenter.setTranslationX(0f);
	}

	/*
	 * Otto events
	 */

	@Subscribe
	public void onHotelProductRateUp(Events.HotelProductRateUp event) {
		setPriceFromTripBucket();
	}

	@Subscribe
	public void onFlightPriceChange(Events.FlightPriceChange event) {
		setPriceFromTripBucket();
	}

	@Subscribe
	public void onCouponApplied(Events.CouponApplyDownloadSuccess event) {
		setPriceFromTripBucket();
	}

	@Subscribe
	public void onCouponRemoved(Events.CouponRemoveDownloadSuccess event) {
		setPriceFromTripBucket();
	}

	@Subscribe
	public void onLCCPaymentFeesAdded(Events.LCCPaymentFeesAdded event) {
		setPriceFromTripBucket();
	}
}
