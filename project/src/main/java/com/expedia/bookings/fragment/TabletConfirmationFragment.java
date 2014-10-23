package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;

public abstract class TabletConfirmationFragment extends LobableFragment {

	protected abstract String getItinNumber();

	protected abstract LineOfBusiness getNextBookingItem();

	protected abstract String getConfirmationSummaryText();

	protected abstract void shareItinerary();

	protected abstract void addItineraryToCalendar();

	private ViewGroup mBookNextContainer;
	private ViewGroup mDoneBookingContainer;
	private View mImageCard;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}

		View v = inflater.inflate(R.layout.fragment_tablet_confirmation, container, false);

		Ui.setText(v, R.id.confirmation_summary_text, getConfirmationSummaryText());

		Ui.setText(v, R.id.confirmation_itinerary_text_view, getString(R.string.tablet_itinerary_confirmation_TEMPLATE, getItinNumber(), Db.getBillingInfo().getEmail()));

		// Inflate the custom actions layout id
		ViewGroup actionContainer = Ui.findView(v, R.id.custom_actions_container);
		inflater.inflate(R.layout.include_tablet_confirmation_actions_layout, actionContainer, true);

		// Setup a dropping animation with the image card.  Only animate on versions of Android
		// that will allow us to make the animation nice and smooth.
		mImageCard = Ui.findView(v, R.id.confirmation_image_view);
		if (savedInstanceState == null) {
			mImageCard.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					mImageCard.getViewTreeObserver().removeOnPreDrawListener(this);
					animateImageCard();
					return false;
				}
			});
		}

		//////////////////////////
		/// Bottom button layout related

		mBookNextContainer = Ui.findView(v, R.id.confirmation_book_next_container);
		mBookNextContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				OmnitureTracking.trackBookNextClick(getActivity(), getLob());
				LineOfBusiness nextLob = getNextBookingItem();
				if (nextLob == LineOfBusiness.HOTELS) {
					Db.getTripBucket().getHotel().setSelected(true);
					Db.getTripBucket().getFlight().setSelected(false);
				}
				if (nextLob == LineOfBusiness.FLIGHTS) {
					Db.getTripBucket().getFlight().setSelected(true);
					Db.getTripBucket().getHotel().setSelected(false);
				}
				Events.post(new Events.BookingConfirmationBookNext(nextLob));
			}
		});

		if (getNextBookingItem() == null) {
			mBookNextContainer.setVisibility(View.GONE);
			Ui.findView(v, R.id.confirmation_booking_bar_separator).setVisibility(View.GONE);
		}

		mDoneBookingContainer = Ui.findView(v, R.id.confirmation_done_booking_container);
		mDoneBookingContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				OmnitureTracking.trackDoneBookingClick(getActivity(), getLob());
				NavUtils.goToItin(getActivity());
				getActivity().finish();
			}
		});

		//////////////////////////
		/// Action button layout related

		Ui.setOnClickListener(v, R.id.call_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getActivity(), PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
			}
		});

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareItinerary();
			}
		});

		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
			Ui.setOnClickListener(v, R.id.calendar_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					addItineraryToCalendar();
				}
			});
		}
		else {
			Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
		}

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// Animation

	private void animateImageCard() {
		// Animate the card from -height to 0
		mImageCard.setTranslationY(-mImageCard.getHeight());

		ViewPropertyAnimator animator = mImageCard.animate();
		animator.translationY(0);
		animator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime) * 2);
		animator.setInterpolator(new OvershootInterpolator());

		if (Build.VERSION.SDK_INT >= 16) {
			animator.withLayer();
		}
		else {
			mImageCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			animator.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mImageCard.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			});
		}

		animator.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		//We don't need to do anything else.
	}

}
